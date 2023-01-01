/*
 * This file is generated by jOOQ.
 */
package io.github.skunkworks1983.timeclock.db.generated.tables.records;


import io.github.skunkworks1983.timeclock.db.generated.tables.Members;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class MembersRecord extends UpdatableRecordImpl<MembersRecord> implements Record8<String, String, String, String, Float, Integer, Long, Integer>
{
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Setter for <code>members.id</code>.
     */
    public void setId(String value)
    {
        set(0, value);
    }
    
    /**
     * Getter for <code>members.id</code>.
     */
    public String getId()
    {
        return (String) get(0);
    }
    
    /**
     * Setter for <code>members.role</code>.
     */
    public void setRole(String value)
    {
        set(1, value);
    }
    
    /**
     * Getter for <code>members.role</code>.
     */
    public String getRole()
    {
        return (String) get(1);
    }
    
    /**
     * Setter for <code>members.firstName</code>.
     */
    public void setFirstname(String value)
    {
        set(2, value);
    }
    
    /**
     * Getter for <code>members.firstName</code>.
     */
    public String getFirstname()
    {
        return (String) get(2);
    }
    
    /**
     * Setter for <code>members.lastName</code>.
     */
    public void setLastname(String value)
    {
        set(3, value);
    }
    
    /**
     * Getter for <code>members.lastName</code>.
     */
    public String getLastname()
    {
        return (String) get(3);
    }
    
    /**
     * Setter for <code>members.hours</code>.
     */
    public void setHours(Float value)
    {
        set(4, value);
    }
    
    /**
     * Getter for <code>members.hours</code>.
     */
    public Float getHours()
    {
        return (Float) get(4);
    }
    
    /**
     * Setter for <code>members.isSignedIn</code>.
     */
    public void setIssignedin(Integer value)
    {
        set(5, value);
    }
    
    /**
     * Getter for <code>members.isSignedIn</code>.
     */
    public Integer getIssignedin()
    {
        return (Integer) get(5);
    }
    
    /**
     * Setter for <code>members.lastSignedIn</code>.
     */
    public void setLastsignedin(Long value)
    {
        set(6, value);
    }
    
    /**
     * Getter for <code>members.lastSignedIn</code>.
     */
    public Long getLastsignedin()
    {
        return (Long) get(6);
    }
    
    /**
     * Setter for <code>members.penalties</code>.
     */
    public void setPenalties(Integer value)
    {
        set(7, value);
    }
    
    /**
     * Getter for <code>members.penalties</code>.
     */
    public Integer getPenalties()
    {
        return (Integer) get(7);
    }
    
    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------
    
    @Override
    public Record1<String> key()
    {
        return (Record1) super.key();
    }
    
    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------
    
    @Override
    public Row8<String, String, String, String, Float, Integer, Long, Integer> fieldsRow()
    {
        return (Row8) super.fieldsRow();
    }
    
    @Override
    public Row8<String, String, String, String, Float, Integer, Long, Integer> valuesRow()
    {
        return (Row8) super.valuesRow();
    }
    
    @Override
    public Field<String> field1()
    {
        return Members.MEMBERS.ID;
    }
    
    @Override
    public Field<String> field2()
    {
        return Members.MEMBERS.ROLE;
    }
    
    @Override
    public Field<String> field3()
    {
        return Members.MEMBERS.FIRSTNAME;
    }
    
    @Override
    public Field<String> field4()
    {
        return Members.MEMBERS.LASTNAME;
    }
    
    @Override
    public Field<Float> field5()
    {
        return Members.MEMBERS.HOURS;
    }
    
    @Override
    public Field<Integer> field6()
    {
        return Members.MEMBERS.ISSIGNEDIN;
    }
    
    @Override
    public Field<Long> field7()
    {
        return Members.MEMBERS.LASTSIGNEDIN;
    }
    
    @Override
    public Field<Integer> field8()
    {
        return Members.MEMBERS.PENALTIES;
    }
    
    @Override
    public String component1()
    {
        return getId();
    }
    
    @Override
    public String component2()
    {
        return getRole();
    }
    
    @Override
    public String component3()
    {
        return getFirstname();
    }
    
    @Override
    public String component4()
    {
        return getLastname();
    }
    
    @Override
    public Float component5()
    {
        return getHours();
    }
    
    @Override
    public Integer component6()
    {
        return getIssignedin();
    }
    
    @Override
    public Long component7()
    {
        return getLastsignedin();
    }
    
    @Override
    public Integer component8()
    {
        return getPenalties();
    }
    
    @Override
    public String value1()
    {
        return getId();
    }
    
    @Override
    public String value2()
    {
        return getRole();
    }
    
    @Override
    public String value3()
    {
        return getFirstname();
    }
    
    @Override
    public String value4()
    {
        return getLastname();
    }
    
    @Override
    public Float value5()
    {
        return getHours();
    }
    
    @Override
    public Integer value6()
    {
        return getIssignedin();
    }
    
    @Override
    public Long value7()
    {
        return getLastsignedin();
    }
    
    @Override
    public Integer value8()
    {
        return getPenalties();
    }
    
    @Override
    public MembersRecord value1(String value)
    {
        setId(value);
        return this;
    }
    
    @Override
    public MembersRecord value2(String value)
    {
        setRole(value);
        return this;
    }
    
    @Override
    public MembersRecord value3(String value)
    {
        setFirstname(value);
        return this;
    }
    
    @Override
    public MembersRecord value4(String value)
    {
        setLastname(value);
        return this;
    }
    
    @Override
    public MembersRecord value5(Float value)
    {
        setHours(value);
        return this;
    }
    
    @Override
    public MembersRecord value6(Integer value)
    {
        setIssignedin(value);
        return this;
    }
    
    @Override
    public MembersRecord value7(Long value)
    {
        setLastsignedin(value);
        return this;
    }
    
    @Override
    public MembersRecord value8(Integer value)
    {
        setPenalties(value);
        return this;
    }
    
    @Override
    public MembersRecord values(String value1, String value2, String value3, String value4, Float value5,
                                Integer value6, Long value7, Integer value8)
    {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    /**
     * Create a detached MembersRecord
     */
    public MembersRecord()
    {
        super(Members.MEMBERS);
    }
    
    /**
     * Create a detached, initialised MembersRecord
     */
    public MembersRecord(String id, String role, String firstname, String lastname, Float hours, Integer issignedin,
                         Long lastsignedin, Integer penalties)
    {
        super(Members.MEMBERS);
        
        setId(id);
        setRole(role);
        setFirstname(firstname);
        setLastname(lastname);
        setHours(hours);
        setIssignedin(issignedin);
        setLastsignedin(lastsignedin);
        setPenalties(penalties);
    }
}
