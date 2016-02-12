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

/*
 * NES Mos6502 (decimal mode missing), cycle-exact
 */
public class MOS6510
{
    public final static int        MOS6510_IRQ = 1;
    public final static int        MOS6510_NMI = 2;
    public final static int        MOS6510_RST = 4;

    public final static int        FLAG_N      = 0x80;
    public final static int        FLAG_V      = 0x40;
    public final static int        FLAG_B      = 0x10;
    public final static int        FLAG_D      = 0x08;
    public final static int        FLAG_I      = 0x04;
    public final static int        FLAG_Z      = 0x02;
    public final static int        FLAG_C      = 0x01;

    public final static Opcode[]   OPCODES     = {
                                               // $00 - $07
            Opcode.BRK, Opcode.ORA, Opcode.KIL, Opcode.SLO, Opcode.NOP, Opcode.ORA, Opcode.ASL, Opcode.SLO,
            // $08 - $0F
            Opcode.PHP, Opcode.ORA, Opcode.ASL, Opcode.ANC, Opcode.NOP, Opcode.ORA, Opcode.ASL, Opcode.SLO,
            // $10 - $17
            Opcode.BPL, Opcode.ORA, Opcode.KIL, Opcode.SLO, Opcode.NOP, Opcode.ORA, Opcode.ASL, Opcode.SLO,
            // $18 - $1F
            Opcode.CLC, Opcode.ORA, Opcode.NOP, Opcode.SLO, Opcode.NOP, Opcode.ORA, Opcode.ASL, Opcode.SLO,
            // $20 - $27
            Opcode.JSR, Opcode.AND, Opcode.KIL, Opcode.RLA, Opcode.BIT, Opcode.AND, Opcode.ROL, Opcode.RLA,
            // $28 - $2F
            Opcode.PLP, Opcode.AND, Opcode.ROL, Opcode.ANC, Opcode.BIT, Opcode.AND, Opcode.ROL, Opcode.RLA,
            // $30 - $37
            Opcode.BMI, Opcode.AND, Opcode.KIL, Opcode.RLA, Opcode.NOP, Opcode.AND, Opcode.ROL, Opcode.RLA,
            // $38 - $3F
            Opcode.SEC, Opcode.AND, Opcode.NOP, Opcode.RLA, Opcode.NOP, Opcode.AND, Opcode.ROL, Opcode.RLA,
            // $40 - $47
            Opcode.RTI, Opcode.EOR, Opcode.KIL, Opcode.SRE, Opcode.NOP, Opcode.EOR, Opcode.LSR, Opcode.SRE,
            // $48 - $4F
            Opcode.PHA, Opcode.EOR, Opcode.LSR, Opcode.ALR, Opcode.JMP, Opcode.EOR, Opcode.LSR, Opcode.SRE,
            // $50 - $57
            Opcode.BVC, Opcode.EOR, Opcode.KIL, Opcode.SRE, Opcode.NOP, Opcode.EOR, Opcode.LSR, Opcode.SRE,
            // $58 - $5F
            Opcode.CLI, Opcode.EOR, Opcode.NOP, Opcode.SRE, Opcode.NOP, Opcode.EOR, Opcode.LSR, Opcode.SRE,
            // $60 - $67
            Opcode.RTS, Opcode.ADC, Opcode.KIL, Opcode.RRA, Opcode.NOP, Opcode.ADC, Opcode.ROR, Opcode.RRA,
            // $68 - $6F
            Opcode.PLA, Opcode.ADC, Opcode.ROR, Opcode.ARR, Opcode.JMP, Opcode.ADC, Opcode.ROR, Opcode.RRA,
            // $70 - $77
            Opcode.BVS, Opcode.ADC, Opcode.KIL, Opcode.RRA, Opcode.NOP, Opcode.ADC, Opcode.ROR, Opcode.RRA,
            // $78 - $7F
            Opcode.SEI, Opcode.ADC, Opcode.NOP, Opcode.RRA, Opcode.NOP, Opcode.ADC, Opcode.ROR, Opcode.RRA,
            // $80 - $87
            Opcode.NOP, Opcode.STA, Opcode.NOP, Opcode.SAX, Opcode.STY, Opcode.STA, Opcode.STX, Opcode.SAX,
            // $88 - $8F
            Opcode.DEY, Opcode.NOP, Opcode.TXA, Opcode.XAA, Opcode.STY, Opcode.STA, Opcode.STX, Opcode.SAX,
            // $90 - $97
            Opcode.BCC, Opcode.STA, Opcode.KIL, Opcode.AHX, Opcode.STY, Opcode.STA, Opcode.STX, Opcode.SAX,
            // $98 - $9F
            Opcode.TYA, Opcode.STA, Opcode.TXS, Opcode.TAS, Opcode.SHY, Opcode.STA, Opcode.SHX, Opcode.AHX,
            // $A0 - $A7
            Opcode.LDY, Opcode.LDA, Opcode.LDX, Opcode.LAX, Opcode.LDY, Opcode.LDA, Opcode.LDX, Opcode.LAX,
            // $A8 - $AF
            Opcode.TAY, Opcode.LDA, Opcode.TAX, Opcode.LAX, Opcode.LDY, Opcode.LDA, Opcode.LDX, Opcode.LAX,
            // $B0 - $B7
            Opcode.BCS, Opcode.LDA, Opcode.KIL, Opcode.LAX, Opcode.LDY, Opcode.LDA, Opcode.LDX, Opcode.LAX,
            // $B8 - $BF
            Opcode.CLV, Opcode.LDA, Opcode.TSX, Opcode.LAS, Opcode.LDY, Opcode.LDA, Opcode.LDX, Opcode.LAX,
            // $C0 - $C7
            Opcode.CPY, Opcode.CMP, Opcode.NOP, Opcode.DCP, Opcode.CPY, Opcode.CMP, Opcode.DEC, Opcode.DCP,
            // $C8 - $CF
            Opcode.INY, Opcode.CMP, Opcode.DEX, Opcode.AXS, Opcode.CPY, Opcode.CMP, Opcode.DEC, Opcode.DCP,
            // $D0 - $D7
            Opcode.BNE, Opcode.CMP, Opcode.KIL, Opcode.DCP, Opcode.NOP, Opcode.CMP, Opcode.DEC, Opcode.DCP,
            // $D8 - $DF
            Opcode.CLD, Opcode.CMP, Opcode.NOP, Opcode.DCP, Opcode.NOP, Opcode.CMP, Opcode.DEC, Opcode.DCP,
            // $E0 - $E7
            Opcode.CPX, Opcode.SBC, Opcode.NOP, Opcode.ISC, Opcode.CPX, Opcode.SBC, Opcode.INC, Opcode.ISC,
            // $E8 - $EF
            Opcode.INX, Opcode.SBC, Opcode.NOP, Opcode.SBC, Opcode.CPX, Opcode.SBC, Opcode.INC, Opcode.ISC,
            // $F0 - $F7
            Opcode.BEQ, Opcode.SBC, Opcode.KIL, Opcode.ISC, Opcode.NOP, Opcode.SBC, Opcode.INC, Opcode.ISC,
            // $F8 - $FF
            Opcode.SED, Opcode.SBC, Opcode.NOP, Opcode.ISC, Opcode.NOP, Opcode.SBC, Opcode.INC, Opcode.ISC
                                               };

