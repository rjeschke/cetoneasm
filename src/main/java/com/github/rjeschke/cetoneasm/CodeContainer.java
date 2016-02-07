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

public class CodeContainer
{
    private final int    startAddress;
    private final byte[] buffer   = new byte[65536];
    private int          position = 0;

    public CodeContainer(final int address)
    {
        this.startAddress = address;
    }

    public void add(final int value) throws AssemblerException
    {
        if (this.position + this.startAddress >= this.buffer.length)
        {
            throw new AssemblerException(null, "Code container overflow");
        }

        this.buffer[this.position++] = (byte)value;
    }
}
