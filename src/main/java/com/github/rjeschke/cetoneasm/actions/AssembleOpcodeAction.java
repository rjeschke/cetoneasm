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

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final WidthType widthtype,
            final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    public AssembleOpcodeAction(final FileLocation location, final Opcode opc, final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run(final Runtime runtime) throws AssemblerException
    {
        // TODO Auto-generated method stub

    }

}
