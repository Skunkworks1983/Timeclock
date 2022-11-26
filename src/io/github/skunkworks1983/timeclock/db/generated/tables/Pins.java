/*
 * This file is generated by jOOQ.
 */
package io.github.skunkworks1983.timeclock.db.generated.tables;


import io.github.skunkworks1983.timeclock.db.generated.DefaultSchema;
import io.github.skunkworks1983.timeclock.db.generated.Keys;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.PinsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
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
public class Pins extends TableImpl<PinsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>pins</code>
     */
    public static final Pins PINS = new Pins();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PinsRecord> getRecordType() {
        return PinsRecord.class;
    }

    /**
     * The column <code>pins.memberId</code>.
     */
    public final TableField<PinsRecord, String> MEMBERID = createField(DSL.name("memberId"), SQLDataType.VARCHAR(36), this, "");

    /**
     * The column <code>pins.salt</code>.
     */
    public final TableField<PinsRecord, byte[]> SALT = createField(DSL.name("salt"), SQLDataType.BLOB, this, "");

    /**
     * The column <code>pins.hash</code>.
     */
    public final TableField<PinsRecord, String> HASH = createField(DSL.name("hash"), SQLDataType.VARCHAR, this, "");

    private Pins(Name alias, Table<PinsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Pins(Name alias, Table<PinsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>pins</code> table reference
     */
    public Pins(String alias) {
        this(DSL.name(alias), PINS);
    }

    /**
     * Create an aliased <code>pins</code> table reference
     */
    public Pins(Name alias) {
        this(alias, PINS);
    }

    /**
     * Create a <code>pins</code> table reference
     */
    public Pins() {
        this(DSL.name("pins"), null);
    }

    public <O extends Record> Pins(Table<O> child, ForeignKey<O, PinsRecord> key) {
        super(child, key, PINS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<PinsRecord> getPrimaryKey() {
        return Keys.PINS__;
    }

    @Override
    public Pins as(String alias) {
        return new Pins(DSL.name(alias), this);
    }

    @Override
    public Pins as(Name alias) {
        return new Pins(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Pins rename(String name) {
        return new Pins(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Pins rename(Name name) {
        return new Pins(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, byte[], String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
