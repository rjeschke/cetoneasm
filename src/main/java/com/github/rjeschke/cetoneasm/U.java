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

public final class U
{
    private U()
    {
        // meh!
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static String normalizePath(final String filename)
    {
        final List<String> comps = Strings.split(filename.trim().replace('\\', '/'), '/');

        for (int i = 0; i < comps.size(); i++)
        {
            comps.set(i, comps.get(i).trim());
            if (i > 0 && comps.get(i).isEmpty())
            {
                comps.remove(i--);
            }
            if (i > 1 && comps.get(i).equals(".."))
            {
                comps.remove(i - 1);
                comps.remove(i - 1);
                i -= 2;
            }
        }

        return Strings.join(comps, isWindows() ? "\\" : "/");
    }
}
