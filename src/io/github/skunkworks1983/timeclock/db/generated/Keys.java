/*
 * This file is generated by jOOQ.
 */
package io.github.skunkworks1983.timeclock.db.generated;


import io.github.skunkworks1983.timeclock.db.generated.tables.Members;
import io.github.skunkworks1983.timeclock.db.generated.tables.Pins;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.MembersRecord;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.PinsRecord;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Keys
{
    
    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------
    
    public static final UniqueKey<MembersRecord> MEMBERS__ = Internal.createUniqueKey(Members.MEMBERS, DSL.name(""),
                                                                                      new TableField[]{Members.MEMBERS.ID},
                                                                                      true);
    public static final UniqueKey<PinsRecord> PINS__ = Internal.createUniqueKey(Pins.PINS, DSL.name(""),
                                                                                new TableField[]{Pins.PINS.MEMBERID},
                                                                                true);
}
