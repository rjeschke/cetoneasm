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

package com.github.rjeschke.cetoneasm.emu;

import com.github.rjeschke.cetoneasm.Con;
import com.github.rjeschke.cetoneasm.U;

/**
 * This is a minimal, non-C64, testing environment to test-run algorithms and
 * inspect timing behaviour and runtimes.
 */
public class Machine
{
    private final byte[]        ram          = new byte[65536];
    private final MOS6510       cpu;
    private int                 cycle        = 0;
    private final StringBuilder ioString     = new StringBuilder();
    private boolean             verbose      = false;
    private boolean             extraVerbose = false;

    public Machine()
    {
        this.cpu = new MOS6510(this);
    }

    public boolean irq()
    {
        return false;
    }

    public boolean nmi()
    {
        return false;
    }

    public void reset()
    {
        this.cpu.reset();
    }

    public int loadPrg(final byte[] prg)
    {
        final int loadAddress = (prg[0] & 255) | ((prg[1] & 255) << 8);

        for (int i = 0; i < prg.length - 2; i++)
        {
            this.write(loadAddress + i, prg[2 + i]);
        }

        return loadAddress;
    }

    private static boolean wasEnterPressed()
    {
        boolean wasSpace = false;
        try
        {
            while (System.in.available() > 0)
            {
                final int r = System.in.read();
                if (r == '\n')
                {
                    wasSpace = true;
                }
            }
        }
        catch (final Throwable t)
        {

        }
        return wasSpace;
    }

    public void execute(final int jsrAddress, final boolean verbose, final boolean extraVerbose)
    {
        this.cpu.jsr(jsrAddress, 0);
        this.cycle = 0;
        this.verbose = verbose;
        this.extraVerbose = extraVerbose;
        final StringBuilder line = new StringBuilder();
        for (;;)
        {
            if (verbose && this.cpu.getCycle() == 0)
            {
                if (line.length() > 0)
                {
                    if (this.ioString.length() > 0)
                    {
                        while (line.length() < 60)
                        {
                            line.append(' ');
                        }
                        line.append("; ");
                        line.append(this.ioString);
                    }
                    Con.info(line.toString());
                }
                line.setLength(0);
                this.ioString.setLength(0);

                line.append(String.format("%8d: %04X %s ", this.cycle, this.cpu.getPC(), this.cpu.getStatusLine()));
                line.append(U.disassemble(this.ram, this.cpu.getPC()));
            }
            this.cpu.clock();
            if (this.cpu.getPC() == 0)
            {
                break;
            }
            if (wasEnterPressed())
            {
                Con.warn(" Interrupted");
                break;
            }
            this.cycle++;
        }
        Con.info(" Elapsed time: %d clock cycles, %.3f ms (PAL)", this.cycle, 1000. * this.cycle / 985248.);
    }

    public int read(final int address)
    {
        final int addr = address & 65535;
        if (this.verbose)
        {
            if (this.extraVerbose || addr != this.cpu.getPC())
            {
                this.ioString.append(String.format("R:%d:%04X:%02X ", this.cycle, addr, this.ram[addr] & 255));
            }
        }
        return this.ram[addr] & 255;
    }

    public void write(final int address, final int value)
    {
        final int addr = address & 65535;
        if (this.verbose)
        {
            this.ioString.append(String.format("W:%d:%04X:%02X ", this.cycle, addr, value & 255));
        }
        this.ram[addr] = (byte)value;
    }
}
