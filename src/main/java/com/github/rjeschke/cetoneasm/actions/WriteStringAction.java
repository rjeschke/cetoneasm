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

import java.io.UnsupportedEncodingException;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;

public class WriteStringAction extends Action
{
    private final String  value;
    private final byte[]  bytes;
    private final boolean isWords;

    public WriteStringAction(final FileLocation location, final String value, final boolean isWords,
            final boolean isScreen)
    {
        super(location);
        this.value = value;
        this.isWords = isWords;

        try
        {
            this.bytes = value.getBytes("US-ASCII");
            if (isScreen)
            {
                for (int i = 0; i < this.bytes.length; i++)
                {
                    this.bytes[i] &= 0x3f;
                }
            }
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new RuntimeException("WTF", e);
        }
    }

    public String getValue()
    {
        return this.value;
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        if (this.isWords)
        {
            for (int i = 0; i < this.bytes.length; i++)
            {
                assembler.emmitDataWord(this.bytes[i] & 255);
            }
        }
        else
        {
            for (int i = 0; i < this.bytes.length; i++)
            {
                assembler.emmitDataByte(this.bytes[i] & 255);
            }
        }
    }

    @Override
    public String toString()
    {
        return "write-string:" + this.value;
    }
}
