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

import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.MetaAction;

public class StringStoreAction extends MetaAction
{
    private final String  value;
    private final boolean isScreens;

    public StringStoreAction(final FileLocation location, final String value, final boolean isScreens)
    {
        super(location);
        this.value = value;
        this.isScreens = isScreens;
    }

    public byte[] getBytes() throws AssemblerException
    {
        try
        {
            final byte[] bytes = this.value.getBytes("US-ASCII");
            if (this.isScreens)
            {
                for (int i = 0; i < bytes.length; i++)
                {
                    bytes[i] &= 0x3f;
                }
            }
            return bytes;
        }
        catch (final UnsupportedEncodingException e)
        {
            this.error("Unsupported encoding", e);
        }
        // We never come here
        return null;
    }

}
