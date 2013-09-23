/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;

import java.nio.ByteOrder;

/**
 * Class to encapsulate an atom of data. This Intermediate Representation (IR)
 * is language, schema, platform independent.
 * <p/>
 * Processing and optimization could be run over a list of IrNodes to perform various functions
 * <ul>
 *     <li>ordering of fields based on size</li>
 *     <li>padding of fields in order to provide expansion room</li>
 *     <li>computing offsets of individual fields</li>
 *     <li>etc.</li>
 * </ul>
 */
public class IrNode
{
    /** Size not determined */
    public static final int VARIABLE_SIZE = -1;

    /** Offset not computed or set */
    public static final int UNKNOWN_OFFSET = -1;

    private final PrimitiveType primitiveType;
    private final int size;
    private final int offset;
    private final ByteOrder byteOrder;
    private final Metadata metadata;

    /**
     * Construct an {@link IrNode} by providing values for all fields.
     *
     * @param primitiveType representing this node or null.
     * @param size          of the node in bytes.
     * @param offset        within the {@link uk.co.real_logic.sbe.xml.Message}.
     * @param byteOrder     for the encoding.
     * @param metadata      for the {@link uk.co.real_logic.sbe.xml.Message}.
     */
    public IrNode(final PrimitiveType primitiveType,
                  final int size,
                  final int offset,
                  final ByteOrder byteOrder,
                  final Metadata metadata)
    {
        this.primitiveType = primitiveType;
        this.size = size;
        this.offset = offset;
        this.byteOrder = byteOrder;
        this.metadata = metadata;

        if (metadata == null)
        {
            throw new RuntimeException("metadata of IrNode must not be null");
        }
    }

    /**
     * Construct a default {@link IrNode} based on {@link Metadata} with defaults for other fields.
     *
     * @param metadata for this node.
     */
    public IrNode(final Metadata metadata)
    {
        this.primitiveType = null;
        this.size = 0;
        this.offset = 0;
        this.byteOrder = null;
        this.metadata = metadata;

        if (metadata == null)
        {
            throw new RuntimeException("metadata of IrNode must not be null");
        }
    }

    public PrimitiveType getPrimitiveType()
    {
        return primitiveType;
    }

    public int size()
    {
        return size;
    }

    public int getOffset()
    {
        return offset;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    public enum Flag
    {
        MESSAGE_START,
        MESSAGE_END,
        STRUCT_START,
        STRUCT_END,
        FIELD_START,
        FIELD_END,
        GROUP_START,
        GROUP_END,
        NONE
    }

    /** Metadata describing an {@link IrNode} */
    public static class Metadata
    {
        /** Invalid ID value */
        public static final long INVALID_ID = Long.MAX_VALUE;

        private final String name;
        private final long id;
        private final long irId;
        private final Flag flag;
        private final PrimitiveValue minValue;
        private final PrimitiveValue maxValue;
        private final PrimitiveValue nullValue;
        private final PrimitiveValue constValue;
        private final String description;

        /*
         * ENUM_START
         *    ENUM_ENCODING
         *    ENUM_VALUE...
         * ENUM_END
         * SET_START
         *    SET_ENCODING
         *    SET_CHOICE...
         * SET_END
         *
         * irId = generated Id field
         * length field (START_STRUCT or normal) has xRefIrId for <data> field (generateIrId for <data> field and save)
         * VAR_START has xRefIrId to length FIELD_START
         * count field (START_STRUCT or normal) has xRefId for <group> field (generateIrId for <group> field and save)
         * GROUP_START has xRefIrId to count FIELD_START
         *
         * GROUP_START - point to field id that holds count
         * GROUP_END -
         */

        /**
         * Default constructor that is used for START/END nodes.
         *
         * @param name        of the type, field, message, etc.
         * @param id          of the type, field, message, etc.
         * @param irId        of the IrNode.
         * @param flag        representing the flag for the metadata of the IrNode.
         * @param description representing the type, field, message, etc.
         */
        public Metadata(final String name,
                        final long id,
                        final long irId,
                        final Flag flag,
                        final String description)
        {
            this.name = name;
            this.id = id;
            this.irId = irId;
            this.flag = flag;
            this.minValue = null;
            this.maxValue = null;
            this.nullValue = null;
            this.constValue = null;
            this.description = description;
        }

        /**
         * Constructor that is used for {@link uk.co.real_logic.sbe.xml.Presence#REQUIRED} encoding nodes.
         *
         * @param name     of the type.
         * @param minValue of the type or null.
         * @param maxValue of the type or null.
         */
        public Metadata(final String name, final PrimitiveValue minValue, final PrimitiveValue maxValue)
        {
            this.name = name;
            this.id = INVALID_ID;
            this.irId = INVALID_ID;
            this.flag = IrNode.Flag.NONE;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.nullValue = null;
            this.constValue = null;
            this.description = null;
        }

        /**
         * Constructor that is used for {@link uk.co.real_logic.sbe.xml.Presence#OPTIONAL} encoding nodes.
         *
         * @param name       of the type.
         * @param minValue   of the type or null.
         * @param maxValue   of the type or null.
         * @param nullValue  of the type.
         */
        public Metadata(final String name, final PrimitiveValue minValue, final PrimitiveValue maxValue, final PrimitiveValue nullValue)
        {
            this.name = name;
            this.id = INVALID_ID;
            this.irId = INVALID_ID;
            this.flag = IrNode.Flag.NONE;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.nullValue = nullValue;
            this.constValue = null;
            this.description = null;
        }

        /**
         * Constructor that is used for {@link uk.co.real_logic.sbe.xml.Presence#CONSTANT} encoding nodes.
         *
         * @param name       of the type.
         * @param constValue of the type.
         */
        public Metadata(final String name, final PrimitiveValue constValue)
        {
            this.name = name;
            this.id = INVALID_ID;
            this.irId = INVALID_ID;
            this.flag = IrNode.Flag.NONE;
            this.minValue = null;
            this.maxValue = null;
            this.nullValue = null;
            this.constValue = constValue;
            this.description = null;
        }

        public String getName()
        {
            return name;
        }

        public long getId()
        {
            return id;
        }

        public Flag getFlag()
        {
            return flag;
        }
    }
}