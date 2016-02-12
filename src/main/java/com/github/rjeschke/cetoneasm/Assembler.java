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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.github.rjeschke.cetoneasm.actions.CallMacroAction;
import com.github.rjeschke.cetoneasm.actions.CounterSetAction;
import com.github.rjeschke.cetoneasm.actions.DefineMacroAction;
import com.github.rjeschke.cetoneasm.actions.JumpIdAction;
import com.github.rjeschke.cetoneasm.actions.JumpToIdAction;
import com.github.rjeschke.cetoneasm.actions.MetaGotoAction;
import com.github.rjeschke.cetoneasm.actions.MetaLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetVariableAction;
import com.github.rjeschke.neetutils.collections.Colls;

public class Assembler
{
    private final static int                         FINAL_PASS         = 5;
    private final HashMap<String, Variable>          variables          = new HashMap<String, Variable>();
    private final HashMap<String, Variable>          labels             = new HashMap<String, Variable>();
    private final HashMap<String, DefineMacroAction> definedMacros      = new HashMap<String, DefineMacroAction>();
    private int[]                                    jumpTable;
    private int[]                                    jumpCount;
    private int[]                                    counterTable;
    private final long[]                             arithStack         = new long[1024];
    private int                                      arithSp;
    private Variable                                 pcVariable         = null;
    private final ArrayList<CodeContainer>           codeContainers     = new ArrayList<CodeContainer>();
    private int                                      passNumber         = 0;
    private int                                      jumpId             = -1;
    private boolean                                  throwIfUnitialized = false;
    private String                                   parentLabel        = null;
    private final Config                             config;

    private static String[]                          PASS_NAMES         = Colls.objArray(
                                                                                "Resolve and import include files",
                                                                                "Macro expansion",
                                                                                "Variable and label gathering",
                                                                                "Assembly warm up",
                                                                                "First assembly pass",
                                                                                "Final assembly pass");

    public Assembler(final Config config)
    {
        this.init();
        this.config = config;
    }

    public void init()
    {
        this.variables.clear();
        this.labels.clear();
        this.jumpTable = null;
        this.jumpCount = null;
        this.counterTable = null;
        this.codeContainers.clear();
        this.definedMacros.clear();
        this.variables.put("@", this.pcVariable = new Variable());
        this.arithSp = 0;
        this.jumpId = -1;
        this.parentLabel = null;
    }

    private void startPass(final int index)
    {
        Con.info(" Pass #%d = %s", index + 1, PASS_NAMES[index]);

        this.passNumber = index;
        this.jumpId = -1;
        this.parentLabel = null;

        switch (index)
        {
        case 0: // .INCLUDE
            break;
        case 1: // .MACRO, .CALL
            break;
        case 2: // Gather variables/labels
            break;
        case 3: // Initial compile
            Arrays.fill(this.jumpCount, 0);
            this.codeContainers.clear();
            this.pcVariable.reset();
            this.arithSp = 0;
            this.throwIfUnitialized = false;
            break;
        case 4: // First real compilation
            Arrays.fill(this.jumpCount, 0);
            //$FALL-THROUGH$
        case 5: // Final compilation
            Arrays.fill(this.jumpCount, 0);
            this.codeContainers.clear();
            this.pcVariable.reset();
            this.arithSp = 0;
            this.throwIfUnitialized = true;
            break;
        }
    }

    public String resolveFilename(final String filename)
    {
        return filename;
    }

    public Config getConfig()
    {
        return this.config;
    }

    public boolean isFinalPass()
    {
        return this.passNumber == FINAL_PASS;
    }

    private CodeContainer getCurrentCodeContainer()
    {
        return this.codeContainers.get(this.codeContainers.size() - 1);
    }

    public int getPassNumber()
    {
        return this.passNumber;
    }

    public void setJump(final int id)
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

    public void setCounter(final int id, final int value)
    {
        this.counterTable[id] = value;
    }

    public int getCounter(final int id)
    {
        return this.counterTable[id];
    }

