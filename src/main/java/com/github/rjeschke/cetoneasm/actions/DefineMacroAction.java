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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.CounterState;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.MetaAction;

public class DefineMacroAction extends MetaAction
{
    private final String           name;
    private final List<String>     arguments;
    private final List<Action>     actions;
    private final HashSet<Integer> jumpIds    = new HashSet<Integer>();
    private final HashSet<Integer> counterIds = new HashSet<Integer>();

    public DefineMacroAction(final FileLocation location, final String name, final List<String> arguments,
            final List<Action> actions)
    {
        super(location);
        this.name = name;
        this.arguments = arguments;
        this.actions = actions;

        this.prepare();
    }

    private static void addDefines(final Action a, final HashSet<String> def)
    {
        if (a instanceof SetVariableAction)
        {
            final String varName = ((SetVariableAction)a).getVariableName();
            if (!"@".equals(varName))
            {
                def.add(varName);
            }
        }
        else if (a instanceof SetLabelAction)
        {
            def.add(((SetLabelAction)a).getLabelName());
        }

        for (final Action b : a.getNestedActions())
        {
            addDefines(b, def);
        }
    }

    private static void mangle(final Action a, final HashSet<String> def)
    {
        if (a instanceof SetVariableAction)
        {
            final SetVariableAction sva = (SetVariableAction)a;
            if (def.contains(sva.getVariableName()))
            {
                sva.makeLocal();
            }
        }
        else if (a instanceof SetLabelAction)
        {
            final SetLabelAction sla = (SetLabelAction)a;
            if (def.contains(sla.getLabelName()))
            {
                sla.makeLocal();
            }
        }
        else if (a instanceof GetVariableAction)
        {
            final GetVariableAction gva = (GetVariableAction)a;
            if (def.contains(gva.getVariableName()))
            {
                gva.makeLocal();
            }
        }

        for (final Action b : a.getNestedActions())
        {
            mangle(b, def);
        }
    }

    private void prepare()
    {
        final HashSet<String> def = new HashSet<String>(this.arguments);
        for (final Action a : this.actions)
        {
            addDefines(a, def);
        }

        for (final Action a : this.actions)
        {
            mangle(a, def);

            if (a instanceof JumpIdAction)
            {
                this.jumpIds.add(((JumpIdAction)a).getID());
            }
            else if (a instanceof CounterSetAction)
            {
                this.counterIds.add(((CounterSetAction)a).getID());
            }
        }

        for (int i = 0; i < this.arguments.size(); i++)
        {
            this.arguments.set(i, "__" + this.arguments.get(i));
        }
    }

    public List<Action> getMangledActions(final List<List<Action>> args, final long id, final FileLocation caller)
            throws AssemblerException
    {
        final ArrayList<Action> ret = new ArrayList<Action>();

        if (args.size() != this.arguments.size())
        {
            throw new AssemblerException(caller, "Argument count mismatch, expected " + this.arguments.size()
                    + ", got " + args.size());
        }

        ret.add(new SetLabelAction(caller, this.name + "$" + id, true));

        // Create args assignment
        for (int i = 0; i < this.arguments.size(); i++)
        {
            ret.addAll(args.get(i));
            ret.add(new SetVariableAction(this.getLocation(), this.arguments.get(i)));
        }

        final HashMap<Integer, Integer> cidMap = new HashMap<Integer, Integer>();
        final HashMap<Integer, Integer> jidMap = new HashMap<Integer, Integer>();

        for (final Integer i : this.counterIds)
        {
            cidMap.put(i, CounterState.get().newId());
        }

        for (final Integer i : this.jumpIds)
        {
            jidMap.put(i, CounterState.get().newJumpId());
        }

        final ArrayList<Action> as = new ArrayList<Action>(this.actions);

        for (int i = 0; i < as.size(); i++)
        {
            final Action a = as.get(i);
            if (a instanceof JumpIdAction)
            {
                final JumpIdAction b = (JumpIdAction)a;
                as.set(i, b.mangle(jidMap.get(b.getID()).intValue()));
            }
            else if (a instanceof JumpToIdAction)
            {
                final JumpToIdAction b = (JumpToIdAction)a;
                as.set(i, b.mangle(jidMap.get(b.getID()).intValue()));
            }
            else if (a instanceof ConditionalJumpAction)
            {
                final ConditionalJumpAction b = (ConditionalJumpAction)a;
                as.set(i, b.mangle(jidMap.get(b.getID()).intValue()));
            }
            else if (a instanceof CounterSetAction)
            {
                final CounterSetAction b = (CounterSetAction)a;
                as.set(i, b.mangle(cidMap.get(b.getID()).intValue()));
            }
            else if (a instanceof CounterCompareAction)
            {
                final CounterCompareAction b = (CounterCompareAction)a;
                as.set(i, b.mangle(cidMap.get(b.getCID()).intValue(), jidMap.get(b.getJID()).intValue()));
            }
            else if (a instanceof CounterDecrementAction)
            {
                final CounterDecrementAction b = (CounterDecrementAction)a;
                as.set(i, b.mangle(cidMap.get(b.getCID()).intValue(), jidMap.get(b.getJID()).intValue()));
            }
        }

        ret.addAll(as);

        return ret;
    }

    public String getName()
    {
        return this.name;
    }
}
