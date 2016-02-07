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

public class TokenizerException extends Exception
{
    private static final long  serialVersionUID = -3770986600420935401L;
    private final FileLocation location;

    public TokenizerException(final Tokenizer tokenizer, final String message)
    {
        super(message);
        this.location = tokenizer.getLocation();
    }

    public TokenizerException(final Tokenizer tokenizer, final String message, final Throwable cause)
    {
        super(message, cause);
        this.location = tokenizer.getLocation();
    }

    public FileLocation getLocation()
    {
        return this.location;
    }
}