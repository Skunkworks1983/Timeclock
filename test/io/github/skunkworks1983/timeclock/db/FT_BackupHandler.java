package io.github.skunkworks1983.timeclock.db;

import io.github.skunkworks1983.timeclock.controller.AdminController;
import io.github.skunkworks1983.timeclock.ui.TextToSpeechHandler;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FT_BackupHandler
{
    private final String TEST_DB_FILE = "testdb";
    private final String MERGE_DB_FILE = "testdb-merge";
    
    private LocalDateTime sessionStart = TimeUtil.getDateTime(TimeUtil.getCurrentTimestamp());
    
    private ScheduleStore scheduleStore;
    private SessionStore sessionStore;
    private MemberStore memberStore;
    private SigninStore signinStore;
    private PinStore pinStore;
    
    private AdminController adminController;
    
    private Member admin;
    private Member student1;
    private Member student2;
    
    private BackupHandler backupHandler;
    
    @Before
    public void setup() throws Exception
    {
        Files.deleteIfExists(Path.of(TEST_DB_FILE));
        Files.deleteIfExists(Path.of(MERGE_DB_FILE));
        
        DatabaseConnector.setUpDatabase(TEST_DB_FILE);
        
        scheduleStore = new ScheduleStore();
        sessionStore = new SessionStore(scheduleStore);
        memberStore = new MemberStore(sessionStore);
        signinStore = new SigninStore();
        pinStore = new PinStore();
        
        admin = new Member(UUID.randomUUID(), Role.ADMIN, "admin", "admin", 0, 0, false, 0);
        student1 = new Member(UUID.randomUUID(), Role.STUDENT, "student", "1", 0, 0, false, 0);
        student2 = new Member(UUID.randomUUID(), Role.STUDENT, "student", "2", 0, 0, false, 0);
        
        memberStore.createMember(admin);
        memberStore.createMember(student1);
        memberStore.createMember(student2);
        
        sessionStart = sessionStart.with(ChronoField.DAY_OF_WEEK, DayOfWeek.TUESDAY.getValue())
                                   .with(ChronoField.HOUR_OF_DAY, 18)
                                   .with(ChronoField.MINUTE_OF_HOUR, 0)
                                   .with(ChronoField.SECOND_OF_MINUTE, 0)
                                   .with(ChronoField.MICRO_OF_SECOND, 0);
        
        adminController = new AdminController(memberStore, signinStore, pinStore, sessionStore, scheduleStore,
                                              new TextToSpeechHandler()
                                              {
                                                  @Override
                                                  public void speak(String text)
                                                  {
                                                      System.out.println(text);
                                                  }
                                              }, backupHandler);
        
        adminController.createGroupSignIn(Arrays.asList(admin, student1, student2), admin, sessionStart.atOffset(
                OffsetDateTime.now().getOffset()), sessionStart.plusHours(2).atOffset(OffsetDateTime.now().getOffset()));
        
        Files.copy(Path.of(TEST_DB_FILE), Path.of(MERGE_DB_FILE));
        
        backupHandler = new BackupHandler(signinStore, memberStore, null);
    }
    
    @After
    public void teardown() throws Exception
    {
        Files.delete(Path.of(TEST_DB_FILE));
        Files.delete(Path.of(MERGE_DB_FILE));
    }
    
    @Test
    public void mergeTest() throws Exception
    {
        LocalDateTime conflictingSessionStart = sessionStart.plusHours(1);
        LocalDateTime conflictingSessionEnd = conflictingSessionStart.plusHours(2);
        DatabaseConnector.setDatabaseFile(MERGE_DB_FILE);
        sessionStore.createPreviousSession(admin, TimeUtil.getEpochSeconds(conflictingSessionStart),
                                           TimeUtil.getEpochSeconds(conflictingSessionStart.plusHours(2)));
        memberStore.addPreviousSignIn(admin, TimeUtil.getEpochSeconds(conflictingSessionStart), TimeUtil.getEpochSeconds(conflictingSessionEnd));
        memberStore.addPreviousSignIn(student1, TimeUtil.getEpochSeconds(conflictingSessionStart), TimeUtil.getEpochSeconds(conflictingSessionStart.plusMinutes(30)));
        memberStore.addPreviousSignIn(student2, TimeUtil.getEpochSeconds(conflictingSessionStart.plusMinutes(60).plusSeconds(1)), TimeUtil.getEpochSeconds(conflictingSessionEnd));
        
        Member student3 = new Member(UUID.randomUUID(), Role.STUDENT, "student", "3", 0, 0, false, 0);
        memberStore.createMember(student3);
        
        char[] pin = new char[]{'1', '2', '3', '4'};
        pinStore.createPin(student3.getId(), pin);
        
        DatabaseConnector.setDatabaseFile(TEST_DB_FILE);
        backupHandler.mergeOtherDatabase(MERGE_DB_FILE);
        
        List<Member> updatedMembers = memberStore.getMembers();
        for(Member updatedMember: updatedMembers)
        {
            if(updatedMember.getId().equals(admin.getId()))
            {
                admin = updatedMember;
            }
            else if(updatedMember.getId().equals(student1.getId()))
            {
                student1 = updatedMember;
            }
            else if(updatedMember.getId().equals(student2.getId()))
            {
                student2 = updatedMember;
            }
        }
        
        assertThat(admin.getHours(), new DoubleEquals(3));
        assertThat(student1.getHours(), new DoubleEquals(2));
        assertThat(student2.getHours(), new DoubleEquals(3));
        
        assertThat("student3 exists", updatedMembers.stream().anyMatch(m -> m.getId().equals(student3.getId())), is(true));
        assertThat("pin is 1234", pinStore.checkPin(student3.getId(), pin), is(true));
    }
    
    @Test
    public void multiSessionMergeTest() throws Exception
    {
        LocalDateTime conflictingSessionStart = sessionStart.plusHours(1);
        LocalDateTime conflictingSessionEnd = conflictingSessionStart.plusHours(2);
        DatabaseConnector.setDatabaseFile(MERGE_DB_FILE);
        sessionStore.createPreviousSession(admin, TimeUtil.getEpochSeconds(conflictingSessionStart),
                                           TimeUtil.getEpochSeconds(conflictingSessionStart.plusHours(2)));
        memberStore.addPreviousSignIn(admin, TimeUtil.getEpochSeconds(conflictingSessionStart), TimeUtil.getEpochSeconds(conflictingSessionEnd));
        memberStore.addPreviousSignIn(student1, TimeUtil.getEpochSeconds(conflictingSessionStart), TimeUtil.getEpochSeconds(conflictingSessionStart.plusMinutes(30)));
        memberStore.addPreviousSignIn(student2, TimeUtil.getEpochSeconds(conflictingSessionStart.plusMinutes(60).plusSeconds(1)), TimeUtil.getEpochSeconds(conflictingSessionEnd));
    
        LocalDateTime nonConflictingSessionStart = sessionStart.plusDays(1);
        LocalDateTime nonConflictingSessionEnd = nonConflictingSessionStart.plusHours(2);
    
        sessionStore.createPreviousSession(admin, TimeUtil.getEpochSeconds(nonConflictingSessionStart),
                                           TimeUtil.getEpochSeconds(nonConflictingSessionEnd));
        memberStore.addPreviousSignIn(admin, TimeUtil.getEpochSeconds(nonConflictingSessionStart), TimeUtil.getEpochSeconds(nonConflictingSessionEnd));
        memberStore.addPreviousSignIn(student1, TimeUtil.getEpochSeconds(nonConflictingSessionStart), TimeUtil.getEpochSeconds(nonConflictingSessionEnd));
        memberStore.addPreviousSignIn(student2, TimeUtil.getEpochSeconds(nonConflictingSessionStart.plusMinutes(60)), TimeUtil.getEpochSeconds(nonConflictingSessionEnd));
        
        DatabaseConnector.setDatabaseFile(TEST_DB_FILE);
        
        backupHandler.mergeOtherDatabase(MERGE_DB_FILE);
    
        List<Member> updatedMembers = memberStore.getMembers();
        for(Member updatedMember: updatedMembers)
        {
            if(updatedMember.getId().equals(admin.getId()))
            {
                admin = updatedMember;
            }
            else if(updatedMember.getId().equals(student1.getId()))
            {
                student1 = updatedMember;
            }
            else if(updatedMember.getId().equals(student2.getId()))
            {
                student2 = updatedMember;
            }
        }
    
        assertThat(admin.getHours(), new DoubleEquals(1 + 2 + 2));
        assertThat(student1.getHours(), new DoubleEquals(2 + 0 + 2));
        assertThat(student2.getHours(), new DoubleEquals(1 + 1 + 2));
    }
    
    private static class DoubleEquals extends BaseMatcher<Double>
    {
        private double value;
        
        public DoubleEquals(double value)
        {
            this.value = value;
        }
        
        @Override
        public boolean matches(Object o)
        {
            if(!(o instanceof Double))
            {
                return false;
            }
            return (value - .001) < (Double)o && (Double)o < (value + .001);
        }
    
        @Override
        public void describeTo(Description description)
        {
            description.appendText(String.format("double value == %f (+- 0.001)", value));
        }
    }
}
