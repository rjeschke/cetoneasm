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

public enum MetaCommand
{
    INCLUDE,
    DB,
    DW,
    IF,
    ELIF,
    ELSE,
    ENDIF,
    MACRO,
    ENDMACRO,
    CALL;

    private final static HashMap<String, MetaCommand> MAP = new HashMap<String, MetaCommand>();

    static
    {
        for (final MetaCommand mc : MetaCommand.values())
        {
            MAP.put(mc.toString(), mc);
        }
    }

    public static MetaCommand byName(final String name)
    {
        return MAP.get(name);
    }
}