    private void incPC() throws AssemblerException
    {
        final int old = this.getPC();
        this.pcVariable.set((this.getPC() + 1) & 65535);
        if (old > this.getPC())
        {
            throw new AssemblerException(null, "@ wrapped from $FFFF->$0000");
        }
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
        Variable var = null;

        if (labelName.startsWith("_"))
        {
            if (this.parentLabel == null)
            {
                throw new AssemblerException(null, "Local label '" + labelName + "'without parent");
            }
            var = this.labels.get(this.parentLabel + "$$" + labelName);
        }
        else
        {
            var = this.labels.get(labelName);
        }

        if (var == null)
        {
            throw new AssemblerException(null, "Undefined label '" + labelName + "'");
        }
        if (!labelName.contains("$$") && !labelName.startsWith("_"))
        {
            this.parentLabel = labelName;
        }
        var.set(pc);
    }

    public Variable getVariable(final String name) throws AssemblerException
    {
        Variable var = null;
        if (name.startsWith("_"))
        {
            if (this.parentLabel == null)
            {
                throw new AssemblerException(null, "Local variable '" + name + "'without parent");
            }
            final String mangled = this.parentLabel + "$$" + name;
            var = this.variables.get(mangled);
            if (var == null)
            {
                var = this.labels.get(mangled);
            }
        }
        else
        {
            var = this.variables.get(name);
            if (var == null)
            {
                var = this.labels.get(name);
            }
        }
        if (var == null)
        {
            throw new AssemblerException(null, "Undefined variable or label '" + name + "'");
        }
        if (this.throwIfUnitialized && !var.isInitialized())
        {
            throw new AssemblerException(null, "Read access to uninitialized variable '" + name + "'");
        }
        return var;
    }

