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

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Assembler;

public class LoadNumberAction extends Action
{
    private final long value;

    public LoadNumberAction(final FileLocation location, final long value)
    {
        super(location);
        this.value = value;
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        assembler.push(this.value);
    }

    public long getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return "load:" + this.value;
    }
}
