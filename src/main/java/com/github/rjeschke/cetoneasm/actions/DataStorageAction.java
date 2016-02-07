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
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;

public class DataStorageAction extends Action
{
    private final boolean            isWordSize;
    private final List<List<Action>> expressions;

    public DataStorageAction(final FileLocation location, final boolean isWordSize, final List<List<Action>> expressions)
    {
        super(location);
        this.isWordSize = isWordSize;
        this.expressions = expressions;
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        for (final List<Action> actions : this.expressions)
        {
            if (actions.size() == 1 && (actions.get(0) instanceof StringStoreAction))
            {
                final byte[] bytes = ((StringStoreAction)actions.get(0)).getBytes();
                for (int i = 0; i < bytes.length; i++)
                {
                    if (this.isWordSize)
                    {
                        assembler.emmitDataWord(bytes[i] & 255);
                    }
                    else
                    {
                        assembler.emmitDataByte(bytes[i] & 255);
                    }
                }
            }
            else
            {
                final int value = (int)assembler.evalExpression(actions) & 65535;
                if (this.isWordSize)
                {
                    assembler.emmitDataWord(value);
                }
                else
                {
                    assembler.emmitDataByte(value);
                }
            }
        }
    }
}
