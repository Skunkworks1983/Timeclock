/*
 * This file is generated by jOOQ.
 */
package io.github.skunkworks1983.timeclock.db.generated.tables.records;


import io.github.skunkworks1983.timeclock.db.generated.tables.Pins;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PinsRecord extends UpdatableRecordImpl<PinsRecord> implements Record3<String, byte[], String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>pins.memberId</code>.
     */
    public void setMemberid(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>pins.memberId</code>.
     */
    public String getMemberid() {
        return (String) get(0);
    }

    /**
     * Setter for <code>pins.salt</code>.
     */
    public void setSalt(byte[] value) {
        set(1, value);
    }

    /**
     * Getter for <code>pins.salt</code>.
     */
    public byte[] getSalt() {
        return (byte[]) get(1);
    }

    /**
     * Setter for <code>pins.hash</code>.
     */
    public void setHash(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>pins.hash</code>.
     */
    public String getHash() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, byte[], String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, byte[], String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return Pins.PINS.MEMBERID;
    }

    @Override
    public Field<byte[]> field2() {
        return Pins.PINS.SALT;
    }

    @Override
    public Field<String> field3() {
        return Pins.PINS.HASH;
    }

    @Override
    public String component1() {
        return getMemberid();
    }

    @Override
    public byte[] component2() {
        return getSalt();
    }

    @Override
    public String component3() {
        return getHash();
    }

    @Override
    public String value1() {
        return getMemberid();
    }

    @Override
    public byte[] value2() {
        return getSalt();
    }

    @Override
    public String value3() {
        return getHash();
    }

    @Override
    public PinsRecord value1(String value) {
        setMemberid(value);
        return this;
    }

    @Override
    public PinsRecord value2(byte[] value) {
        setSalt(value);
        return this;
    }

    @Override
    public PinsRecord value3(String value) {
        setHash(value);
        return this;
    }

    @Override
    public PinsRecord values(String value1, byte[] value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PinsRecord
     */
    public PinsRecord() {
        super(Pins.PINS);
    }

    /**
     * Create a detached, initialised PinsRecord
     */
    public PinsRecord(String memberid, byte[] salt, String hash) {
        super(Pins.PINS);

        setMemberid(memberid);
        setSalt(salt);
        setHash(hash);
    }
}
