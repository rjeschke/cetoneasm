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

import com.github.rjeschke.cetoneasm.CounterState;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.MetaAction;

public class JumpIdAction extends MetaAction
{
    private final int id;

    public JumpIdAction(final FileLocation location)
    {
        super(location);
        this.id = CounterState.get().newJumpId();
    }

    public int getID()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return "jump-label:" + this.id;
    }
}
