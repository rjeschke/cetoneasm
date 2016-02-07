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

import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Opcodes.Opcode;
import com.github.rjeschke.cetoneasm.Runtime;

public class AssembleOpcodeAction extends Action
{
    public enum WidthType
    {
        ABSOLUTE,
        ABSOLUTE_X,
        ABSOLUTE_Y
    }

    private final String       mnemonic;
    private final Opcode       opcode;
    private final List<Action> expression;
    private final WidthType    widthType;

    private AssembleOpcodeAction(final FileLocation location, final String mnemonic, final Opcode opcode,
            final WidthType widthtype, final List<Action> expression)
    {
        super(location);
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.widthType = widthtype;
        this.expression = expression;
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final List<Action> expression)
    {
        this(location, opcode, null, null, expression);
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final WidthType widthtype,
            final List<Action> expression)
    {
        this(location, opcode, null, widthtype, expression);
    }

    public AssembleOpcodeAction(final FileLocation location, final Opcode opcode, final List<Action> expression)
    {
        this(location, null, opcode, null, expression);
    }

    @Override
    public void run(final Runtime runtime) throws AssemblerException
    {
        // TODO Auto-generated method stub
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
        sb.append(';');
        sb.append(this.expression);
        return sb.toString();
    }
}