    public final static AddrMode[] ADDR_MODES  = {
                                               // $00 - $07
            AddrMode.BRK,
            AddrMode.ZPIX_READ,
            AddrMode.KIL,
            AddrMode.ZPIX_RMW,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $08 - $0F
            AddrMode.PUSH,
            AddrMode.IMMEDIATE,
            AddrMode.ACCU,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $10 - $17
            AddrMode.REL,
            AddrMode.ZPIY_READ,
            AddrMode.KIL,
            AddrMode.ZPIY_RMW,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW,
            AddrMode.ZPX_RMW,
            // $18 - $1F
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.NONE,
            AddrMode.ABSY_RMW,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_RMW,
            AddrMode.ABSX_RMW,
            // $20 - $27
            AddrMode.JSR,
            AddrMode.ZPIX_READ,
            AddrMode.KIL,
            AddrMode.ZPIX_RMW,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $28 - $2F
            AddrMode.POP,
            AddrMode.IMMEDIATE,
            AddrMode.ACCU,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $30 - $37
            AddrMode.REL,
            AddrMode.ZPIY_READ,
            AddrMode.KIL,
            AddrMode.ZPIY_RMW,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW,
            AddrMode.ZPX_RMW,
            // $38 - $3F
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.NONE,
            AddrMode.ABSY_RMW,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_RMW,
            AddrMode.ABSX_RMW,
            // $40 - $47
            AddrMode.RTI,
            AddrMode.ZPIX_READ,
            AddrMode.KIL,
            AddrMode.ZPIX_RMW,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $48 - $4F
            AddrMode.PUSH,
            AddrMode.IMMEDIATE,
            AddrMode.ACCU,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_JMP,
            AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $50 - $57
            AddrMode.REL,
            AddrMode.ZPIY_READ,
            AddrMode.KIL,
            AddrMode.ZPIY_RMW,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW,
            AddrMode.ZPX_RMW,
            // $58 - $5F
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.NONE,
            AddrMode.ABSY_RMW,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_RMW,
            AddrMode.ABSX_RMW,
            // $60 - $67
            AddrMode.RTS,
            AddrMode.ZPIX_READ,
            AddrMode.KIL,
            AddrMode.ZPIX_RMW,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $68 - $6F
            AddrMode.POP,
            AddrMode.IMMEDIATE,
            AddrMode.ACCU,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_IND,
            AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $70 - $77
            AddrMode.REL,
            AddrMode.ZPIY_READ,
            AddrMode.KIL,
            AddrMode.ZPIY_RMW,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW,
            AddrMode.ZPX_RMW,
            // $78 - $7F
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.NONE,
            AddrMode.ABSY_RMW,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_RMW,
            AddrMode.ABSX_RMW,
            // $80 - $87
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_WRITE,
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_WRITE,
            AddrMode.ZP_WRITE,
            AddrMode.ZP_WRITE,
            AddrMode.ZP_WRITE,
            AddrMode.ZP_WRITE,
            // $88 - $8F
            AddrMode.NONE,
            AddrMode.IMMEDIATE,
            AddrMode.NONE,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_WRITE,
            AddrMode.ABS_WRITE,
            AddrMode.ABS_WRITE,
            AddrMode.ABS_WRITE,
            // $90 - $97
            AddrMode.REL,
            AddrMode.ZPIY_WRITE,
            AddrMode.KIL,
            AddrMode.ZPIY_WRITE,
            AddrMode.ZPX_WRITE,
            AddrMode.ZPX_WRITE,
            AddrMode.ZPY_WRITE,
            AddrMode.ZPY_WRITE,
            // $98 - $9F
            AddrMode.NONE,
            AddrMode.ABSY_WRITE,
            AddrMode.NONE,
            AddrMode.ABSY_WRITE,
            AddrMode.ABSX_WRITE,
            AddrMode.ABSX_WRITE,
            AddrMode.ABSY_WRITE,
            AddrMode.ABSY_WRITE,
            // $A0 - $A7
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_READ,
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            // $A8 - $AF
            AddrMode.NONE,
            AddrMode.IMMEDIATE,
            AddrMode.NONE,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            // $B0 - $B7
            AddrMode.REL,
            AddrMode.ZPIY_READ,
            AddrMode.KIL,
            AddrMode.ZPIY_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPY_READ,
            AddrMode.ZPY_READ,
            // $B8 - $BF
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.NONE,
            AddrMode.ABSY_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSY_READ,
            AddrMode.ABSY_READ,
            // $C0 - $C7
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_READ,
            AddrMode.IMMEDIATE,
            AddrMode.ZPIX_RMW,
            AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $C8 - $CF
            AddrMode.NONE, AddrMode.IMMEDIATE,
            AddrMode.NONE,
            AddrMode.IMMEDIATE,
            AddrMode.ABS_READ,
            AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $D0 - $D7
            AddrMode.REL, AddrMode.ZPIY_READ, AddrMode.KIL,
            AddrMode.ZPIY_RMW,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW,
            AddrMode.ZPX_RMW,
            // $D8 - $DF
            AddrMode.NONE, AddrMode.ABSY_READ, AddrMode.NONE, AddrMode.ABSY_RMW,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_READ,
            AddrMode.ABSX_RMW,
            AddrMode.ABSX_RMW,
            // $E0 - $E7
            AddrMode.IMMEDIATE, AddrMode.ZPIX_READ, AddrMode.IMMEDIATE, AddrMode.ZPIX_RMW, AddrMode.ZP_READ,
            AddrMode.ZP_READ,
            AddrMode.ZP_RMW,
            AddrMode.ZP_RMW,
            // $E8 - $EF
            AddrMode.NONE, AddrMode.IMMEDIATE, AddrMode.NONE, AddrMode.IMMEDIATE, AddrMode.ABS_READ, AddrMode.ABS_READ,
            AddrMode.ABS_RMW,
            AddrMode.ABS_RMW,
            // $F0 - $F7
            AddrMode.REL, AddrMode.ZPIY_READ, AddrMode.KIL, AddrMode.ZPIY_RMW, AddrMode.ZPX_READ, AddrMode.ZPX_READ,
            AddrMode.ZPX_RMW, AddrMode.ZPX_RMW,
            // $F8 - $FF
            AddrMode.NONE, AddrMode.ABSY_READ, AddrMode.NONE, AddrMode.ABSY_RMW, AddrMode.ABSX_READ,
            AddrMode.ABSX_READ, AddrMode.ABSX_RMW, AddrMode.ABSX_RMW
                                               };

    private int                    a;
    private int                    x;
    private int                    y;
    private int                    sr;
    private int                    sp;
    private int                    pcl;
    private int                    pch;
    private int                    pcc;
    private int                    addrl;
    private int                    addrh;
    private boolean                addrc;
    private int                    operand;
    private int                    decoding;
    private final Machine          machine;
    private int                    cycle;
    private AddrMode               addrMode;
    private Opcode                 opcode;
    private boolean                irq;
    private boolean                irqLatch;
    private boolean                nmi;
    private boolean                nmiLatch;
    private boolean                debug       = false;
    private boolean                read        = true;

    public MOS6510(final Machine machine)
    {
        this.machine = machine;
    }

    public int getCycle()
    {
        return this.cycle;
    }

    public boolean isReading()
    {
        return this.read;
    }

    public void setDebugMode(final boolean mode)
    {
        this.debug = mode;
    }

