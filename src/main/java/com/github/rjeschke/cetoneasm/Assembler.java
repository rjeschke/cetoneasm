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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.github.rjeschke.cetoneasm.actions.JumpLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetVariableAction;
import com.github.rjeschke.neetutils.collections.Colls;

public class Assembler
{
    private final HashMap<String, Variable> variables          = new HashMap<String, Variable>();
    private final HashMap<String, Variable> labels             = new HashMap<String, Variable>();
    private final HashMap<Long, Integer>    jumpLabelToIndex   = new HashMap<Long, Integer>();
    private final long[]                    arithStack         = new long[1024];
    private int                             arithSp;
    private Variable                        pcVariable         = null;
    private final ArrayList<CodeContainer>  codeContainers     = new ArrayList<CodeContainer>();
    private int                             passNumber         = 0;
    private long                            jumpId             = -1;
    private boolean                         throwIfUnitialized = false;

    private static String[]                 PASS_NAMES         = Colls.objArray(
                                                                       "Resolve and import include files",
                                                                       "Macro expansion",
                                                                       "Variable and label gathering",
                                                                       "Assembly warm up",
                                                                       "First assembly pass",
                                                                       "Final assembly pass");

    public Assembler()
    {
        this.init();
    }

    private void init()
    {
        this.variables.clear();
        this.variables.put("@", this.pcVariable = new Variable());
        this.arithSp = 0;
        this.jumpId = -1;
    }

    private void startPass(final int index)
    {
        Con.info(" Pass #%d = %s", index + 1, PASS_NAMES[index]);

        this.passNumber = index;
        this.jumpId = -1;

        switch (index)
        {
        case 0: // .INCLUDE
            break;
        case 1: // .CALL
            break;
        case 2: // Gather variables/labels
            this.jumpLabelToIndex.clear();
            break;
        case 3: // Initial compile
            this.codeContainers.clear();
            this.pcVariable.reset();
            this.arithSp = 0;
            this.throwIfUnitialized = false;
            break;
        case 4: // First real compilation
        case 5: // Final compilation
            this.codeContainers.clear();
            this.pcVariable.reset();
            this.arithSp = 0;
            this.throwIfUnitialized = true;
            break;
        }
    }

    private CodeContainer getCurrentCodeContainer()
    {
        return this.codeContainers.get(this.codeContainers.size() - 1);
    }

    public int getPassNumber()
    {
        return this.passNumber;
    }

    public void setJumpId(final long id)
    {
        this.jumpId = id;
    }

    public void emmitByte(final int value) throws AssemblerException
    {
        if (this.codeContainers.isEmpty()
                || !this.getCurrentCodeContainer().isConsecutive(this.getPC())
                || this.getCurrentCodeContainer().isDataContainer())
        {
            this.codeContainers.add(new CodeContainer(this.getPC(), false));
        }
        this.getCurrentCodeContainer().add(value);
        this.incPC();
    }

    public void emmitWord(final int value) throws AssemblerException
    {
        this.emmitByte(value & 255);
        this.emmitByte((value >> 8) & 255);
    }

    public void emmitDataByte(final int value) throws AssemblerException
    {
        if (this.codeContainers.isEmpty()
                || !this.getCurrentCodeContainer().isConsecutive(this.getPC())
                || !this.getCurrentCodeContainer().isDataContainer())
        {
            this.codeContainers.add(new CodeContainer(this.getPC(), true));
        }
        this.getCurrentCodeContainer().add(value);
        this.incPC();
    }

    public void emmitDataWord(final int value) throws AssemblerException
    {
        this.emmitDataByte(value & 255);
        this.emmitDataByte((value >> 8) & 255);
    }

    private void incPC() throws AssemblerException
    {
        this.pcVariable.set((this.getPC() + 1) & 65535);
    }

    public int getPC() throws AssemblerException
    {
        if (!this.pcVariable.isInitialized())
        {
            throw new AssemblerException(null, "PC(@) not set");
        }
        return (int)this.pcVariable.get() & 65535;
    }

    public void setLabelAddress(final String labelName, final int pc) throws AssemblerException
    {
        final Variable var = this.labels.get(labelName);
        if (var == null)
        {
            throw new AssemblerException(null, "Undefined label '" + labelName + "'");
        }
        var.set(pc);
    }

