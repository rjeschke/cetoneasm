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

public enum AddressingMode
{
    IMPLIED, // NOP, ASL
    RELATIVE, // BNE $ab
    IMMEDIATE, // LDA #$ab
    ABSOLUTE, // LDA $abcd
    ABSOLUTE_X, // LDA $abcd,X
    ABSOLUTE_Y, // LDA $abcd,Y
    ZEROPAGE, // LDA $ab
    ZEROPAGE_X, // LDA $ab,X
    ZEROPAGE_Y, // LDA $ab,Y
    INDIRECT, // JMP ($abcd)
    INDEXED_INDIRECT, // LDA ($ab),Y
    INDIRECT_INDEXED, // LDA ($ab,X)
    ILL
}