    public void setVariableValue(final String name, final long value) throws AssemblerException
    {
        Variable var;
        if (name.startsWith("_"))
        {
            if (this.parentLabel == null)
            {
                throw new AssemblerException(null, "Local variable '" + name + "'without parent");
            }
            var = this.variables.get(this.parentLabel + "$$" + name);
        }
        else
        {
            var = this.variables.get(name);
        }
        if (var == null)
        {
            throw new AssemblerException(null, "Undefined variable '" + name + "'");
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

            // ////////////////////////////////////////////////////////////////
            // Pass 1: .INCLUDE
            this.startPass(0);

            // ////////////////////////////////////////////////////////////////
            // Pass 2: :MACRO, .CALL
            this.startPass(1);
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof DefineMacroAction)
                {
                    final DefineMacroAction dma = (DefineMacroAction)action;
                    if (this.definedMacros.containsKey(dma.getName()))
                    {
                        throw new AssemblerException(action.getLocation(), "Duplicate macro definition for '"
                                + dma.getName() + "'");
                    }
                    this.definedMacros.put(dma.getName(), dma);
                    actions.remove(i--);
                }
            }
            Con.info("  %d macro definition(s)", this.definedMacros.size());
            long id = 0;
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof CallMacroAction)
                {
                    final CallMacroAction cma = (CallMacroAction)action;
                    final DefineMacroAction dma = this.definedMacros.get(cma.getName());
                    if (dma == null)
                    {
                        throw new AssemblerException(action.getLocation(), "Unknown macro '" + cma.getName() + "'");
                    }
                    final List<Action> expanded = dma.getMangledActions(cma.getArguments(), ++id, action.getLocation());
                    actions.remove(i);
                    actions.addAll(i, expanded);
                    i += expanded.size() - 1;
                }
            }

            // ////////////////////////////////////////////////////////////////
            // Pass 3: Gather variables/labels
            this.startPass(2);

            // Transfrom MetaLabelAction to JumpIdAction
            final HashMap<String, Integer> metaJumps = new HashMap<String, Integer>();
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof MetaLabelAction)
                {
                    final String name = ((MetaLabelAction)action).getName();
                    final JumpIdAction jump = new JumpIdAction(action.getLocation());
                    metaJumps.put(name, jump.getID());
                    actions.set(i, jump);
                }
            }
            // Find maximum jump/counter IDs
            int maxJumpId = 0;
            int maxCounterId = 0;
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof JumpIdAction)
                {
                    maxJumpId = Math.max(maxJumpId, ((JumpIdAction)action).getID());
                }
                else if (action instanceof CounterSetAction)
                {
                    maxCounterId = Math.max(maxCounterId, ((CounterSetAction)action).getID());
                }
            }
            this.jumpTable = new int[maxJumpId + 1];
            this.jumpCount = new int[this.jumpTable.length];
            this.counterTable = new int[maxCounterId + 1];
            // Populate jumpTable
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof JumpIdAction)
                {
                    this.jumpTable[((JumpIdAction)action).getID()] = i;
                    actions.remove(i--);
                }
            }
            // Map .GOTO
            for (int i = 0; i < actions.size(); i++)
            {
                final Action action = currentAction = actions.get(i);
                if (action instanceof MetaGotoAction)
                {
                    final String name = ((MetaGotoAction)action).getName();
                    if (!metaJumps.containsKey(name))
                    {
                        throw new AssemblerException(action.getLocation(), "Unknown .LABEL '" + name + "'");
                    }
                    actions.set(i, new JumpToIdAction(action.getLocation(), metaJumps.get(name).intValue()));
                }
            }
            // Gather all declared variables and labels
            String lastLabel = null;
            for (final Action action : actions)
            {
                currentAction = action;
                if (action instanceof SetVariableAction)
                {
                    String varName = ((SetVariableAction)action).getVariableName();
                    if (varName.startsWith("_"))
                    {
                        if (lastLabel == null)
                        {
                            throw new AssemblerException(action.getLocation(),
                                    "Local label defined without parent label");
                        }
                        varName = lastLabel + "$$" + varName;
                    }
                    if (this.labels.containsKey(varName))
                    {
                        throw new AssemblerException(action.getLocation(),
                                "Conflicting variable name '" + varName + "' (defined as label already)");
                    }
                    if (!this.variables.containsKey(varName))
                    {
                        this.variables.put(varName, new Variable());
                    }
                }
                else if (action instanceof SetLabelAction)
                {
                    String labelName = ((SetLabelAction)action).getLabelName();

                    if (labelName.startsWith("_"))
                    {
                        if (lastLabel == null)
                        {
                            throw new AssemblerException(action.getLocation(),
                                    "Local label defined without parent label");
                        }
                        labelName = lastLabel + "$$" + labelName;
                    }
                    else
                    {
                        lastLabel = labelName;
                    }

                    if (this.variables.containsKey(labelName))
                    {
                        throw new AssemblerException(action.getLocation(),
                                "Conflicting label name '" + labelName + "' (defined as variable already)");
                    }
                    if (this.labels.containsKey(labelName))
                    {
                        throw new AssemblerException(action.getLocation(), "Duplicate label '" + labelName + "'");
                    }
                    this.labels.put(labelName, new Variable());
                }
            }
            Con.info("  %d variable(s), %d label(s), %d .LABEL(s)", this.variables.size() - 1, this.labels.size(),
                    metaJumps.size());

            // ////////////////////////////////////////////////////////////////
            // Pass 4-6: Compile
            final ActionIterable iterable = new ActionIterable(actions, this);
            for (int pnr = 3; pnr < 6; pnr++)
            {
                this.startPass(pnr);
                for (final Action action : iterable)
                {
                    currentAction = action;
                    action.run(this);
                }
            }

            // ////////////////////////////////////////////////////////////////
            // Finished
            final ArrayList<CodeContainer> ret = new ArrayList<CodeContainer>(this.codeContainers);
            Collections.sort(ret);
            return ret;
        }
        catch (final AssemblerException ae)
        {
            if (ae.getLocation() == null && currentAction != null)
            {
                throw new AssemblerException(currentAction.getLocation(), ae.getMessage(),
                        ae.getCause() != null ? ae.getCause() : ae);
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
                    temp = this.assembler.jumpTable[this.assembler.jumpId];
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
                    this.index = this.assembler.jumpTable[this.assembler.jumpId];
                    if (++this.assembler.jumpCount[this.assembler.jumpId] > (1 << 24))
                    {
                        //
                    }
                    this.assembler.jumpId = -1;
                }
                return this.actions.get(this.index++);
            }

            @Override
            public void remove()
            {
                throw new IllegalStateException("ActionIterator does not support remove()");
            }
        }
    }
}
