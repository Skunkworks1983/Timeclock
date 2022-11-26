/*
 * This file is generated by jOOQ.
 */
package io.github.skunkworks1983.timeclock.db.generated.tables;


import io.github.skunkworks1983.timeclock.db.generated.DefaultSchema;
import io.github.skunkworks1983.timeclock.db.generated.Keys;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.MembersRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Members extends TableImpl<MembersRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>members</code>
     */
    public static final Members MEMBERS = new Members();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MembersRecord> getRecordType() {
        return MembersRecord.class;
    }

    /**
     * The column <code>members.id</code>.
     */
    public final TableField<MembersRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36), this, "");

    /**
     * The column <code>members.role</code>.
     */
    public final TableField<MembersRecord, String> ROLE = createField(DSL.name("role"), SQLDataType.VARCHAR, this, "");

    /**
     * The column <code>members.firstName</code>.
     */
    public final TableField<MembersRecord, String> FIRSTNAME = createField(DSL.name("firstName"), SQLDataType.VARCHAR, this, "");

    /**
     * The column <code>members.lastName</code>.
     */
    public final TableField<MembersRecord, String> LASTNAME = createField(DSL.name("lastName"), SQLDataType.VARCHAR, this, "");

    /**
     * The column <code>members.hours</code>.
     */
    public final TableField<MembersRecord, Float> HOURS = createField(DSL.name("hours"), SQLDataType.REAL, this, "");

    /**
     * The column <code>members.lastSignedIn</code>.
     */
    public final TableField<MembersRecord, Integer> LASTSIGNEDIN = createField(DSL.name("lastSignedIn"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>members.isSignedIn</code>.
     */
    public final TableField<MembersRecord, Integer> ISSIGNEDIN = createField(DSL.name("isSignedIn"), SQLDataType.INTEGER, this, "");

    private Members(Name alias, Table<MembersRecord> aliased) {
        this(alias, aliased, null);
    }

    private Members(Name alias, Table<MembersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>members</code> table reference
     */
    public Members(String alias) {
        this(DSL.name(alias), MEMBERS);
    }

    /**
     * Create an aliased <code>members</code> table reference
     */
    public Members(Name alias) {
        this(alias, MEMBERS);
    }

    /**
     * Create a <code>members</code> table reference
     */
    public Members() {
        this(DSL.name("members"), null);
    }

    public <O extends Record> Members(Table<O> child, ForeignKey<O, MembersRecord> key) {
        super(child, key, MEMBERS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<MembersRecord> getPrimaryKey() {
        return Keys.MEMBERS__;
    }

    @Override
    public Members as(String alias) {
        return new Members(DSL.name(alias), this);
    }

    @Override
    public Members as(Name alias) {
        return new Members(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Members rename(String name) {
        return new Members(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Members rename(Name name) {
        return new Members(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<String, String, String, String, Float, Integer, Integer> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
