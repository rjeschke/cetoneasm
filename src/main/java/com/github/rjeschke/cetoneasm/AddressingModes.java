package com.github.rjeschke.cetoneasm;

public enum AddressingModes
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