    public Variable getVariable(final String name) throws AssemblerException
    {
        Variable var = this.variables.get(name);
        if (var == null)
        {
            var = this.labels.get(name);
            if (var == null)
            {
                throw new AssemblerException(null, "Undefined variable or label '" + name + "'");
            }
        }
        if (this.throwIfUnitialized && !var.isInitialized())
        {
            throw new AssemblerException(null, "Read access to uninitialized variable '" + name + "'");
        }
        return var;
    }

    public void setVariableValue(final String name, final long value)
    {
        Variable var = this.variables.get(name);
        if (var == null)
        {
            var = new Variable();
            this.variables.put(name, var);
        }
        var.set(value);
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

    public long evalExpression(final List<Action> actions) throws AssemblerException
    {
        for (final Action action : actions)
        {
            action.run(this);
        }
        return this.pop();
    }

    public List<CodeContainer> assemble(final Config config, final List<Action> iActions) throws AssemblerException
    {
        Action currentAction = null;
        try
        {
            final ArrayList<Action> actions = new ArrayList<Action>(iActions);

            // Pass 1: .INCLUDE
            this.startPass(0);

            // Pass 2: .CALL
            this.startPass(1);

            // Pass 3: Gather variables/labels
            this.startPass(2);

            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = actions.get(i);
                if (action instanceof JumpLabelAction)
                {
                    this.jumpLabelToIndex.put(((JumpLabelAction)action).getID(), i);
                    actions.remove(i--);
                }
            }

            for (final Action action : actions)
            {
                currentAction = action;
                if (action instanceof SetVariableAction)
                {
                    final String varName = ((SetVariableAction)action).getVariableName();
                    if (!this.variables.containsKey(varName))
                    {
                        this.variables.put(varName, new Variable());
                    }
                }
                else if (action instanceof SetLabelAction)
                {
                    final String varName = ((SetLabelAction)action).getLabelName();
                    if (!this.labels.containsKey(varName))
                    {
                        this.labels.put(varName, new Variable());
                    }
                }
            }

            final ActionIterable iterable = new ActionIterable(actions, this);

            // Pass 4: Compile
            this.startPass(3);
            for (final Action action : iterable)
            {
                currentAction = action;
                action.run(this);
            }

            // Pass 5: Compile, throw if unitialized
            this.startPass(4);
            for (final Action action : iterable)
            {
                currentAction = action;
                action.run(this);
            }

            // Pass 6: Compile, throw if unitialized, look-ahead settling
            this.startPass(5);
            for (final Action action : iterable)
            {
                currentAction = action;
                action.run(this);
            }

            final ArrayList<CodeContainer> ret = new ArrayList<CodeContainer>(this.codeContainers);
            Collections.sort(ret);
            return ret;
        }
        catch (final AssemblerException ae)
        {
            if (ae.getLocation() == null && currentAction != null)
            {
                throw new AssemblerException(currentAction.getLocation(), ae.getMessage(), ae.getCause());
            }
            throw ae;
        }
    }

    private static class ActionIterable implements Iterable<Action>
    {
        private final List<Action> actions;
        private final Assembler    assembler;

        public ActionIterable(final List<Action> actions, final Assembler assembler)
        {
            this.actions = actions;
            this.assembler = assembler;
        }

        @Override
        public Iterator<Action> iterator()
        {
            return new ActionIterator(this.actions, this.assembler);
        }

        private static class ActionIterator implements Iterator<Action>
        {
            private final List<Action> actions;
            private final Assembler    assembler;
            private int                index = 0;

            public ActionIterator(final List<Action> actions, final Assembler assembler)
            {
                this.actions = actions;
                this.assembler = assembler;
            }

            @Override
            public boolean hasNext()
            {
                final int temp;
                if (this.assembler.jumpId >= 0)
                {
                    temp = this.assembler.jumpLabelToIndex.get(this.assembler.jumpId);
                }
                else
                {
                    temp = this.index;
                }
                return temp < this.actions.size();
            }

            @Override
            public Action next()
            {
                if (this.assembler.jumpId >= 0)
                {
                    this.index = this.assembler.jumpLabelToIndex.get(this.assembler.jumpId);
                    this.assembler.jumpId = -1;
                }
                return this.actions.get(this.index++);
            }

            @Override
            public void remove()
            {
                //
            }
        }
    }
}
