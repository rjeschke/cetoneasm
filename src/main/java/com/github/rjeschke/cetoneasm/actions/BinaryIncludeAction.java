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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;

public class BinaryIncludeAction extends Action
{
    private final String filename;
    private byte[]       data = null;

    public BinaryIncludeAction(final FileLocation location, final String filename)
    {
        super(location);
        this.filename = filename;
    }

    private void resolve(final Assembler assembler) throws AssemblerException
    {
        if (this.data == null)
        {
            final int length = (int)assembler.pop();
            final int skip = (int)assembler.pop();

            final String resolved = assembler.resolveFilename(this.filename, false);

            if (resolved == null)
            {
                this.error("Can not find binary file '" + this.filename + "'");
            }

            try
            {
                final FileInputStream input = new FileInputStream(resolved);
                try
                {
                    final byte[] buf = new byte[512];
                    int toSkip = skip;
                    while (toSkip > 0)
                    {
                        final int r = input.read(buf, 0, Math.min(buf.length, toSkip));
                        if (r < 0)
                        {
                            break;
                        }
                        toSkip -= r;
                    }
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int todo = length < 0 ? Integer.MAX_VALUE : length;
                    while (todo > 0)
                    {
                        final int r = input.read(buf, 0, Math.min(todo, buf.length));
                        if (r == -1)
                        {
                            break;
                        }
                        out.write(buf, 0, r);
                        todo -= r;
                    }
                    this.data = out.toByteArray();
                }
                finally
                {
                    input.close();
                }
            }
            catch (final IOException e)
            {
                this.error("IOException: " + e.getMessage());
            }
        }
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        this.resolve(assembler);
        for (int i = 0; i < this.data.length; i++)
        {
            assembler.emmitDataByte(this.data[i] & 255);
        }
    }
}
