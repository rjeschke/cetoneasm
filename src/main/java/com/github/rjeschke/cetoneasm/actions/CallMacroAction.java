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

import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.MetaAction;

public class CallMacroAction extends MetaAction
{
    private final String             name;
    private final List<List<Action>> arguments;

    public CallMacroAction(final FileLocation location, final String name, final List<List<Action>> arguments)
    {
        super(location);
        this.name = name;
        this.arguments = arguments;
    }

    public String getName()
    {
        return this.name;
    }

    public List<List<Action>> getArguments()
    {
        return this.arguments;
    }
}
