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

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Linker
{
    public static byte[] link(final Config config, final List<CodeContainer> containers) throws LinkerException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final CodeContainer first = containers.get(0);

        out.write(first.getStartAddress());
        out.write(first.getStartAddress() >> 8);
        first.writeTo(out);

        int pc = first.getEndAddress();
        for (int i = 1; i < containers.size(); i++)
        {
            final CodeContainer cc = containers.get(i);
            if (cc.getStartAddress() < pc)
            {
                throw new LinkerException(String.format("Code blocks overlap, conflicting PC $%04d",
                        cc.getStartAddress()));
            }
            for (int n = 0; n < cc.getStartAddress() - pc; n++)
            {
                out.write(0);
            }
            cc.writeTo(out);
            pc = cc.getEndAddress();
        }

        return out.toByteArray();
    }
}