    public String getStatusLine()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X %02X %02X %02X ", this.a, this.x, this.y, this.sp));
        for (int i = 7; i >= 0; i--)
        {
            sb.append((this.sr & (1 << i)) != 0 ? "1" : "0");
        }
        return sb.toString();
    }

    private void doIRQs()
    {
        if (this.machine.irq())
        {
            if (!this.irqLatch)
            {
                this.irqLatch = this.irq = true;
            }
        }
        else
        {
            this.irq = this.irqLatch = false;
        }
        if (this.machine.nmi())
        {
            if (!this.nmiLatch)
            {
                this.nmiLatch = this.nmi = true;
            }
        }
        else
        {
            this.nmi = this.nmiLatch = false;
        }
    }

    public void reset()
    {
        this.a = this.x = this.y = 0;
        this.cycle = 0;
        this.sp = 0xfd;
        this.sr = 0x20 | FLAG_B | FLAG_I;
        this.pcl = this.machine.read(0xfffc);
        this.pch = this.machine.read(0xfffd);
        this.nmi = this.nmiLatch = false;
        this.irq = this.irqLatch = false;
        this.read = true;
    }

    public void setAXY(final int val)
    {
        this.a = this.x = this.y = val & 0xff;
    }

    public void jsr(final int dest, final int src)
    {
        this.cycle = 0;
        final int src2 = (src - 1) & 0xffff;
        this.push(src2 >> 8);
        this.push(src2);
        this.pcl = dest & 0xff;
        this.pch = (dest >> 8) & 0xff;
    }

    private void push(final int value)
    {
        this.machine.write(0x100 | this.sp, value & 0xff);
        this.decSP();
    }

    public void jmp(final int dest)
    {
        this.pcl = dest & 0xff;
        this.pch = (dest >> 8) & 0xff;
    }

    private void addAddr(final int value)
    {
        this.addrl = this.addrl + value;
        this.addrc = this.addrl > 0xff;
        this.addrl &= 0xff;
    }

    private boolean fixAddr()
    {
        if (this.addrc)
        {
            this.addrh = (this.addrh + 1) & 0xff;
            this.addrc = false;
            return true;
        }
        return false;
    }

    private void incPC()
    {
        this.pcl++;
        this.pch = (this.pch + (this.pcl >> 8)) & 0xff;
        this.pcl &= 0xff;
    }

    private void incSP()
    {
        this.sp = (this.sp + 1) & 0xff;
    }

    private void decSP()
    {
        this.sp = (this.sp - 1) & 0xff;
    }

    public int getPC()
    {
        return this.pcl + (this.pch << 8);
    }

    private int getAddr()
    {
        return this.addrl + (this.addrh << 8);
    }

    private void fetchOpcode()
    {
        this.decoding = this.machine.read(this.getPC());
        this.incPC();
    }

    private void fetchOperand()
    {
        this.operand = this.machine.read(this.getPC());
        this.incPC();
    }

    private void fetchAddrl()
    {
        this.addrl = this.machine.read(this.getPC());
        this.addrh = 0;
        this.addrc = false;
        this.incPC();
    }

    private void fetchAddrh()
    {
        this.addrh = this.machine.read(this.getPC());
        this.incPC();
    }

    private void wasteRead()
    {
        this.machine.read(this.getPC());
    }

    private void initOpcode()
    {
        this.read = true;
        this.cycle = 1;

        if (this.nmi)
        {
            this.nmi = false;
            this.opcode = Opcode.NMI;
            this.addrMode = AddrMode.NMI;
            this.cycle = 1;
            return;
        }

        if (this.irq && (this.sr & FLAG_I) == 0)
        {
            this.irq = false;
            this.opcode = Opcode.IRQ;
            this.addrMode = AddrMode.IRQ;
            this.cycle = 1;
            return;
        }

        this.fetchOpcode();
        // this.opcode = OPCODES[this.decoding];
        // this.addrMode = ADDR_MODES[this.decoding];
        // /*
        switch (this.decoding)
        {
        case 0x00: // BRK
            this.opcode = Opcode.BRK;
            this.addrMode = AddrMode.BRK;
            break;
        case 0x01: // ORA ($xx,X)
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0x02: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x03: // SLO ($xx,X)
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0x04: // NOP $xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x05: // ORA $xx
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x06: // ASL $xx
            this.opcode = Opcode.ASL;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0x07: // SLO $xx
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0x08: // PHP
            this.opcode = Opcode.PHP;
            this.addrMode = AddrMode.PUSH;
            break;
        case 0x09: // ORA #$xx
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x0a: // ASL A
            this.opcode = Opcode.ASL;
            this.addrMode = AddrMode.ACCU;
            break;
        case 0x0b: // ANC #$xx
            this.opcode = Opcode.ANC;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x0c: // NOP $xxxx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x0d: // ORA $xxxx
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x0e: // ASL $xxxx
            this.opcode = Opcode.ASL;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0x0f: // SLO $xxxx
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0x10: // BPL
            this.opcode = Opcode.BPL;
            this.addrMode = AddrMode.REL;
            break;
        case 0x11: // ORA ($xx),Y
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0x12: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x13: // SLO ($xx),Y
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0x14: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x15: // ORA $xx,X
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x16: // ASL $xx,X
            this.opcode = Opcode.ASL;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0x17: // SLO $xx,X
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0x18: // CLC
            this.opcode = Opcode.CLC;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x19: // ORA $xxxx,Y
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0x1a: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x1b: // SLO $xxxx,Y
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0x1c: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x1d: // ORA $xxxx,X
            this.opcode = Opcode.ORA;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x1e: // ASL $xxxx,X
            this.opcode = Opcode.ASL;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0x1f: // SLO $xxxx,X
            this.opcode = Opcode.SLO;
            this.addrMode = AddrMode.ABSX_RMW;
            break;

        case 0x20: // JSR $xxxx
            this.opcode = Opcode.JSR;
            this.addrMode = AddrMode.JSR;
            break;
        case 0x21: // AND ($xx,X)
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0x22: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x23: // RLA ($xx,X)
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0x24: // BIT $xx
            this.opcode = Opcode.BIT;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x25: // AND $xx
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x26: // ROL $xx
            this.opcode = Opcode.ROL;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0x27: // RLA $xx
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0x28: // PLP
            this.opcode = Opcode.PLP;
            this.addrMode = AddrMode.POP;
            break;
        case 0x29: // AND #$xx
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x2a: // ROL A
            this.opcode = Opcode.ROL;
            this.addrMode = AddrMode.ACCU;
            break;
        case 0x2b: // ANC #$xx
            this.opcode = Opcode.ANC;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x2c: // BIT $xxxx
            this.opcode = Opcode.BIT;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x2d: // AND $xxxx
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x2e: // ROL $xxxx
            this.opcode = Opcode.ROL;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0x2f: // RLA $xxxx
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0x30: // BMI
            this.opcode = Opcode.BMI;
            this.addrMode = AddrMode.REL;
            break;
        case 0x31: // AND ($xx),Y
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0x32: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x33: // RLA ($xx),Y
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0x34: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x35: // AND $xx,X
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x36: // ROL $xx,X
            this.opcode = Opcode.ROL;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0x37: // RLA $xx,X
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0x38: // SEC
            this.opcode = Opcode.SEC;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x39: // AND $xxxx,Y
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0x3a: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x3b: // RLA $xxxx,Y
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0x3c: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x3d: // AND $xxxx,X
            this.opcode = Opcode.AND;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x3e: // ROL $xxxx,X
            this.opcode = Opcode.ROL;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0x3f: // RLA $xxxx,X
            this.opcode = Opcode.RLA;
            this.addrMode = AddrMode.ABSX_RMW;
            break;

        case 0x40: // RTI
            this.opcode = Opcode.RTI;
            this.addrMode = AddrMode.RTI;
            break;
        case 0x41: // EOR ($xx,X)
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0x42: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x43: // SRE ($xx,X)
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0x44: // NOP $xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x45: // EOR $xx
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x46: // LSR $xx
            this.opcode = Opcode.LSR;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0x47: // SRE $xx
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0x48: // PHA
            this.opcode = Opcode.PHA;
            this.addrMode = AddrMode.PUSH;
            break;
        case 0x49: // EOR #$xx
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x4a: // LSR A
            this.opcode = Opcode.LSR;
            this.addrMode = AddrMode.ACCU;
            break;
        case 0x4b: // ALR #$xx
            this.opcode = Opcode.ALR;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x4c: // JMP $xxxx
            this.opcode = Opcode.JMP;
            this.addrMode = AddrMode.ABS_JMP;
            break;
        case 0x4d: // EOR $xxxx
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x4e: // LSR $xxxx
            this.opcode = Opcode.LSR;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0x4f: // SRE $xxxx
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0x50: // BVC
            this.opcode = Opcode.BVC;
            this.addrMode = AddrMode.REL;
            break;
        case 0x51: // EOR ($xx),Y
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0x52: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x53: // SRE ($xx),Y
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0x54: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x55: // EOR $xx,X
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x56: // LSR $xx,X
            this.opcode = Opcode.LSR;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0x57: // SRE $xx,X
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0x58: // CLI
            this.opcode = Opcode.CLI;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x59: // EOR $xxxx,Y
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0x5a: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x5b: // SRE $xxxx,Y
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0x5c: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x5d: // EOR $xxxx,X
            this.opcode = Opcode.EOR;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x5e: // LSR $xxxx,X
            this.opcode = Opcode.LSR;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0x5f: // SRE $xxxx,X
            this.opcode = Opcode.SRE;
            this.addrMode = AddrMode.ABSX_RMW;
            break;

        case 0x60: // RTS
            this.opcode = Opcode.RTS;
            this.addrMode = AddrMode.RTS;
            break;
        case 0x61: // ADC ($xx,X)
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0x62: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x63: // RRA ($xx,X)
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0x64: // NOP $xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x65: // ADC $xx
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0x66: // ROR $xx
            this.opcode = Opcode.ROR;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0x67: // RRA $xx
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0x68: // PLA
            this.opcode = Opcode.PLA;
            this.addrMode = AddrMode.POP;
            break;
        case 0x69: // ADC #$xx
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x6a: // ROR A
            this.opcode = Opcode.ROR;
            this.addrMode = AddrMode.ACCU;
            break;
        case 0x6b: // ARR #$xx
            this.opcode = Opcode.ARR;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x6c: // JMP ($xxxx)
            this.opcode = Opcode.JMP;
            this.addrMode = AddrMode.ABS_IND;
            break;
        case 0x6d: // ADC $xxxx
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0x6e: // ROR $xxxx
            this.opcode = Opcode.ROR;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0x6f: // RRA $xxxx
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0x70: // BVS
            this.opcode = Opcode.BVS;
            this.addrMode = AddrMode.REL;
            break;
        case 0x71: // ADC ($xx),Y
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0x72: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x73: // RRA ($xx),Y
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0x74: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x75: // ADC $xx,X
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0x76: // ROR $xx,X
            this.opcode = Opcode.ROR;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0x77: // RRA $xx,X
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0x78: // SEI
            this.opcode = Opcode.SEI;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x79: // ADC $xxxx,Y
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0x7a: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x7b: // RRA $xxxx,Y
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0x7c: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x7d: // ADC $xxxx,X
            this.opcode = Opcode.ADC;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0x7e: // ROR $xxxx,X
            this.opcode = Opcode.ROR;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0x7f: // RRA $xxxx,X
            this.opcode = Opcode.RRA;
            this.addrMode = AddrMode.ABSX_RMW;
            break;

        case 0x80: // NOP #$xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x81: // STA ($xx,X)
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ZPIX_WRITE;
            break;
        case 0x82: // NOP #$xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x83: // SAX ($xx,X)
            this.opcode = Opcode.SAX;
            this.addrMode = AddrMode.ZPIX_WRITE;
            break;
        case 0x84: // STY $xx
            this.opcode = Opcode.STY;
            this.addrMode = AddrMode.ZP_WRITE;
            break;
        case 0x85: // STA $xx
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ZP_WRITE;
            break;
        case 0x86: // STX $xx
            this.opcode = Opcode.STX;
            this.addrMode = AddrMode.ZP_WRITE;
            break;
        case 0x87: // SAX $xx
            this.opcode = Opcode.SAX;
            this.addrMode = AddrMode.ZP_WRITE;
            break;

        case 0x88: // DEY
            this.opcode = Opcode.DEY;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x89: // NOP #$xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x8a: // TXA
            this.opcode = Opcode.TXA;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x8b: // XAA #$xx
            this.opcode = Opcode.XAA;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0x8c: // STY $xxxxx
            this.opcode = Opcode.STY;
            this.addrMode = AddrMode.ABS_WRITE;
            break;
        case 0x8d: // STA $xxxxx
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ABS_WRITE;
            break;
        case 0x8e: // STX $xxxxx
            this.opcode = Opcode.STX;
            this.addrMode = AddrMode.ABS_WRITE;
            break;
        case 0x8f: // SAX $xxxxx
            this.opcode = Opcode.SAX;
            this.addrMode = AddrMode.ABS_WRITE;
            break;

        case 0x90: // BCC
            this.opcode = Opcode.BCC;
            this.addrMode = AddrMode.REL;
            break;
        case 0x91: // STA ($xx),Y
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ZPIY_WRITE;
            break;
        case 0x92: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0x93: // AHX ($xx),Y
            this.opcode = Opcode.AHX;
            this.addrMode = AddrMode.ZPIY_WRITE;
            break;
        case 0x94: // STY $xx,X
            this.opcode = Opcode.STY;
            this.addrMode = AddrMode.ZPX_WRITE;
            break;
        case 0x95: // STA $xx,X
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ZPX_WRITE;
            break;
        case 0x96: // STX $xx,Y
            this.opcode = Opcode.STX;
            this.addrMode = AddrMode.ZPY_WRITE;
            break;
        case 0x97: // SAX $xx,Y
            this.opcode = Opcode.SAX;
            this.addrMode = AddrMode.ZPY_WRITE;
            break;

        case 0x98: // TYA
            this.opcode = Opcode.TYA;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x99: // STA $xxxx,Y
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ABSY_WRITE;
            break;
        case 0x9a: // TXS
            this.opcode = Opcode.TXS;
            this.addrMode = AddrMode.NONE;
            break;
        case 0x9b: // TAS $xxxx,Y
            this.opcode = Opcode.TAS;
            this.addrMode = AddrMode.ABSY_WRITE;
            break;
        case 0x9c: // SHY $xxxx,X
            this.opcode = Opcode.SHY;
            this.addrMode = AddrMode.ABSX_WRITE;
            break;
        case 0x9d: // STA $xxxx,X
            this.opcode = Opcode.STA;
            this.addrMode = AddrMode.ABSX_WRITE;
            break;
        case 0x9e: // SHX $xxxx,Y
            this.opcode = Opcode.SHX;
            this.addrMode = AddrMode.ABSY_WRITE;
            break;
        case 0x9f: // AHX $xxxx,Y
            this.opcode = Opcode.AHX;
            this.addrMode = AddrMode.ABSY_WRITE;
            break;

        case 0xa0: // LDY #$xx
            this.opcode = Opcode.LDY;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xa1: // LDA ($xx,X)
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0xa2: // LDX #$xx
            this.opcode = Opcode.LDX;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xa3: // LAX ($xx,X)
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0xa4: // LDY $xx
            this.opcode = Opcode.LDY;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xa5: // LDA $xx
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xa6: // LDX $xx
            this.opcode = Opcode.LDX;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xa7: // LAX $xx
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ZP_READ;
            break;

        case 0xa8: // TAY
            this.opcode = Opcode.TAY;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xa9: // LDA #$xx
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xaa: // TAX
            this.opcode = Opcode.TAX;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xab: // LAX #$xx
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xac: // LDY $xxxxx
            this.opcode = Opcode.LDY;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xad: // LDA $xxxxx
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xae: // LDX $xxxxx
            this.opcode = Opcode.LDX;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xaf: // LAX $xxxxx
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ABS_READ;
            break;

        case 0xb0: // BCS
            this.opcode = Opcode.BCS;
            this.addrMode = AddrMode.REL;
            break;
        case 0xb1: // LDA ($xx),Y
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0xb2: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0xb3: // LAX ($xx),Y
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0xb4: // LDY $xx,X
            this.opcode = Opcode.LDY;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xb5: // LDA $xx,X
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xb6: // LDX $xx,Y
            this.opcode = Opcode.LDX;
            this.addrMode = AddrMode.ZPY_READ;
            break;
        case 0xb7: // LAX $xx,Y
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ZPY_READ;
            break;

        case 0xb8: // CLV
            this.opcode = Opcode.CLV;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xb9: // LDA $xxxx,Y
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0xba: // TSX
            this.opcode = Opcode.TSX;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xbb: // LAS $xxxx,Y
            this.opcode = Opcode.LAS;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0xbc: // LDY $xxxx,X
            this.opcode = Opcode.LDY;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xbd: // LDA $xxxx,X
            this.opcode = Opcode.LDA;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xbe: // LDX $xxxx,Y
            this.opcode = Opcode.LDX;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0xbf: // LAX $xxxx,Y
            this.opcode = Opcode.LAX;
            this.addrMode = AddrMode.ABSY_READ;
            break;

        case 0xc0: // CPY #$xx
            this.opcode = Opcode.CPY;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xc1: // CMP ($xx,X)
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0xc2: // NOP #$xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xc3: // DCP ($xx,X)
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0xc4: // CPY $xx
            this.opcode = Opcode.CPY;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xc5: // CMP $xx
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xc6: // DEC $xx
            this.opcode = Opcode.DEC;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0xc7: // DCP $xx
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0xc8: // INY
            this.opcode = Opcode.INY;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xc9: // CMP #$xx
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xca: // DEX
            this.opcode = Opcode.DEX;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xcb: // AXS #$xx
            this.opcode = Opcode.AXS;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xcc: // CPY $xxxxx
            this.opcode = Opcode.CPY;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xcd: // CMP $xxxxx
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xce: // DEC $xxxxx
            this.opcode = Opcode.DEC;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0xcf: // DCP $xxxxx
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0xd0: // BNE
            this.opcode = Opcode.BNE;
            this.addrMode = AddrMode.REL;
            break;
        case 0xd1: // CMP ($xx),Y
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0xd2: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0xd3: // DCP ($xx),Y
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0xd4: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xd5: // CMP $xx,X
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xd6: // DEC $xx,X
            this.opcode = Opcode.DEC;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0xd7: // DCP $xx,X
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0xd8: // CLD
            this.opcode = Opcode.CLD;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xd9: // CMP $xxxx,Y
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0xda: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xdb: // DCP $xxxx,Y
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0xdc: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xdd: // CMP $xxxx,X
            this.opcode = Opcode.CMP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xde: // DEC $xxxx,X
            this.opcode = Opcode.DEC;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0xdf: // DCP $xxxx,X
            this.opcode = Opcode.DCP;
            this.addrMode = AddrMode.ABSX_RMW;
            break;

        case 0xe0: // CPX #$xx
            this.opcode = Opcode.CPX;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xe1: // SBC ($xx,X)
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ZPIX_READ;
            break;
        case 0xe2: // NOP #$xx
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xe3: // ISC ($xx,X)
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ZPIX_RMW;
            break;
        case 0xe4: // CPX $xx
            this.opcode = Opcode.CPX;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xe5: // SBC $xx
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ZP_READ;
            break;
        case 0xe6: // INC $xx
            this.opcode = Opcode.INC;
            this.addrMode = AddrMode.ZP_RMW;
            break;
        case 0xe7: // ISC $xx
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ZP_RMW;
            break;

        case 0xe8: // INX
            this.opcode = Opcode.INX;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xe9: // SBC #$xx
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xea: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xeb: // SBC #$xx
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.IMMEDIATE;
            break;
        case 0xec: // CPX $xxxxx
            this.opcode = Opcode.CPX;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xed: // SBC $xxxxx
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ABS_READ;
            break;
        case 0xee: // INC $xxxxx
            this.opcode = Opcode.INC;
            this.addrMode = AddrMode.ABS_RMW;
            break;
        case 0xef: // ISC $xxxxx
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ABS_RMW;
            break;

        case 0xf0: // BEQ
            this.opcode = Opcode.BEQ;
            this.addrMode = AddrMode.REL;
            break;
        case 0xf1: // SBC ($xx),Y
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ZPIY_READ;
            break;
        case 0xf2: // KIL
            this.opcode = Opcode.KIL;
            this.addrMode = AddrMode.KIL;
            break;
        case 0xf3: // ISC ($xx),Y
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ZPIY_RMW;
            break;
        case 0xf4: // NOP $xx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xf5: // SBC $xx,X
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ZPX_READ;
            break;
        case 0xf6: // INC $xx,X
            this.opcode = Opcode.INC;
            this.addrMode = AddrMode.ZPX_RMW;
            break;
        case 0xf7: // ISC $xx,X
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ZPX_RMW;
            break;

        case 0xf8: // SED
            this.opcode = Opcode.SED;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xf9: // SBC $xxxx,Y
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ABSY_READ;
            break;
        case 0xfa: // NOP
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.NONE;
            break;
        case 0xfb: // ISC $xxxx,Y
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ABSY_RMW;
            break;
        case 0xfc: // NOP $xxxx,X
            this.opcode = Opcode.NOP;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xfd: // SBC $xxxx,X
            this.opcode = Opcode.SBC;
            this.addrMode = AddrMode.ABSX_READ;
            break;
        case 0xfe: // INC $xxxx,X
            this.opcode = Opcode.INC;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        case 0xff: // ISC $xxxx,X
            this.opcode = Opcode.ISC;
            this.addrMode = AddrMode.ABSX_RMW;
            break;
        }
        // */
        if (this.debug)
        {
            this.disCurrent();
        }
    }

    private void setFlagsNZ(final int v)
    {
        this.sr = (v & 0xff) == 0
                ? (this.sr & ~FLAG_N) | FLAG_Z
                : (this.sr & ~(FLAG_N | FLAG_Z)) | (v & FLAG_N);
    }

    private void setFlagC(final boolean v)
    {
        if (v)
        {
            this.sr |= FLAG_C;
        }
        else
        {
            this.sr &= ~FLAG_C;
        }
    }

    private void setFlagV(final boolean v)
    {
        if (v)
        {
            this.sr |= FLAG_V;
        }
        else
        {
            this.sr &= ~FLAG_V;
        }
    }

    private void setFlagZ(final boolean v)
    {
        if (v)
        {
            this.sr |= FLAG_Z;
        }
        else
        {
            this.sr &= ~FLAG_Z;
        }
    }

    private void opADC()
    {
        final int t16 = this.a + this.operand + (this.sr & FLAG_C);
        this.setFlagsNZ(t16 & 0xff);
        this.setFlagC(t16 > 0xff);
        this.setFlagV(((this.a ^ this.operand) & 0x80) == 0 && ((this.a ^ t16) & 0x80) != 0);
        this.a = t16 & 0xff;
    }

    private void opSBC()
    {
        final int t16 = this.a - this.operand - ((this.sr & FLAG_C) ^ FLAG_C);
        this.setFlagsNZ(t16 & 0xff);
        this.setFlagC(t16 >= 0);
        this.setFlagV(((this.a ^ this.operand) & 0x80) != 0 && ((this.a ^ t16) & 0x80) != 0);
        this.a = t16 & 0xff;
    }

    private void opBRA(final boolean flag)
    {
        if (flag)
        {
            this.pcl += (byte)this.operand;
            this.pcc = this.pcl < 0 ? -1 : this.pcl > 0xff ? 1 : 0;
            this.pcl &= 0xff;
            this.cycle++;
        }
        else
        {
            this.doIRQs();
            this.initOpcode();
        }
    }

    private void doOp()
    {
        int tmp;
        switch (this.opcode)
        {
        case NMI:
        case IRQ:
            break;
        case ADC:
            this.opADC();
            break;
        case AHX:
            this.operand = this.x & this.a & (this.addrh + 1);
            if (this.addrc)
            {
                this.addrh = this.operand;
            }
            break;
        case ALR:
            this.a &= this.operand;
            this.setFlagC((this.a & 1) != 0);
            this.a >>= 1;
            this.setFlagsNZ(this.a);
            break;
        case ANC:
            this.setFlagsNZ(this.a &= this.operand);
            this.setFlagC((this.sr & FLAG_N) != 0);
            break;
        case AND:
            this.setFlagsNZ(this.a &= this.operand);
            break;
        case ARR:
            tmp = this.operand & this.a;
            this.a = (tmp >> 1) | ((this.sr & FLAG_C) << 7);
            this.setFlagsNZ(this.a);
            this.setFlagC((this.a & 0x40) != 0);
            this.setFlagV(((this.a & 0x40) ^ ((this.a & 0x20) << 1)) != 0);
            break;
        case ASL:
            this.sr = (this.sr & ~FLAG_C) | (this.operand >> 7);
            this.setFlagsNZ(this.operand = ((this.operand << 1) & 0xff));
            break;
        case AXS:
            // x = (a & x) - imm, only update NZC, don't use C in sbc
            tmp = (this.a & this.x) - this.operand;
            this.setFlagsNZ(tmp);
            this.setFlagC(tmp >= 0);
            this.operand = this.a;
            this.x = tmp & 0xff;
            break;
        case BCC:
            this.opBRA((this.sr & FLAG_C) == 0);
            break;
        case BCS:
            this.opBRA((this.sr & FLAG_C) != 0);
            break;
        case BEQ:
            this.opBRA((this.sr & FLAG_Z) != 0);
            break;
        case BIT:
            this.setFlagZ((this.a & this.operand) == 0);
            this.sr = (this.sr & ~(FLAG_N | FLAG_V)) | (this.operand & (FLAG_N | FLAG_V));
            break;
        case BMI:
            this.opBRA((this.sr & FLAG_N) != 0);
            break;
        case BNE:
            this.opBRA((this.sr & FLAG_Z) == 0);
            break;
        case BPL:
            this.opBRA((this.sr & FLAG_N) == 0);
            break;
        case BRK:
            // TODO nothing to do here?
            break;
        case BVC:
            this.opBRA((this.sr & FLAG_V) == 0);
            break;
        case BVS:
            this.opBRA((this.sr & FLAG_V) != 0);
            break;
        case CLC:
            this.sr &= ~FLAG_C;
            break;
        case CLD:
            this.sr &= ~FLAG_D;
            break;
        case CLI:
            this.sr &= ~FLAG_I;
            break;
        case CLV:
            this.sr &= ~FLAG_V;
            break;
        case CMP:
            tmp = this.a - this.operand;
            this.setFlagC(tmp >= 0);
            this.setFlagsNZ(tmp);
            break;
        case CPX:
            tmp = this.x - this.operand;
            this.setFlagC(tmp >= 0);
            this.setFlagsNZ(tmp);
            break;
        case CPY:
            tmp = this.y - this.operand;
            this.setFlagC(tmp >= 0);
            this.setFlagsNZ(tmp);
            break;
        case DCP:
            this.operand = (this.operand - 1) & 0xff;
            tmp = this.a - this.operand;
            this.setFlagC(tmp >= 0);
            this.setFlagsNZ(tmp);
            break;
        case DEC:
            this.operand = (this.operand - 1) & 0xff;
            this.setFlagsNZ(this.operand);
            break;
        case DEX:
            this.x = (this.x - 1) & 0xff;
            this.setFlagsNZ(this.x);
            break;
        case DEY:
            this.y = (this.y - 1) & 0xff;
            this.setFlagsNZ(this.y);
            break;
        case EOR:
            this.a ^= this.operand;
            this.setFlagsNZ(this.a);
            break;
        case INC:
            this.operand = (this.operand + 1) & 0xff;
            this.setFlagsNZ(this.operand);
            break;
        case INX:
            this.x = (this.x + 1) & 0xff;
            this.setFlagsNZ(this.x);
            break;
        case INY:
            this.y = (this.y + 1) & 0xff;
            this.setFlagsNZ(this.y);
            break;
        case ISC:
            this.operand = (this.operand + 1) & 0xff;
            this.setFlagsNZ(this.operand);
            this.opSBC();
            break;
        case JMP:
            // TODO nothing to do here?
            break;
        case JSR:
            // TODO nothing to do here?
            break;
        case KIL:
            break;
        case LAS:
            this.a = this.x = this.sp = this.operand &= this.sp;
            this.setFlagsNZ(this.operand);
            break;
        case LAX:
            this.setFlagsNZ(this.a = this.x = this.operand);
            break;
        case LDA:
            this.setFlagsNZ(this.a = this.operand);
            break;
        case LDX:
            this.setFlagsNZ(this.x = this.operand);
            break;
        case LDY:
            this.setFlagsNZ(this.y = this.operand);
            break;
        case LSR:
            this.sr = (this.sr & ~FLAG_C) | (this.operand & FLAG_C);
            this.operand >>= 1;
            this.setFlagsNZ(this.operand);
            break;
        case NOP:
            break;
        case ORA:
            this.a |= this.operand;
            this.setFlagsNZ(this.a);
            break;
        case PHA:
            this.operand = this.a;
            break;
        case PHP:
            this.operand = this.sr | FLAG_B;
            break;
        case PLA:
            this.a = this.operand;
            this.setFlagsNZ(this.a);
            break;
        case PLP:
            this.sr = this.operand | 0x20;
            break;
        case RLA:
            this.operand = (this.operand << 1) | (this.sr & FLAG_C);
            this.sr = (this.sr & ~FLAG_C) | (this.operand >> 8);
            this.operand &= 0xff;
            this.a &= this.operand;
            this.setFlagsNZ(this.a);
            break;
        case ROL:
            this.operand = (this.operand << 1) | (this.sr & FLAG_C);
            this.sr = (this.sr & ~FLAG_C) | (this.operand >> 8);
            this.operand &= 0xff;
            this.setFlagsNZ(this.operand);
            break;
        case ROR:
            tmp = this.operand & FLAG_C;
            this.operand = (this.operand >> 1) | ((this.sr & FLAG_C) << 7);
            this.sr = (this.sr & ~FLAG_C) | tmp;
            this.setFlagsNZ(this.operand);
            break;
        case RRA:
            tmp = this.operand & FLAG_C;
            this.operand = (this.operand >> 1) | ((this.sr & FLAG_C) << 7);
            this.sr = (this.sr & ~FLAG_C) | tmp;
            this.opADC();
            break;
        case RTI:
            // TODO nothing to do here?
            break;
        case RTS:
            // TODO nothing to do here?
            break;
        case SAX:
            this.operand = this.a & this.x;
            break;
        case SBC:
            this.opSBC();
            break;
        case SEC:
            this.sr |= FLAG_C;
            break;
        case SED:
            this.sr |= FLAG_D;
            break;
        case SEI:
            this.sr |= FLAG_I;
            break;
        case SEV:
            this.sr |= FLAG_V;
            break;
        case SHX:
            this.operand = this.x & (this.addrh + 1);
            if (this.addrc)
            {
                this.addrh = this.operand;
            }
            break;
        case SHY:
            this.operand = this.y & (this.addrh + 1);
            if (this.addrc)
            {
                this.addrh = this.operand;
            }
            break;
        case SLO:
            this.sr = (this.sr & ~FLAG_C) | (this.operand >> 7);
            this.operand = (this.operand << 1) & 0xff;
            this.a |= this.operand;
            this.setFlagsNZ(this.a);
            break;
        case SRE:
            this.sr = (this.sr & ~FLAG_C) | (this.operand & FLAG_C);
            this.operand >>= 1;
            this.a ^= this.operand;
            this.setFlagsNZ(this.a);
            break;
        case STA:
            this.operand = this.a;
            break;
        case STX:
            this.operand = this.x;
            break;
        case STY:
            this.operand = this.y;
            break;
        case TAS:
            this.sp = this.a & this.x;
            this.operand = this.sp & (this.addrh + 1);
            if (this.addrc)
            {
                this.addrh = this.operand;
            }
            break;
        case TAX:
            this.setFlagsNZ(this.x = this.a);
            break;
        case TAY:
            this.setFlagsNZ(this.y = this.a);
            break;
        case TSX:
            this.setFlagsNZ(this.x = this.sp);
            break;
        case TXA:
            this.setFlagsNZ(this.a = this.x);
            break;
        case TXS:
            this.sp = this.x;
            break;
        case TYA:
            this.setFlagsNZ(this.a = this.y);
            break;
        case XAA:
            this.setFlagsNZ(this.a = 0xff & this.x & this.operand);
            break;
        }
    }

    private void doCycle2()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
            this.wasteRead();
            this.doIRQs();
            this.doOp();
            this.cycle = 0;
            break;
        case ACCU:
            this.wasteRead();
            this.operand = this.a;
            this.doOp();
            this.a = this.operand;
            this.doIRQs();
            this.cycle = 0;
            break;
        case IMMEDIATE:
            this.fetchOperand();
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case REL:
            this.fetchOperand();
            this.cycle++;
            break;
        case RTS:
        case RTI:
        case POP:
            this.wasteRead();
            this.cycle++;
            break;
        case PUSH:
        case NMI:
        case IRQ:
            this.wasteRead();
            this.cycle++;
            break;
        case BRK:
            this.wasteRead();
            this.incPC();
            this.cycle++;
            break;
        case JSR:
        case ABS_READ:
        case ABS_RMW:
        case ABS_WRITE:
        case ABSX_READ:
        case ABSX_RMW:
        case ABSX_WRITE:
        case ABSY_READ:
        case ABSY_RMW:
        case ABSY_WRITE:
        case ABS_JMP:
        case ZP_READ:
        case ZP_RMW:
        case ZPX_READ:
        case ZPX_RMW:
        case ZPX_WRITE:
        case ZPY_READ:
        case ZPY_RMW:
        case ZPY_WRITE:
        case ZPIX_READ:
        case ZPIX_RMW:
        case ZPIX_WRITE:
        case ZPIY_READ:
        case ZPIY_RMW:
        case ZPIY_WRITE:
        case ABS_IND:
            this.fetchAddrl();
            this.cycle++;
            break;
        case ZP_WRITE:
            this.fetchAddrl();
            this.cycle++;
            break;
        }
    }

    private void doCycle3()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
            // TODO illegal state
            break;
        case REL:
            this.doOp();
            break;
        case RTS:
        case RTI:
            this.incSP();
            this.cycle++;
            break;
        case PUSH:
            this.read = false;
            this.doOp();
            this.machine.write(0x100 | this.sp, this.operand);
            this.decSP();
            this.doIRQs();
            this.cycle = 0;
            break;
        case POP:
            this.incSP();
            this.cycle++;
            break;
        case NMI:
        case IRQ:
        case BRK:
            this.read = false;
            this.machine.write(0x100 | this.sp, this.pch);
            this.decSP();
            this.cycle++;
            break;
        case JSR:
            this.cycle++;
            break;
        case ABS_JMP:
            this.fetchAddrh();
            this.pcl = this.addrl;
            this.pch = this.addrh;
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABS_READ:
        case ABS_RMW:
        case ABS_IND:
            this.fetchAddrh();
            this.cycle++;
            break;
        case ABS_WRITE:
            this.fetchAddrh();
            this.cycle++;
            break;
        case ABSX_READ:
        case ABSX_RMW:
        case ABSX_WRITE:
            this.fetchAddrh();
            this.addAddr(this.x);
            this.cycle++;
            break;
        case ABSY_READ:
        case ABSY_RMW:
        case ABSY_WRITE:
            this.fetchAddrh();
            this.addAddr(this.y);
            this.cycle++;
            break;
        case ZP_READ:
            this.operand = this.machine.read(this.addrl);
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZP_RMW:
            this.operand = this.machine.read(this.addrl);
            this.cycle++;
            break;
        case ZP_WRITE:
            this.read = false;
            this.doOp();
            this.machine.write(this.addrl, this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPX_READ:
        case ZPX_RMW:
        case ZPIX_READ:
        case ZPIX_RMW:
        case ZPIX_WRITE:
            this.operand = this.machine.read(this.addrl);
            this.addrl = (this.addrl + this.x) & 0xff;
            this.cycle++;
            break;
        case ZPX_WRITE:
            this.operand = this.machine.read(this.addrl);
            this.addrl = (this.addrl + this.x) & 0xff;
            this.cycle++;
            break;
        case ZPY_READ:
        case ZPY_RMW:
            this.operand = this.machine.read(this.addrl);
            this.addrl = (this.addrl + this.y) & 0xff;
            this.cycle++;
            break;
        case ZPY_WRITE:
            this.operand = this.machine.read(this.addrl);
            this.addrl = (this.addrl + this.y) & 0xff;
            this.cycle++;
            break;
        case ZPIY_READ:
        case ZPIY_RMW:
        case ZPIY_WRITE:
            this.operand = this.addrl;
            this.addrl = this.machine.read(this.operand);
            this.cycle++;
            break;
        }
    }

    private void doCycle4()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
        case PUSH:
        case ABS_JMP:
        case ZP_READ:
        case ZP_WRITE:
            // TODO illegal state
            break;
        case REL:
            if (this.pcc != 0)
            {
                this.pch = (this.pch + this.pcc) & 0xff;
                this.pcc = 0;
                this.doIRQs();
                this.cycle = 0;
            }
            else
            {
                this.doIRQs();
                this.initOpcode();
            }
            break;
        case RTS:
            this.pcl = this.machine.read(0x100 | this.sp);
            this.incSP();
            this.cycle++;
            break;
        case RTI:
            this.sr = this.machine.read(0x100 | this.sp) | 0x20;
            this.incSP();
            this.cycle++;
            break;
        case POP:
            this.operand = this.machine.read(0x100 | this.sp);
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case NMI:
        case IRQ:
        case BRK:
            this.read = false;
            this.machine.write(0x100 | this.sp, this.pcl);
            this.decSP();
            this.cycle++;
            break;
        case JSR:
            this.read = false;
            this.machine.write(0x100 | this.sp, this.pch);
            this.decSP();
            this.cycle++;
            break;
        case ABS_READ:
            this.operand = this.machine.read(this.getAddr());
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABS_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.cycle++;
            break;
        case ABS_WRITE:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABSX_READ:
        case ABSY_READ:
            this.operand = this.machine.read(this.getAddr());
            if (this.fixAddr())
            {
                this.cycle++;
            }
            else
            {
                this.doOp();
                this.doIRQs();
                this.cycle = 0;
            }
            break;
        case ABSX_RMW:
        case ABSY_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.fixAddr();
            this.cycle++;
            break;
        case ABSX_WRITE:
        case ABSY_WRITE:
            this.operand = this.machine.read(this.getAddr());
            this.fixAddr();
            this.cycle++;
            break;
        case ABS_IND:
            this.pcl = this.machine.read(this.getAddr());
            this.addrl = (this.addrl + 1) & 0xff;
            this.cycle++;
            break;
        case ZP_RMW:
            this.read = false;
            this.machine.write(this.addrl, this.operand);
            this.cycle++;
            break;

        case ZPX_READ:
        case ZPY_READ:
            this.operand = this.machine.read(this.addrl);
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPX_RMW:
        case ZPY_RMW:
            this.operand = this.machine.read(this.addrl);
            this.cycle++;
            break;
        case ZPX_WRITE:
        case ZPY_WRITE:
            this.read = false;
            this.doOp();
            this.machine.write(this.addrl, this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPIX_READ:
        case ZPIX_RMW:
        case ZPIX_WRITE:
            this.operand = this.addrl;
            this.addrl = this.machine.read(this.operand);
            this.cycle++;
            break;
        case ZPIY_READ:
        case ZPIY_RMW:
        case ZPIY_WRITE:
            this.addrh = this.machine.read((this.operand + 1) & 0xff);
            this.addAddr(this.y);
            this.cycle++;
            break;
        }
    }

    private void doCycle5()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
        case PUSH:
        case ABS_JMP:
        case ZP_READ:
        case ZP_WRITE:
        case REL:
        case ABS_READ:
        case ABS_WRITE:
        case ZPX_READ:
        case ZPY_READ:
        case ZPX_WRITE:
        case ZPY_WRITE:
        case POP:
            // TODO illegal state
            break;

        case RTS:
            this.pch = this.machine.read(0x100 | this.sp);
            this.cycle++;
            break;
        case RTI:
            this.pcl = this.machine.read(0x100 | this.sp);
            this.incSP();
            this.cycle++;
            break;

        case NMI:
        case IRQ:
            this.read = false;
            this.sr &= ~FLAG_B;
            this.machine.write(0x100 | this.sp, this.sr);
            this.sr |= FLAG_I;
            this.decSP();
            this.cycle++;
            break;
        case BRK:
            this.read = false;
            this.sr |= FLAG_B;
            this.machine.write(0x100 | this.sp, this.sr);
            this.sr |= FLAG_I;
            this.decSP();
            this.cycle++;
            break;
        case JSR:
            this.read = false;
            this.machine.write(0x100 | this.sp, this.pcl);
            this.decSP();
            this.cycle++;
            break;
        case ABS_RMW:
            this.read = false;
            this.machine.write(this.getAddr(), this.operand);
            this.cycle++;
            break;
        case ABSX_READ:
        case ABSY_READ:
            this.operand = this.machine.read(this.getAddr());
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABSX_WRITE:
        case ABSY_WRITE:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABSX_RMW:
        case ABSY_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.cycle++;
            break;
        case ABS_IND:
            this.pch = this.machine.read(this.getAddr());
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZP_RMW:
            this.read = false;
            this.doOp();
            this.machine.write(this.addrl, this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPX_RMW:
        case ZPY_RMW:
            this.read = false;
            this.machine.write(this.addrl, this.operand);
            this.cycle++;
            break;
        case ZPIX_READ:
        case ZPIX_RMW:
            this.addrh = this.machine.read((this.operand + 1) & 0xff);
            this.cycle++;
            break;
        case ZPIX_WRITE:
            this.addrh = this.machine.read((this.operand + 1) & 0xff);
            this.cycle++;
            break;
        case ZPIY_READ:
            this.operand = this.machine.read(this.getAddr());
            if (this.fixAddr())
            {
                this.cycle++;
            }
            else
            {
                this.doOp();
                this.doIRQs();
                this.cycle = 0;
            }
            break;
        case ZPIY_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.fixAddr();
            this.cycle++;
            break;
        case ZPIY_WRITE:
            this.operand = this.machine.read(this.getAddr());
            this.fixAddr();
            this.cycle++;
            break;
        }
    }

    private void doCycle6()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
        case PUSH:
        case ABS_JMP:
        case ZP_READ:
        case ZP_WRITE:
        case REL:
        case ABS_READ:
        case ABS_WRITE:
        case ZPX_READ:
        case ZPY_READ:
        case ZPX_WRITE:
        case ZPY_WRITE:
        case POP:
        case ABSX_READ:
        case ABSY_READ:
        case ABSX_WRITE:
        case ABSY_WRITE:
        case ABS_IND:
        case ZP_RMW:
            // TODO illegal state
            break;

        case RTS:
            this.incPC();
            this.doIRQs();
            this.cycle = 0;
            break;
        case RTI:
            this.pch = this.machine.read(0x100 | this.sp);
            this.doIRQs();
            this.cycle = 0;
            break;

        case NMI:
            this.pcl = this.machine.read(0xfffa);
            this.cycle++;
            break;
        case IRQ:
        case BRK:
            this.pcl = this.machine.read(0xfffe);
            this.cycle++;
            break;
        case JSR:
            this.fetchAddrh();
            this.pcl = this.addrl;
            this.pch = this.addrh;
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABS_RMW:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABSX_RMW:
        case ABSY_RMW:
            this.read = false;
            this.machine.write(this.getAddr(), this.operand);
            this.cycle++;
            break;
        case ZPX_RMW:
        case ZPY_RMW:
        case ZPIX_WRITE:
        case ZPIY_WRITE:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPIX_READ:
            this.operand = this.machine.read(this.getAddr());
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPIX_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.cycle++;
            break;

        case ZPIY_READ:
            this.operand = this.machine.read(this.getAddr());
            this.doOp();
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPIY_RMW:
            this.operand = this.machine.read(this.getAddr());
            this.cycle++;
            break;
        }
    }

    private void doCycle7()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
        case PUSH:
        case ABS_JMP:
        case ZP_READ:
        case ZP_WRITE:
        case REL:
        case ABS_READ:
        case ABS_WRITE:
        case ZPX_READ:
        case ZPY_READ:
        case ZPX_WRITE:
        case ZPY_WRITE:
        case POP:
        case ABSX_READ:
        case ABSY_READ:
        case ABSX_WRITE:
        case ABSY_WRITE:
        case ABS_IND:
        case ZP_RMW:
        case RTS:
        case RTI:
        case JSR:
        case ABS_RMW:
        case ZPX_RMW:
        case ZPY_RMW:
        case ZPIX_WRITE:
        case ZPIY_WRITE:
        case ZPIX_READ:
        case ZPIY_READ:
            // TODO illegal state
            break;

        case NMI:
            this.pch = this.machine.read(0xfffb);
            this.doIRQs();
            this.cycle = 0;
            break;
        case IRQ:
        case BRK:
            this.pch = this.machine.read(0xffff);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ABSX_RMW:
        case ABSY_RMW:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        case ZPIX_RMW:
        case ZPIY_RMW:
            this.read = false;
            this.machine.write(this.getAddr(), this.operand);
            this.cycle++;
            break;
        }
    }

    private void doCycle8()
    {
        this.read = true;
        switch (this.addrMode)
        {
        case KIL:
            break;
        case NONE:
        case ACCU:
        case IMMEDIATE:
        case PUSH:
        case ABS_JMP:
        case ZP_READ:
        case ZP_WRITE:
        case REL:
        case ABS_READ:
        case ABS_WRITE:
        case ZPX_READ:
        case ZPY_READ:
        case ZPX_WRITE:
        case ZPY_WRITE:
        case POP:
        case ABSX_READ:
        case ABSY_READ:
        case ABSX_WRITE:
        case ABSY_WRITE:
        case ABS_IND:
        case ZP_RMW:
        case RTS:
        case RTI:
        case JSR:
        case ABS_RMW:
        case ZPX_RMW:
        case ZPY_RMW:
        case ZPIX_WRITE:
        case ZPIY_WRITE:
        case ZPIX_READ:
        case ZPIY_READ:
        case NMI:
        case IRQ:
        case BRK:
        case ABSX_RMW:
        case ABSY_RMW:
            // TODO illegal state
            break;

        case ZPIX_RMW:
        case ZPIY_RMW:
            this.read = false;
            this.doOp();
            this.machine.write(this.getAddr(), this.operand);
            this.doIRQs();
            this.cycle = 0;
            break;
        }
    }

    public void clock()
    {
        if (this.cycle == 0)
        {
            this.read = true;
        }

        // if(this.read && this.mmu.getBA())
        // return;

        switch (this.cycle)
        {
        case 0:
            this.initOpcode();
            break;
        case 1:
            this.doCycle2();
            break;
        case 2:
            this.doCycle3();
            break;
        case 3:
            this.doCycle4();
            break;
        case 4:
            this.doCycle5();
            break;
        case 5:
            this.doCycle6();
            break;
        case 6:
            this.doCycle7();
            break;
        case 7:
            this.doCycle8();
            break;
        }
    }

    private enum AddrMode
    {
        /** KIL */
        KIL,
        /** NMI */
        NMI,
        /** IRQ */
        IRQ,
        /** BRK */
        BRK,
        /** RTI */
        RTI,
        /** RTS */
        RTS,
        /** PUSH */
        PUSH,
        /** POP */
        POP,
        /** JSR */
        JSR,
        /** A */
        ACCU,
        /** None */
        NONE,
        /** #$xx */
        IMMEDIATE,
        /** JMP $xxxx */
        ABS_JMP,
        /** $xxxx */
        ABS_READ,
        /** $xxxx */
        ABS_RMW,
        /** $xxxx */
        ABS_WRITE,
        /** $xx */
        ZP_READ,
        /** $xx */
        ZP_RMW,
        /** $xx */
        ZP_WRITE,
        /** $xx,X */
        ZPX_READ,
        /** $xx,X */
        ZPX_RMW,
        /** $xx,X */
        ZPX_WRITE,
        /** $xx,Y */
        ZPY_READ,
        /** $xx,Y */
        ZPY_RMW,
        /** $xx,Y */
        ZPY_WRITE,
        /** $xxxx,X */
        ABSX_READ,
        /** $xxxx,X */
        ABSX_RMW,
        /** $xxxx,X */
        ABSX_WRITE,
        /** $xxxx,Y */
        ABSY_READ,
        /** $xxxx,Y */
        ABSY_RMW,
        /** $xxxx,Y */
        ABSY_WRITE,
        /** BRA */
        REL,
        /** ($xx,X) */
        ZPIX_READ,
        /** ($xx,X) */
        ZPIX_RMW,
        /** ($xx,X) */
        ZPIX_WRITE,
        /** ($xx),Y */
        ZPIY_READ,
        /** ($xx),Y */
        ZPIY_RMW,
        /** ($xx),Y */
        ZPIY_WRITE,
        /** ($xxxx) */
        ABS_IND
    }

    private enum Opcode
    {
        ADC,
        AHX,
        ALR,
        ANC,
        AND,
        ARR,
        ASL,
        AXS,
        BCC,
        BCS,
        BEQ,
        BIT,
        BMI,
        BNE,
        BPL,
        BRK,
        BVC,
        BVS,
        CLC,
        CLD,
        CLI,
        CLV,
        CMP,
        CPX,
        CPY,
        DCP,
        DEC,
        DEX,
        DEY,
        EOR,
        INC,
        INX,
        INY,
        IRQ,
        ISC,
        JMP,
        JSR,
        KIL,
        LAS,
        LAX,
        LDA,
        LDX,
        LDY,
        LSR,
        NMI,
        NOP,
        ORA,
        PHA,
        PHP,
        PLA,
        PLP,
        RLA,
        ROL,
        ROR,
        RRA,
        RTI,
        RTS,
        SAX,
        SBC,
        SEC,
        SED,
        SEI,
        SEV,
        SHX,
        SHY,
        SLO,
        SRE,
        STA,
        STX,
        STY,
        TAS,
        TAX,
        TAY,
        TSX,
        TXA,
        TXS,
        TYA,
        XAA
    }

    public void disCurrent()
    {
        // TODO this must be refactored to use non-interfering reads (use data
        // already read by pipeline)
        // TODO add illegal opcodes
        final int pc = this.getPC() - 1;
        if (pc == 0)
        {
            return;
        }

        final int opc = this.machine.read(pc);
        final int pb = this.machine.read((pc + 1) & 0xffff);
        final int pw = pb | (this.machine.read((pc + 2) & 0xffff) << 8);
        final int ba = pc + 2 + ((byte)pb);

        System.out.printf("S%3d C%3d %04X %02x : %02X %02X %02X %02X ", 0, 0, pc, opc, this.a, this.x, this.y, this.sp);
        System.out.print(this.irqLatch ? "+" : "-");
        System.out.print(this.irq ? "I" : "i");
        System.out.print(this.nmiLatch ? "+" : "-");
        System.out.print(this.nmi ? "N " : "n ");
        for (int i = 7; i >= 0; i--)
        {
            System.out.printf((this.sr & (1 << i)) != 0 ? "1" : "0");
        }
        System.out.printf(" ");

        switch (opc)
        {
        case 0x00: // BRK 7
            System.out.printf("BRK\n");
            break;
        case 0x01: // ORA ($xx,X) 6
            System.out.printf("ORA ($%02x,X)\n", pb);
            break;
        case 0x05: // ORA $xx 3
            System.out.printf("ORA $%02x\n", pb);
            break;
        case 0x06: // ASL $xx 5
            System.out.printf("ASL $%02x\n", pb);
            break;
        case 0x08: // PHP 3
            System.out.printf("PHP\n");
            break;
        case 0x09: // ORA #$xx 2
            System.out.printf("ORA #$%02x\n", pb);
            break;
        case 0x0a: // ASL A 2
            System.out.printf("ASL A\n");
            break;
        case 0x0b: // ANC #$xx 2
            System.out.printf("ANC #$%02x\n", pb);
            break;
        case 0x0d: // ORA $xxxx 4
            System.out.printf("ORA $%04x\n", pw);
            break;
        case 0x0e: // ASL $xxxx 6
            System.out.printf("ASL $%04x\n", pw);
            break;

        case 0x10: // BPL $xx 2
            System.out.printf("BPL $%04x\n", ba);
            break;
        case 0x11: // ORA ($xx),Y 5
            System.out.printf("ORA ($%02x),Y\n", pb);
            break;
        case 0x15: // ORA $xx,X 4
            System.out.printf("ORA $%02x,X\n", pb);
            break;
        case 0x16: // ASL $xx,X 6
            System.out.printf("ASL $%02x,X\n", pb);
            break;
        case 0x18: // CLC 2
            System.out.printf("CLC\n");
            break;
        case 0x19: // ORA $xxxx,Y 4
            System.out.printf("ORA $%04x,Y\n", pw);
            break;
        case 0x1d: // ORA $xxxx,X 4
            System.out.printf("ORA $%04x,X\n", pw);
            break;
        case 0x1e: // ASL $xxxx,X 7
            System.out.printf("ASL $%04x,X\n", pw);
            break;
        case 0x1f: // ASO $xxxx,X 7
            System.out.printf("ASO $%04x,X\n", pw);
            break;

        case 0x20: // JSR $xxxx 6
            System.out.printf("JSR $%04x\n", pw);
            break;
        case 0x21: // AND ($xx,X) 6
            System.out.printf("AND ($%02x,X)\n", pb);
            break;
        case 0x24: // BIT $xx 3
            System.out.printf("BIT $%02x\n", pb);
            break;
        case 0x25: // AND $xx 3
            System.out.printf("AND $%02x\n", pb);
            break;
        case 0x26: // ROL $xx 5
            System.out.printf("ROL $%02x\n", pb);
            break;
        case 0x28: // PLP 4
            System.out.printf("PLP\n");
            break;
        case 0x29: // AND #$xx 2
            System.out.printf("AND #$%02x\n", pb);
            break;
        case 0x2a: // ROL A 2
            System.out.printf("ROL A\n");
            break;
        case 0x2b: // ANC #$xx 2
            System.out.printf("ANC #$%02x\n", pb);
            break;
        case 0x2c: // BIT $xxxx 4
            System.out.printf("BIT $%04x\n", pw);
            break;
        case 0x2d: // AND $xxxx 4
            System.out.printf("AND $%04x\n", pw);
            break;
        case 0x2e: // ROL $xxxx 6
            System.out.printf("ROL $%04x\n", pw);
            break;

        case 0x30: // BMI $xx 2
            System.out.printf("BMI $%04x\n", ba);
            break;
        case 0x31: // AND ($xx),Y 5
            System.out.printf("AND ($%02x),Y\n", pb);
            break;
        case 0x35: // AND $xx,X 4
            System.out.printf("AND $%02x,X\n", pb);
            break;
        case 0x36: // ROL $xx,X 6
            System.out.printf("ROL $%02x,X\n", pb);
            break;
        case 0x38: // SEC 2
            System.out.printf("SEC\n");
            break;
        case 0x39: // AND $xxxx,Y 4
            System.out.printf("AND $%04x,Y\n", pw);
            break;
        case 0x3d: // AND $xxxx,X 4
            System.out.printf("AND $%04x,X\n", pw);
            break;
        case 0x3e: // ROL $xxxx,X 7
            System.out.printf("ROL $%04x,X\n", pw);
            break;

        case 0x40: // RTI 6
            System.out.printf("RTI\n");
            break;
        case 0x41: // EOR ($xx,X) 6
            System.out.printf("EOR ($%02x,X)\n", pb);
            break;
        case 0x45: // EOR $xx 3
            System.out.printf("EOR $%02x\n", pb);
            break;
        case 0x46: // LSR $xx 5
            System.out.printf("LSR $%02x\n", pb);
            break;
        case 0x48: // PHA 3
            System.out.printf("PHA\n");
            break;
        case 0x49: // EOR #$xx 2
            System.out.printf("EOR #$%02x\n", pb);
            break;
        case 0x4a: // LSR A 2
            System.out.printf("LSR A\n");
            break;
        case 0x4c: // JMP $xxxx 3
            System.out.printf("JMP $%04x\n", pw);
            break;
        case 0x4d: // EOR $xxxx 4
            System.out.printf("EOR $%04x\n", pw);
            break;
        case 0x4e: // LSR $xxxx 6
            System.out.printf("LSR $%04x\n", pw);
            break;

        case 0x50: // BVC $xx 2
            System.out.printf("BVC $%04x\n", ba);
            break;
        case 0x51: // EOR ($xx),Y 5
            System.out.printf("EOR ($%02x),Y\n", pb);
            break;
        case 0x55: // EOR $xx,X 4
            System.out.printf("EOR $%02x,X\n", pb);
            break;
        case 0x56: // LSR $xx,X 6
            System.out.printf("LSR $%02x,X\n", pb);
            break;
        case 0x58: // CLI 2
            System.out.printf("CLI\n");
            break;
        case 0x59: // EOR $xxxx,Y 4
            System.out.printf("EOR $%04x,Y\n", pw);
            break;
        case 0x5d: // EOR $xxxx,X 4
            System.out.printf("EOR $%04x,X\n", pw);
            break;
        case 0x5e: // LSR $xxxx,X 7
            System.out.printf("LSR $%04x,X\n", pw);
            break;

        case 0x60: // RTS 6
            System.out.printf("RTS\n");
            break;
        case 0x61: // ADC ($xx,X) 6
            System.out.printf("ADC ($%02x,X)\n", pb);
            break;
        case 0x65: // ADC $xx 3
            System.out.printf("ADC $%02x\n", pb);
            break;
        case 0x66: // ROR $xx 5
            System.out.printf("ROR $%02x\n", pb);
            break;
        case 0x68: // PLA 4
            System.out.printf("PLA\n");
            break;
        case 0x69: // ADC #$xx 2
            System.out.printf("ADC #$%02x\n", pb);
            break;
        case 0x6a: // ROR A 2
            System.out.printf("ROR A\n");
            break;
        case 0x6c: // JMP ($xxxx) 5
            System.out.printf("JMP ($%04x)\n", pw);
            break;
        case 0x6d: // ADC $xxxx 4
            System.out.printf("ADC $%04x\n", pw);
            break;
        case 0x6e: // ROR $xxxx 6
            System.out.printf("ROR $%04x\n", pw);
            break;

        case 0x70: // BVS $xx 2
            System.out.printf("BVS $%04x\n", ba);
            break;
        case 0x71: // ADC ($xx),Y 5
            System.out.printf("ADC ($%02x),Y\n", pb);
            break;
        case 0x75: // ADC $xx,X 4
            System.out.printf("ADC $%02x,X\n", pb);
            break;
        case 0x76: // ROR $xx,X 6
            System.out.printf("ROR $%02x,X\n", pb);
            break;
        case 0x78: // SEI 2
            System.out.printf("SEI\n");
            break;
        case 0x79: // ADC $xxxx,Y 4
            System.out.printf("ADC $%04x,Y\n", pw);
            break;
        case 0x7d: // ADC $xxxx,X 4
            System.out.printf("ADC $%04x,X\n", pw);
            break;
        case 0x7e: // ROR $xxxx,X 7
            System.out.printf("ROR $%04x,X\n", pw);
            break;

        case 0x81: // STA ($xx,X) 6
            System.out.printf("STA ($%02x,X)\n", pb);
            break;
        case 0x84: // STY $xx 3
            System.out.printf("STY $%02x\n", pb);
            break;
        case 0x85: // STA $xx 3
            System.out.printf("STA $%02x\n", pb);
            break;
        case 0x86: // STX $xx 3
            System.out.printf("STX $%02x\n", pb);
            break;
        case 0x88: // DEY 2
            System.out.printf("DEY\n");
            break;
        case 0x8a: // TXA 2
            System.out.printf("TXA\n");
            break;
        case 0x8c: // STY $xxxx 4
            System.out.printf("STY $%04x\n", pw);
            break;
        case 0x8d: // STA $xxxx 4
            System.out.printf("STA $%04x\n", pw);
            break;
        case 0x8e: // STX $xxxx 4
            System.out.printf("STX $%04x\n", pw);
            break;

        case 0x90: // BCC $xx 2
            System.out.printf("BCC $%04x\n", ba);
            break;
        case 0x91: // STA ($xx),Y 6
            System.out.printf("STA ($%02x),Y\n", pb);
            break;
        case 0x94: // STY $xx,X 4
            System.out.printf("STY $%02x,X\n", pb);
            break;
        case 0x95: // STA $xx,X 4
            System.out.printf("STA $%02x,X\n", pb);
            break;
        case 0x96: // STX $xx,Y 4
            System.out.printf("STX $%02x,Y\n", pb);
            break;
        case 0x98: // TYA 2
            System.out.printf("TYA\n");
            break;
        case 0x99: // STA $xxxx,Y 5
            System.out.printf("STA $%04x,Y\n", pw);
            break;
        case 0x9a: // TXS 2
            System.out.printf("TXS\n");
            break;
        case 0x9d: // STA $xxxx,X 5
            System.out.printf("STA $%04x,X\n", pw);
            break;

        case 0xa0: // LDY #$xx 2
            System.out.printf("LDY #$%02x\n", pb);
            break;
        case 0xa1: // LDA ($xx,X) 6
            System.out.printf("LDA ($%02x,X)\n", pb);
            break;
        case 0xa2: // LDX #$xx 2
            System.out.printf("LDX #$%02x\n", pb);
            break;
        case 0xa4: // LDY $xx 3
            System.out.printf("LDY $%02x\n", pb);
            break;
        case 0xa5: // LDA $xx 3
            System.out.printf("LDA $%02x\n", pb);
            break;
        case 0xa6: // LDX $xx 3
            System.out.printf("LDX $%02x\n", pb);
            break;
        case 0xa8: // TAY 2
            System.out.printf("TAY\n");
            break;
        case 0xa9: // LDA #$xx 2
            System.out.printf("LDA #$%02x\n", pb);
            break;
        case 0xaa: // TAX 2
            System.out.printf("TAX\n");
            break;
        case 0xac: // LDY $xxxx 4
            System.out.printf("LDY $%04x\n", pw);
            break;
        case 0xad: // LDA $xxxx 4
            System.out.printf("LDA $%04x\n", pw);
            break;
        case 0xae: // LDX $xxxx 4
            System.out.printf("LDX $%04x\n", pw);
            break;

        case 0xb0: // BCS $xx 2
            System.out.printf("BCS $%04x\n", ba);
            break;
        case 0xb1: // LDA ($xx),Y 5
            System.out.printf("LDA ($%02x),Y\n", pb);
            break;
        case 0xb4: // LDY $xx,X 4
            System.out.printf("LDY $%02x,X\n", pb);
            break;
        case 0xb5: // LDA $xx,X 4
            System.out.printf("LDA $%02x,X\n", pb);
            break;
        case 0xb6: // LDX $xx,Y 4
            System.out.printf("LDX $%02x,Y\n", pb);
            break;
        case 0xb8: // CLV 2
            System.out.printf("CLV\n");
            break;
        case 0xb9: // LDA $xxxx,Y 4
            System.out.printf("LDA $%04x,Y\n", pw);
            break;
        case 0xba: // TSX 2
            System.out.printf("TSX\n");
            break;
        case 0xbc: // LDY $xxxx,X 4
            System.out.printf("LDY $%04x,X\n", pw);
            break;
        case 0xbd: // LDA $xxxx,X 4
            System.out.printf("LDA $%04x,X\n", pw);
            break;
        case 0xbe: // LDX $xxxx,Y 4
            System.out.printf("LDX $%04x,Y\n", pw);
            break;

        case 0xc0: // CPY #$xx 2
            System.out.printf("CPY #$%02x\n", pb);
            break;
        case 0xc1: // CMP ($xx,X) 6
            System.out.printf("CMP ($%02x,X)\n", pb);
            break;
        case 0xc4: // CPY $xx 3
            System.out.printf("CPY $%02x\n", pb);
            break;
        case 0xc5: // CMP $xx 3
            System.out.printf("CMP $%02x\n", pb);
            break;
        case 0xc6: // DEC $xx 5
            System.out.printf("DEC $%02x\n", pb);
            break;
        case 0xc8: // INY 2
            System.out.printf("INY\n");
            break;
        case 0xc9: // CMP #$xx 2
            System.out.printf("CMP #$%02x\n", pb);
            break;
        case 0xca: // DEX 2
            System.out.printf("DEX\n");
            break;
        case 0xcc: // CPY $xxxx 4
            System.out.printf("CPY $%04x\n", pw);
            break;
        case 0xcd: // CMP $xxxx 4
            System.out.printf("CMP $%04x\n", pw);
            break;
        case 0xce: // DEC $xxxx 3
            System.out.printf("DEC $%04x\n", pw);
            break;

        case 0xd0: // BNE $xx 2
            System.out.printf("BNE $%04x\n", ba);
            break;
        case 0xd1: // CMP ($xx),Y 5
            System.out.printf("CMP ($%02x),Y\n", pb);
            break;
        case 0xd5: // CMP $xx,X 4
            System.out.printf("CMP $%02x,X\n", pb);
            break;
        case 0xd6: // DEC $xx,X 6
            System.out.printf("DEC $%02x,X\n", pb);
            break;
        case 0xd8: // CLD 2
            System.out.printf("CLD\n");
            break;
        case 0xd9: // CMP $xxxx,Y 4
            System.out.printf("CMP $%04x,Y\n", pw);
            break;
        case 0xdd: // CMP $xxxx,X 4
            System.out.printf("CMP $%04x,X\n", pw);
            break;
        case 0xde: // DEC $xxxx,X 7
            System.out.printf("DEC $%04x,X\n", pw);
            break;

        case 0xe0: // CPX #$xx 2
            System.out.printf("CPX #$%02x\n", pb);
            break;
        case 0xe1: // SBC ($xx,X) 6
            System.out.printf("SBC ($%02x,X)\n", pb);
            break;
        case 0xe4: // CPX $xx 3
            System.out.printf("CPX $%02x\n", pb);
            break;
        case 0xe5: // SBC $xx 3
            System.out.printf("SBC $%02x\n", pb);
            break;
        case 0xe6: // INC $xx 5
            System.out.printf("INC $%02x\n", pb);
            break;
        case 0xe8: // INX 2
            System.out.printf("INX\n");
            break;
        case 0xe9: // SBC #$xx 2
            System.out.printf("SBC #$%02x\n", pb);
            break;
        case 0xea: // NOP 2
            System.out.printf("NOP\n");
            break;
        case 0xec: // CPX $xxxx 4
            System.out.printf("CPX $%04x\n", pw);
            break;
        case 0xed: // SBC $xxxx 4
            System.out.printf("SBC $%04x\n", pw);
            break;
        case 0xee: // INC $xxxx 6
            System.out.printf("INC $%04x\n", pw);
            break;

        case 0xf0: // BEQ $xx 2
            System.out.printf("BEQ $%04x\n", ba);
            break;
        case 0xf1: // SBC ($xx),Y 5
            System.out.printf("SBC ($%02x),Y\n", pb);
            break;
        case 0xf5: // SBC $xx,X 4
            System.out.printf("SBC $%02x,X\n", pb);
            break;
        case 0xf6: // INC $xx,X 6
            System.out.printf("INC $%02x,X\n", pb);
            break;
        case 0xf8: // SED 2
            System.out.printf("SED\n");
            break;
        case 0xf9: // SBC $xxxx,Y 4
            System.out.printf("SBC $%04x,Y\n", pw);
            break;
        case 0xfd: // SBC $xxxx,X 4
            System.out.printf("SBC $%04x,X\n", pw);
            break;
        case 0xfe: // INC $xxxx,X 7
            System.out.printf("INC $%04x,X\n", pw);
            break;

        default:
            System.out.printf("*** ILL\n");
            break;
        }
    }
}
