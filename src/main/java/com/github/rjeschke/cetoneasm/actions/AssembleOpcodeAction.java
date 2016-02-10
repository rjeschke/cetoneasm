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

package com.github.rjeschke.cetoneasm.actions;

import java.util.HashMap;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AddressingMode;
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Opcodes;
import com.github.rjeschke.cetoneasm.Opcodes.Opcode;

public class AssembleOpcodeAction extends Action
{
    public enum WidthType
    {
        ABSOLUTE,
        ABSOLUTE_X,
        ABSOLUTE_Y
    }

    private final String    mnemonic;
    private final Opcode    opcode;
    private final WidthType widthType;

    private AssembleOpcodeAction(final FileLocation location, final String mnemonic, final Opcode opcode,
            final WidthType widthtype)
    {
        super(location);
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.widthType = widthtype;
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final WidthType widthtype)
    {
        this(location, opcode, null, widthtype);
    }

    public AssembleOpcodeAction(final FileLocation location, final Opcode opcode)
    {
        this(location, null, opcode, null);
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        final int address = (this.opcode != null && this.opcode.adressingMode != AddressingMode.IMPLIED)
                | this.widthType != null
                ? (int)assembler.pop() & 0xffff : 0;
        if (this.opcode != null)
        {
            assembler.emmitByte(this.opcode.value);

            switch (this.opcode.adressingMode)
            {
            case IMPLIED:
                break;
            case ABSOLUTE:
            case ABSOLUTE_X:
            case ABSOLUTE_Y:
            case INDIRECT:
                assembler.emmitWord(address);
                break;
            case IMMEDIATE:
            case ZEROPAGE:
            case ZEROPAGE_X:
            case ZEROPAGE_Y:
            case INDEXED_INDIRECT:
            case INDIRECT_INDEXED:
                assembler.emmitByte(address);
                break;
            case RELATIVE:
            {
                final int disp = assembler.getPassNumber() > 3 ? address - (assembler.getPC() + 1) : 0;
                if (disp < -128 || disp > 127)
                {
                    this.error("Branch out of reach: " + disp);
                }
                assembler.emmitByte(disp);
                break;
            }
            case ILL:
                this.error("Internal error, encountered illegal addressing mode");
            }
        }
        else
        {
            final boolean small = address < 256;
            final HashMap<AddressingMode, Opcode> map = Opcodes.BY_NAME_MAPPED.get(this.mnemonic);

            switch (this.widthType)
            {
            case ABSOLUTE:
                assembler.emmitByte(map.get(small ? AddressingMode.ZEROPAGE : AddressingMode.ABSOLUTE).value);
                break;
            case ABSOLUTE_X:
                assembler.emmitByte(map.get(small ? AddressingMode.ZEROPAGE_X : AddressingMode.ABSOLUTE_X).value);
                break;
            case ABSOLUTE_Y:
                assembler.emmitByte(map.get(small ? AddressingMode.ZEROPAGE_Y : AddressingMode.ABSOLUTE_Y).value);
                break;
            }
            if (small)
            {
                assembler.emmitByte(address);
            }
            else
            {
                assembler.emmitWord(address);
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        if (this.opcode != null)
        {
            sb.append(this.opcode);
        }
        else
        {
            sb.append(this.mnemonic);
            if (this.widthType != null)
            {
                sb.append(',');
                sb.append(this.widthType);
            }
        }
        return sb.toString();
    }
}
