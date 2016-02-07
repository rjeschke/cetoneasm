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

import java.util.HashMap;
import java.util.List;

public class Runtime
{
    private final HashMap<String, Variable> variables  = new HashMap<String, Variable>();
    private final long[]                    arithStack = new long[1024];
    private int                             arithSp;
    private Variable                        pcVariable = null;

    public Runtime()
    {
        this.init();
    }

    private void init()
    {
        this.variables.clear();
        this.variables.put("@", this.pcVariable = new Variable());
        this.arithSp = 0;
    }

    public int getPC() throws AssemblerException
    {
        if (!this.pcVariable.initialized)
        {
            throw new AssemblerException(null, "PC(@) not set");
        }
        return (int)this.pcVariable.value & 65535;
    }

    public Variable getVariable(final String name) throws AssemblerException
    {
        final Variable var = this.variables.get(name);
        if (var == null)
        {
            throw new AssemblerException(null, "Undefined variable '" + name + "'");
        }
        return var;
    }

    public Variable getOrCreateVar(final String name)
    {
        Variable var = this.variables.get(name);
        if (var == null)
        {
            var = new Variable();
            this.variables.put(name, var);
        }
        return var;
    }

    public long pop() throws AssemblerException
    {
        if (this.arithSp == 0)
        {
            throw new AssemblerException(null, "Arithmetic stack underrun");
        }
        return this.arithStack[--this.arithSp];
    }

    public void push(final long value) throws AssemblerException
    {
        if (this.arithSp == this.arithStack.length)
        {
            throw new AssemblerException(null, "Arithmetic stack overflow");
        }
        this.arithStack[this.arithSp++] = value;
    }

    public void run(final List<Action> actions)
    {
        // 1. pass(es) .INCLUDE expansion
        // 2. pass: dry run for variable initialization
        // 3. pass: real run, uninitialized variables throw

    }
}
