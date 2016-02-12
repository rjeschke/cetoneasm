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

import java.util.List;

import com.github.rjeschke.cetoneasm.Opcodes.Opcode;
import com.github.rjeschke.neetutils.Strings;

public final class U
{
    private final static String CASM_EXT = ".casm64";

    private U()
    {
        // meh!
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static String normalizePath(final String filename)
    {
        final List<String> comps = Strings.split(filename.trim().replace('\\', '/'), '/');

        for (int i = 0; i < comps.size(); i++)
        {
            comps.set(i, comps.get(i).trim());
            if (i > 0 && comps.get(i).isEmpty())
            {
                comps.remove(i--);
            }
            if (i > 1 && comps.get(i).equals(".."))
            {
                comps.remove(i - 1);
                comps.remove(i - 1);
                i -= 2;
            }
        }

        return Strings.join(comps, isWindows() ? "\\" : "/");
    }

    public static String addCasmFileExtension(final String path)
    {
        if (!path.contains("."))
        {
            return path + CASM_EXT;
        }
        return path;
    }

    public static String addPrgFileExtension(final String path)
    {
        if (!path.contains("."))
        {
            return path + ".prg";
        }
        return path;
    }

    public static String replaceExtension(final String filename, final String with)
    {
        final int idx = filename.lastIndexOf('.');
        if (idx < 0)
        {
            return filename + with;
        }
        return filename.substring(0, idx) + with;
    }

    public static int readWord(final byte[] buffer, final int offset)
    {
        return (buffer[offset] & 255) | ((buffer[(offset + 1) & 65535] & 255) << 8);
    }

    public static String disassemble(final byte[] buffer, int pc)
    {
        final int opc = buffer[pc] & 255;
        final Opcode op = Opcodes.BY_CODE[opc];
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X ", opc));

        switch (op.adressingMode)
        {
        default:
            sb.append("      ");
            break;
        case ABSOLUTE:
        case ABSOLUTE_X:
        case ABSOLUTE_Y:
        case INDIRECT:
            sb.append(String.format("%02X %02X ", buffer[pc + 1] & 255, buffer[pc + 2] & 255));
            break;
        case RELATIVE:
        case IMMEDIATE:
        case ZEROPAGE:
        case ZEROPAGE_X:
        case ZEROPAGE_Y:
        case INDEXED_INDIRECT:
        case INDIRECT_INDEXED:
            sb.append(String.format("%02X    ", buffer[pc + 1] & 255));
            break;
        }

        sb.append(op.mnemonic);

        pc++;

        switch (op.adressingMode)
        {
        default:
            break;
        case ABSOLUTE:
            sb.append(String.format(" $%04X", readWord(buffer, pc)));
            pc += 2;
            break;
        case ABSOLUTE_X:
            sb.append(String.format(" $%04X,X", readWord(buffer, pc)));
            pc += 2;
            break;
        case ABSOLUTE_Y:
            sb.append(String.format(" $%04X,Y", readWord(buffer, pc)));
            pc += 2;
            break;
        case INDIRECT:
            sb.append(String.format(" ($%04X)", readWord(buffer, pc)));
            pc += 2;
            break;
        case IMMEDIATE:
            sb.append(String.format(" #$%02X", buffer[pc] & 255));
            pc++;
            break;
        case ZEROPAGE:
            sb.append(String.format(" $%02X", buffer[pc] & 255));
            pc++;
            break;
        case ZEROPAGE_X:
            sb.append(String.format(" $%02X,X", buffer[pc] & 255));
            pc++;
            break;
        case ZEROPAGE_Y:
            sb.append(String.format(" $%02X,Y", buffer[pc] & 255));
            pc++;
            break;
        case INDEXED_INDIRECT:
            sb.append(String.format(" ($%02X),Y", buffer[pc] & 255));
            pc++;
            break;
        case INDIRECT_INDEXED:
            sb.append(String.format(" ($%02X,X)", buffer[pc] & 255));
            pc++;
            break;
        case RELATIVE:
            sb.append(String.format(" $%04X", buffer[pc] + pc + 1));
            pc++;
            break;
        }
        return sb.toString();
    }

    public static int getOpcodePayloadSize(final Opcode opc)
    {
        switch (opc.adressingMode)
        {
        default:
            return 0;
        case IMMEDIATE:
        case ZEROPAGE:
        case ZEROPAGE_X:
        case ZEROPAGE_Y:
        case INDEXED_INDIRECT:
        case INDIRECT_INDEXED:
            return 1;
        case ABSOLUTE:
        case ABSOLUTE_X:
        case ABSOLUTE_Y:
        case INDIRECT:
            return 2;
        }
    }
}
