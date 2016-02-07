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

public class Variable
{
    private long    value       = 0;
    private boolean initialized = false;
    private boolean wasRead     = false;

    public Variable()
    {

    }

    public Variable(final long value)
    {
        this.value = value;
        this.initialized = true;
    }

    @Override
    public String toString()
    {
        return "var:" + this.initialized + ":" + this.value;
    }

    public void set(final long value)
    {
        this.value = value;
        this.initialized = true;
    }

    public long get()
    {
        this.wasRead = true;
        return this.value;
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    public boolean wasRead()
    {
        return this.wasRead;
    }

    public void reset()
    {
        this.value = 0;
        this.initialized = false;
        this.wasRead = false;
    }
}
