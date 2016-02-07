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

public enum UnaryOperator
{
    NOT,
    NEG,
    MINUS,
    LOW,
    HIGH;

    private final int priority = 20;

    public int getPriority()
    {
        return this.priority;
    }

    public static UnaryOperator fromToken(final Token token)
    {
        switch (token.getType())
        {
        case NOT:
            return NOT;
        case LESS_THAN:
            return LOW;
        case NEG:
            return NEG;
        case MINUS:
            return MINUS;
        case GREATER_THAN:
            return HIGH;
        default:
            return null;
        }
    }
}
