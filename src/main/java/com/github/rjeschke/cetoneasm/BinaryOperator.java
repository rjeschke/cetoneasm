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

public enum BinaryOperator
{
    ADD(11),
    SUB(11),
    MUL(12),
    DIV(12),
    AND(7),
    OR(5),
    XOR(6),
    SHIFT_LEFT(10),
    SHIFT_RIGHT(10),
    EQUAL(8),
    NOT_EQUAL(8),
    LESS_THAN(9),
    LESS_EQUAL(9),
    GREATER_THAN(9),
    GREATER_EQUAL(9);

    private final int priority;

    private BinaryOperator(final int priority)
    {
        this.priority = priority;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public static BinaryOperator fromToken(final Token token)
    {
        switch (token.getType())
        {
        case PLUS:
            return ADD;
        case MINUS:
            return SUB;
        case MUL:
            return MUL;
        case DIV:
            return DIV;
        case AND:
            return AND;
        case OR:
            return OR;
        case XOR:
            return XOR;
        case SHIFT_LEFT:
            return SHIFT_LEFT;
        case SHIFT_RIGHT:
            return SHIFT_RIGHT;
        case EQUAL:
            return EQUAL;
        case NOT_EQUAL:
            return NOT_EQUAL;
        case LESS_THAN:
            return LESS_THAN;
        case LESS_EQUAL:
            return LESS_EQUAL;
        case GREATER_THAN:
            return GREATER_THAN;
        case GREATER_EQUAL:
            return GREATER_EQUAL;
        default:
            return null;
        }
    }
}
