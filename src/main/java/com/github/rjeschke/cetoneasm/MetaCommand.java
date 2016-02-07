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
