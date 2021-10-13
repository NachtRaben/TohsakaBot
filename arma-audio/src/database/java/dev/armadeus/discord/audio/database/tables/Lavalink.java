/*
 * This file is generated by jOOQ.
 */
package dev.armadeus.discord.audio.database.tables;

import dev.armadeus.discord.audio.database.Keys;
import dev.armadeus.discord.audio.database.Public;
import dev.armadeus.discord.audio.database.tables.records.LavalinkRecord;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Lavalink extends TableImpl<LavalinkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.lavalink</code>
     */
    public static final Lavalink LAVALINK = new Lavalink();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LavalinkRecord> getRecordType() {
        return LavalinkRecord.class;
    }

    /**
     * The column <code>public.lavalink.id</code>.
     */
    public final TableField<LavalinkRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.lavalink.uri</code>.
     */
    public final TableField<LavalinkRecord, String> URI = createField(DSL.name("uri"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.lavalink.password</code>.
     */
    public final TableField<LavalinkRecord, String> PASSWORD = createField(DSL.name("password"), SQLDataType.CLOB.nullable(false), this, "");

    private Lavalink(Name alias, Table<LavalinkRecord> aliased) {
        this(alias, aliased, null);
    }

    private Lavalink(Name alias, Table<LavalinkRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.lavalink</code> table reference
     */
    public Lavalink(String alias) {
        this(DSL.name(alias), LAVALINK);
    }

    /**
     * Create an aliased <code>public.lavalink</code> table reference
     */
    public Lavalink(Name alias) {
        this(alias, LAVALINK);
    }

    /**
     * Create a <code>public.lavalink</code> table reference
     */
    public Lavalink() {
        this(DSL.name("lavalink"), null);
    }

    public <O extends Record> Lavalink( Table<O> child, ForeignKey<O, LavalinkRecord> key) {
        super(child, key, LAVALINK);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<LavalinkRecord> getPrimaryKey() {
        return Keys.LAVALINK_NODES_PKEY;
    }

    @Override
    public List<UniqueKey<LavalinkRecord>> getKeys() {
        return Arrays.<UniqueKey<LavalinkRecord>>asList(Keys.LAVALINK_NODES_PKEY);
    }

    @Override
    public Lavalink as(String alias) {
        return new Lavalink(DSL.name(alias), this);
    }

    @Override
    public Lavalink as(Name alias) {
        return new Lavalink(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Lavalink rename(String name) {
        return new Lavalink(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Lavalink rename(Name name) {
        return new Lavalink(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
