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

import java.util.ArrayList;
import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.Assembler;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.MetaAction;
import com.github.rjeschke.cetoneasm.Parser;
import com.github.rjeschke.cetoneasm.Tokenizer;
import com.github.rjeschke.cetoneasm.TokenizerException;

public class IncludeAction extends MetaAction
{
    private final String filename;

    public IncludeAction(final FileLocation location, final String filename)
    {
        super(location);
        this.filename = filename;
    }

    public String getFileName()
    {
        return this.filename;
    }

    public List<Action> doInclude(final Assembler assembler, final String resolved) throws AssemblerException
    {
        final Tokenizer tok = new Tokenizer(assembler.getConfig(), resolved);
        try
        {
            return Parser.parse(tok.open());
        }
        catch (final TokenizerException e)
        {
            this.error("Include file parsing failed", e);
        }
        finally
        {
            tok.close();
        }
        // We never reach here
        return new ArrayList<Action>();
    }
}
