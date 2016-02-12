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

import java.util.ArrayList;
import java.util.List;

public abstract class Action
{
    protected final FileLocation location;

    public Action(final FileLocation location)
    {
        this.location = location;
    }

    public FileLocation getLocation()
    {
        return this.location;
    }

    public void error(final String message) throws AssemblerException
    {
        throw new AssemblerException(this.location, message);
    }

    public void error(final String message, final Throwable cause) throws AssemblerException
    {
        throw new AssemblerException(this.location, message, cause);
    }

    public List<Action> getNestedActions()
    {
        return new ArrayList<Action>();
    }

    public abstract void run(Assembler assembler) throws AssemblerException;
}
