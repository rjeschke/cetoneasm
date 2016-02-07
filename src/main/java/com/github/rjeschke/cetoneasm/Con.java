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

import java.util.List;

import com.github.rjeschke.neetutils.Strings;

public class Con
{
    private static long startTime = 0;

    public static void initialize()
    {
        startTime = System.currentTimeMillis();
    }

    public static enum Type
    {
        INFO,
        WARNING,
        ERROR
    }

    public static void print(final Type type, final String fmt, final Object... args)
    {
        final List<String> outs = Strings.split(String.format(fmt, args), '\n');
        for (final String s : outs)
        {
            final long time = System.currentTimeMillis() - startTime;
            System.out.printf("[%2d.%03d] ", time / 1000, time % 1000);
            switch (type)
            {
            case INFO:
                System.out.print("[I]");
                break;
            case WARNING:
                System.out.print("[W]");
                break;
            case ERROR:
                System.out.print("[E]");
                break;
            }
            System.out.print(" : ");
            System.out.println(s);
        }
    }

    public static void info(final String fmt, final Object... args)
    {
        print(Type.INFO, fmt, args);
    }

    public static void error(final String fmt, final Object... args)
    {
        print(Type.ERROR, fmt, args);
    }

    public static void warn(final String fmt, final Object... args)
    {
        print(Type.WARNING, fmt, args);
    }
}
