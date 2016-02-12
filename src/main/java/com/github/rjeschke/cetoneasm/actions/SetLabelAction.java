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
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;

public class SetLabelAction extends Action
{
    private String        labelName;
    private final boolean isMacroLabel;

    public SetLabelAction(final FileLocation location, final String labelName)
    {
        this(location, labelName, false);
    }

    public SetLabelAction(final FileLocation location, final String labelName, final boolean isMacroLabel)
    {
        super(location);
        this.labelName = labelName;
        this.isMacroLabel = isMacroLabel;
    }

    @Override
    public void run(final Assembler assembler) throws AssemblerException
    {
        // Hack to allow .MACRO to set initial @
        if (this.isMacroLabel && !assembler.isPcSet())
        {
            assembler.setLabelAddress(this.labelName, 0);
        }
        else
        {
            assembler.setLabelAddress(this.labelName, assembler.getPC());
        }
    }

    public String getLabelName()
    {
        return this.labelName;
    }

    public void makeLocal()
    {
        if (!this.labelName.startsWith("_"))
        {
            this.labelName = "__" + this.labelName;
        }
    }

    @Override
    public String toString()
    {
        return "label:" + this.labelName;
    }
}
