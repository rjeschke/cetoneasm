package com.github.rjeschke.cetoneasm;

public class CodeContainer
{
    private final int    startAddress;
    private final byte[] buffer   = new byte[65536];
    private int          position = 0;

    public CodeContainer(final int address)
    {
        this.startAddress = address;
    }

    public void add(final int value) throws AssemblerException
    {
        if (this.position + this.startAddress >= this.buffer.length)
        {
            throw new AssemblerException(null, "Code container overflow");
        }

        this.buffer[this.position++] = (byte)value;
    }
}
