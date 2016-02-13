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
import java.util.HashMap;

import com.github.rjeschke.cetoneasm.Opcodes.Opcode;

public class CodeContainer implements Comparable<CodeContainer>
{
    private final int     startAddress;
    private final byte[]  buffer   = new byte[65536];
    private int           position = 0;
    private final boolean dataContainer;

    public CodeContainer(final int address, final boolean dataContainer)
    {
        this.startAddress = address;
        this.dataContainer = dataContainer;
    }

    public boolean isDataContainer()
    {
        return this.dataContainer;
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
        return this.toString(new HashMap<Integer, String>());
    }

    public String toString(final HashMap<Integer, String> labelMap)
    {
        final StringBuilder sb = new StringBuilder();
        if (this.dataContainer)
        {
            for (int pc = 0; pc < this.position;)
            {
                sb.append(String.format(" %04X ", (pc + this.startAddress) & ~15));
                final int startSkip = (pc + this.startAddress) & 15;
                final int todo = Math.min(this.position - pc, 16 - startSkip);
                for (int n = 0; n < startSkip; n++)
                {
                    sb.append(".. ");
                }
                for (int n = 0; n < todo; n++)
                {
                    sb.append(String.format("%02X ", this.buffer[pc + n] & 255));
                }
                for (int n = 0; n < 16 - todo - startSkip; n++)
                {
                    sb.append(".. ");
                }
                sb.append(" | ");
                for (int n = 0; n < startSkip; n++)
                {
                    sb.append(" ");
                }
                for (int n = 0; n < todo; n++)
                {
                    final int ch = this.buffer[pc + n] & 255;
                    sb.append(ch < 32 || ch > 127 ? '.' : (char)ch);
                }
                sb.append('\n');
                pc += todo;
            }
        }
        else
        {
            final StringBuilder line = new StringBuilder();
            for (int pc = 0; pc < this.position;)
            {
                final String label = labelMap.get(Integer.valueOf(pc + this.startAddress));
                final int opc = this.buffer[pc] & 255;
                final Opcode op = Opcodes.BY_CODE[opc];
                line.setLength(0);
                line.append(String.format(" %04X %02X ", pc + this.startAddress, opc));

                switch (op.adressingMode)
                {
                default:
                    line.append("      ");
                    break;
                case ABSOLUTE:
                case ABSOLUTE_X:
                case ABSOLUTE_Y:
                case INDIRECT:
                    line.append(String.format("%02X %02X ", this.buffer[pc + 1], this.buffer[pc + 2]));
                    break;
                case RELATIVE:
                case IMMEDIATE:
                case ZEROPAGE:
                case ZEROPAGE_X:
                case ZEROPAGE_Y:
                case INDEXED_INDIRECT:
                case INDIRECT_INDEXED:
                    line.append(String.format("%02X    ", this.buffer[pc + 1]));
                    break;
                }

                line.append(op.mnemonic);

                pc++;

                switch (op.adressingMode)
                {
                default:
                    break;
                case ABSOLUTE:
                    line.append(String.format(" $%04X", this.readWord(pc)));
                    pc += 2;
                    break;
                case ABSOLUTE_X:
                    line.append(String.format(" $%04X,X", this.readWord(pc)));
                    pc += 2;
                    break;
                case ABSOLUTE_Y:
                    line.append(String.format(" $%04X,Y", this.readWord(pc)));
                    pc += 2;
                    break;
                case INDIRECT:
                    line.append(String.format(" ($%04X)", this.readWord(pc)));
                    pc += 2;
                    break;
                case IMMEDIATE:
                    line.append(String.format(" #$%02X", this.buffer[pc] & 255));
                    pc++;
                    break;
                case ZEROPAGE:
                    line.append(String.format(" $%02X", this.buffer[pc] & 255));
                    pc++;
                    break;
                case ZEROPAGE_X:
                    line.append(String.format(" $%02X,X", this.buffer[pc] & 255));
                    pc++;
                    break;
                case ZEROPAGE_Y:
                    line.append(String.format(" $%02X,Y", this.buffer[pc] & 255));
                    pc++;
                    break;
                case INDEXED_INDIRECT:
                    line.append(String.format(" ($%02X),Y", this.buffer[pc] & 255));
                    pc++;
                    break;
                case INDIRECT_INDEXED:
                    line.append(String.format(" ($%02X,X)", this.buffer[pc] & 255));
                    pc++;
                    break;
                case RELATIVE:
                    line.append(String.format(" $%04X", this.buffer[pc] + pc + 1 + this.startAddress));
                    pc++;
                    break;
                }

                // Clock cycle analysis

                while (line.length() < 30)
                {
                    line.append(' ');
                }
                line.append("; ");

                final int cycles = Opcodes.CYCLE_COUNT[op.value];

                if (cycles < 0)
                {
                    if (op.adressingMode == AddressingMode.RELATIVE)
                    {
                        final int from = pc + this.startAddress;
                        final int target = this.buffer[pc - 1] + from;
                        if ((from & 0xff00) != (target & 0xff00))
                        {
                            line.append("2/4");
                        }
                        else
                        {
                            line.append("2/3");
                        }
                    }
                    else
                    {
                        line.append(-cycles);
                        line.append('*');
                    }
                }
                else
                {
                    line.append(cycles);
                }

                if (label != null)
                {
                    while (line.length() < 37)
                    {
                        line.append(' ');
                    }
                    line.append(label);
                }

                sb.append(line);
                sb.append('\n');
            }
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
