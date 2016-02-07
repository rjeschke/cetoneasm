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

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Runtime;
import com.github.rjeschke.cetoneasm.UnaryOperator;

public class UnaryOperatorAction extends Action
{
    private final UnaryOperator operator;

    public UnaryOperatorAction(final FileLocation location, final UnaryOperator operator)
    {
        super(location);
        this.operator = operator;
    }

    @Override
    public void run(final Runtime runtime) throws AssemblerException
    {
        final long a = runtime.pop();
        long c = 0;
        switch (this.operator)
        {
        case HIGH:
            c = (a >> 8) & 255;
            break;
        case LOW:
            c = a & 255;
            break;
        case NOT:
            c = a == 0 ? 1 : 0;
            break;
        case NEG:
            c = ~a;
            break;
        case MINUS:
            c = -a;
            break;
        }
        runtime.push(c);
    }

    @Override
    public String toString()
    {
        return "unary:" + this.operator;
    }
}
