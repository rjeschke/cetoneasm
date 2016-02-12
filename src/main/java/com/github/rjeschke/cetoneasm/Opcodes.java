/*
 * Copyright (C) 2016 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rjeschke.cetoneasm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Opcodes
{
    public static final Opcode[]                                         BY_CODE        = new Opcode[256];
    public static final HashMap<String, List<Opcode>>                    BY_NAME        = new HashMap<String, List<Opcode>>();
    public static final HashMap<String, HashMap<AddressingMode, Opcode>> BY_NAME_MAPPED = new HashMap<String, HashMap<AddressingMode, Opcode>>();
    //@formatter:off
    public static final int[] CYCLE_COUNT =
    {
      7, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6, -2, -5, 0, 8, 4, 4, 6, 6, 2, -4, 2, 7, -4, -4, 7, 7, 6, 6, 0,
      8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6, -2, -5, 0, 8, 4, 4, 6, 6, 2, -4, 2, 7, -4, -4, 7, 7, 6, 6, 0, 8, 3, 3, 5, 5,
      3, 2, 2, 2, 3, 4, 6, 6, -2, -5, 0, 8, 4, 4, 6, 6, 2, -4, 2, 7, -4, -4, 7, 7, 6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2,
      2, 5, 4, 6, 6, -2, -5, 0, 8, 4, 4, 6, 6, 2, -4, 2, 7, -4, -4, 7, 7, 2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4,
      4, 4, -2, 6, 0, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5, 2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4, -2,
      -5, 0, -5, 4, 4, 4, 4, 2, -4, 2, -4, -4, -4, -4, -4, 2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6, -2, -5, 0, 8, 4,
      4, 6, 6, 2, -4, 2, 7, -4, -4, 7, 7, 2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6, -2, -5, 0, 8, 4, 4, 6,
      6, 2, -4, 2, 7, -4, -4, 7, 7};
    //@formatter:on

    public static class Opcode
    {
        public final String         mnemonic;
        public final int            value;
        public final AddressingMode adressingMode;

        public Opcode(final String mnemonic, final int value, final AddressingMode adressingMode)
        {
            this.mnemonic = mnemonic;
            this.value = value;
            this.adressingMode = adressingMode;
        }

        @Override
        public String toString()
        {
            return String.format("$%02X:%s,%s", this.value, this.mnemonic, this.adressingMode);
        }
    }

    static
    {
        try
        {
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(
                            Main.class.getResourceAsStream("/com/github/rjeschke/cetoneasm/opcodes.txt")));

            final String[] addrIn =
            { "", "rel", "imm", "abs", "abx", "aby", "zp", "zpx", "zpy", "ind", "izy", "izx" };
            final AddressingMode[] modes =
            { AddressingMode.IMPLIED,
            AddressingMode.RELATIVE,
            AddressingMode.IMMEDIATE,
            AddressingMode.ABSOLUTE,
            AddressingMode.ABSOLUTE_X,
            AddressingMode.ABSOLUTE_Y,
            AddressingMode.ZEROPAGE,
            AddressingMode.ZEROPAGE_X,
            AddressingMode.ZEROPAGE_Y,
            AddressingMode.INDIRECT,
            AddressingMode.INDEXED_INDIRECT,
            AddressingMode.INDIRECT_INDEXED };

            final HashMap<String, AddressingMode> modeMap = new HashMap<String, AddressingMode>();
            for (int i = 0; i < addrIn.length; i++)
            {
                modeMap.put(addrIn[i], modes[i]);
            }

            for (String line = r.readLine(); line != null; line = r.readLine())
            {
                if (line.trim().isEmpty())
                {
                    continue;
                }
                final String[] ts = line.trim().split(" ");
                final boolean doNotAdd = ts[0].startsWith("*");
                final int opcode = Integer.parseInt(doNotAdd ? ts[0].substring(1) : ts[0], 16);
                final String mn = ts[1].toUpperCase();
                final AddressingMode mode = ts.length == 2 ? AddressingMode.IMPLIED : modeMap.get(ts[2].toLowerCase());
                if (mode == null || mode == AddressingMode.ILL)
                {
                    throw new IOException("Corrupt opcode initialization table: " + line);
                }
                if (BY_CODE[opcode] != null)
                {
                    throw new IOException("Duplicate opcode definition for $" + Integer.toString(opcode, 16));
                }
                BY_CODE[opcode] = new Opcode(mn, opcode, mode);
                if (!doNotAdd)
                {
                    if (!BY_NAME.containsKey(mn))
                    {
                        BY_NAME.put(mn, new ArrayList<Opcode>());
                    }
                    BY_NAME.get(mn).add(BY_CODE[opcode]);
                }
            }
            for (int i = 0; i < BY_CODE.length; i++)
            {
                if (BY_CODE[i] == null)
                {
                    throw new IOException("Missing opcode definition for $" + Integer.toString(i, 16));
                }
            }

            for (final Entry<String, List<Opcode>> e : BY_NAME.entrySet())
            {
                HashMap<AddressingMode, Opcode> map = BY_NAME_MAPPED.get(e.getKey());
                if (map == null)
                {
                    map = new HashMap<AddressingMode, Opcode>();
                    BY_NAME_MAPPED.put(e.getKey(), map);
                }
                for (final Opcode op : e.getValue())
                {
                    if (map.containsKey(op.adressingMode))
                    {
                        throw new IllegalStateException("Duplicate opcode: " + op);
                    }
                    map.put(op.adressingMode, op);
                }
            }
        }
        catch (final Throwable t)
        {
            throw new RuntimeException("Opcode init failed: " + t.getMessage(), t);
        }
    }
}
