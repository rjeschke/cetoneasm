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
import com.github.rjeschke.cetoneasm.BinaryOperator;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Assembler;

public class BinaryOperatorAction extends Action
{
    private final BinaryOperator operator;

    public BinaryOperatorAction(final FileLocation location, final BinaryOperator operator)
    {
        super(location);
        this.operator = operator;
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        final long b = assembler.pop();
        final long a = assembler.pop();
        long c = 0;
        switch (this.operator)
        {
        case ADD:
            c = a + b;
            break;
        case SUB:
            c = a - b;
            break;
        case MUL:
            c = a * b;
            break;
        case DIV:
            c = a / b;
            break;
        case AND:
            c = a & b;
            break;
        case OR:
            c = a | b;
            break;
        case XOR:
            c = a ^ b;
            break;
        case SHIFT_LEFT:
            c = a << b;
            break;
        case SHIFT_RIGHT:
            c = a >> b;
            break;
        case EQUAL:
            c = a == b ? 1 : 0;
            break;
        case NOT_EQUAL:
            c = a != b ? 1 : 0;
            break;
        case LESS_THAN:
            c = a < b ? 1 : 0;
            break;
        case LESS_EQUAL:
            c = a <= b ? 1 : 0;
            break;
        case GREATER_THAN:
            c = a > b ? 1 : 0;
            break;
        case GREATER_EQUAL:
            c = a >= b ? 1 : 0;
            break;
        }
        assembler.push(c);
    }

    @Override
    public String toString()
    {
        return "binary:" + this.operator;
    }
}
