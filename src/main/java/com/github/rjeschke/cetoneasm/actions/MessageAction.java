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
import java.util.IllegalFormatException;
import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.Con;
import com.github.rjeschke.cetoneasm.FileLocation;

public class MessageAction extends Action
{
    private final Type       type;
    final List<List<Action>> arguments;
    final List<Action>       flattened;

    public MessageAction(final FileLocation location, final Type type, final List<List<Action>> arguments)
    {
        super(location);
        this.type = type;
        this.arguments = arguments;
        this.flattened = new ArrayList<Action>();
        for (final List<Action> actions : arguments)
        {
            this.flattened.addAll(actions);
        }
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        if (assembler.isFinalPass())
        {
            String output = "";
            if (this.type.isFormatted())
            {
                final String fmt = ((StoreStringAction)this.arguments.get(0).get(0)).getValue();
                final Object[] args = new Object[this.arguments.size() - 1];
                for (int i = 1; i < this.arguments.size(); i++)
                {
                    final List<Action> actions = this.arguments.get(i);
                    if (actions.size() == 1 && (actions.get(0) instanceof StoreStringAction))
                    {
                        args[i - 1] = ((StoreStringAction)actions.get(0)).getValue();
                    }
                    else
                    {
                        args[i - 1] = Long.valueOf(assembler.evalExpression(actions));
                    }
                }
                try
                {
                    output = String.format(fmt, args);
                }
                catch (final IllegalFormatException ife)
                {
                    this.error("Format error: " + ife.getMessage());
                }
            }
            else
            {
                final StringBuilder sb = new StringBuilder();

                for (final List<Action> actions : this.arguments)
                {
                    if (actions.size() == 1 && (actions.get(0) instanceof StoreStringAction))
                    {
                        sb.append(((StoreStringAction)actions.get(0)).getValue());
                    }
                    else
                    {
                        sb.append(assembler.evalExpression(actions));
                    }
                }

                output = sb.toString();
            }

            switch (this.type)
            {
            case INFO:
            case INFOF:
                Con.info(this.getLocation() + ": " + output);
                break;
            case WARN:
            case WARNF:
                Con.warn(this.getLocation() + ": " + output);
                break;
            case ERROR:
            case ERRORF:
                this.error(output);
                break;
            }
        }
    }

    @Override
    public List<Action> getNestedActions()
    {
        return this.flattened;
    }

    public enum Type
    {
        INFO(false), INFOF(true),
        WARN(false), WARNF(true),
        ERROR(false), ERRORF(true);

        private final boolean isFormatted;

        private Type(final boolean isFormatted)
        {
            this.isFormatted = isFormatted;
        }

        public boolean isFormatted()
        {
            return this.isFormatted;
        }
    }
}
