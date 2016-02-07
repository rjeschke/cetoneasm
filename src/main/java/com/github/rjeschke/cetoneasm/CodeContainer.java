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

import java.io.ByteArrayOutputStream;

import com.github.rjeschke.cetoneasm.Opcodes.Opcode;

public class CodeContainer implements Comparable<CodeContainer>
{
    private final int    startAddress;
    private final byte[] buffer   = new byte[65536];
    private int          position = 0;

    public CodeContainer(final int address)
    {
        this.startAddress = address;
    }

    public int getStartAddress()
    {
        return this.startAddress;
    }

    public int getSize()
    {
        return this.position;
    }

    public int getEndAddress()
    {
        return this.startAddress + this.position;
    }

    public boolean isConsecutive(final int pc)
    {
        return this.position + this.startAddress == pc;
    }

    public void add(final int value) throws AssemblerException
    {
        if (this.position + this.startAddress >= this.buffer.length)
        {
            throw new AssemblerException(null, "Code container overflow");
        }

        this.buffer[this.position++] = (byte)value;
    }

    private int readWord(final int pc)
    {
        return (this.buffer[pc] & 255) | ((this.buffer[pc + 1] & 255) << 8);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (int pc = 0; pc < this.position;)
        {
            final int opc = this.buffer[pc] & 255;
            final Opcode op = Opcodes.BY_CODE[opc];
            sb.append(String.format("%04X %02X ", pc + this.startAddress, opc));

            switch (op.adressingMode)
            {
            default:
                sb.append("      ");
                break;
            case ABSOLUTE:
            case ABSOLUTE_X:
            case ABSOLUTE_Y:
            case INDIRECT:
                sb.append(String.format("%02X %02X ", this.buffer[pc + 1], this.buffer[pc + 2]));
                break;
            case RELATIVE:
            case IMMEDIATE:
            case ZEROPAGE:
            case ZEROPAGE_X:
            case ZEROPAGE_Y:
            case INDEXED_INDIRECT:
            case INDIRECT_INDEXED:
                sb.append(String.format("%02X    ", this.buffer[pc + 1]));
                break;
            }

            sb.append(op.mnemonic);

            pc++;

            switch (op.adressingMode)
            {
            default:
                break;
            case ABSOLUTE:
                sb.append(String.format(" $%04X", this.readWord(pc)));
                pc += 2;
                break;
            case ABSOLUTE_X:
                sb.append(String.format(" $%04X,X", this.readWord(pc)));
                pc += 2;
                break;
            case ABSOLUTE_Y:
                sb.append(String.format(" $%04X,Y", this.readWord(pc)));
                pc += 2;
                break;
            case INDIRECT:
                sb.append(String.format(" ($%04X)", this.readWord(pc)));
                pc += 2;
                break;
            case IMMEDIATE:
                sb.append(String.format(" #$%02X", this.buffer[pc] & 255));
                pc++;
                break;
            case ZEROPAGE:
                sb.append(String.format(" $%02X", this.buffer[pc] & 255));
                pc++;
                break;
            case ZEROPAGE_X:
                sb.append(String.format(" $%02X,X", this.buffer[pc] & 255));
                pc++;
                break;
            case ZEROPAGE_Y:
                sb.append(String.format(" $%02X,Y", this.buffer[pc] & 255));
                pc++;
                break;
            case INDEXED_INDIRECT:
                sb.append(String.format(" ($%02X),Y", this.buffer[pc] & 255));
                pc++;
                break;
            case INDIRECT_INDEXED:
                sb.append(String.format(" ($%02X,X)", this.buffer[pc] & 255));
                pc++;
                break;
            case RELATIVE:
                sb.append(String.format(" $%04X", this.buffer[pc] + pc + 1 + this.startAddress));
                pc++;
                break;
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    public void writeTo(final ByteArrayOutputStream out)
    {
        out.write(this.buffer, 0, this.position);
    }

    @Override
    public int compareTo(final CodeContainer o)
    {
        return Integer.compare(this.startAddress, o.startAddress);
    }
}
