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

import java.util.concurrent.atomic.AtomicInteger;

public class CounterState
{
    private final static ThreadLocal<CounterState> STATES       = new ThreadLocal<CounterState>()
                                                                {
                                                                    @Override
                                                                    protected CounterState initialValue()
                                                                    {
                                                                        return new CounterState();
                                                                    };
                                                                };

    private final AtomicInteger                    JUMP_COUNTER = new AtomicInteger(0);
    private final AtomicInteger                    ID_COUNTER   = new AtomicInteger(0);

    public static CounterState get()
    {
        return STATES.get();
    }

    public int newJumpId()
    {
        return this.JUMP_COUNTER.incrementAndGet();
    }

    public int newId()
    {
        return this.ID_COUNTER.incrementAndGet();
    }

    public void reset()
    {
        this.JUMP_COUNTER.set(0);
        this.ID_COUNTER.set(0);
    }
}
