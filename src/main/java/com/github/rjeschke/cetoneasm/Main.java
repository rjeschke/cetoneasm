package com.github.rjeschke.cetoneasm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Main
{
    public static class Opcode
    {
        public final String          mnemonic;
        public final int             value;
        public final AddressingModes adressingMode;

        public Opcode(final String mnemonic, final int value, final AddressingModes adressingMode)
        {
            this.mnemonic = mnemonic;
            this.value = value;
            this.adressingMode = adressingMode;
        }

        @Override
        public String toString()
        {
            return String.format("%02X %s %s", this.value, this.mnemonic, this.adressingMode);
        }
    }

    public static void main(final String[] args) throws IOException
    {
        final BufferedReader r = new BufferedReader(
                new InputStreamReader(
                        Main.class.getResourceAsStream("/com/github/rjeschke/cetoneasm/opcodes.txt")));

        final String[] addrIn = { "", "rel", "imm", "abs", "abx", "aby", "zp", "zpx", "zpy", "ind", "izy", "izx" };
        final AddressingModes[] modes = {
                AddressingModes.IMPLIED,
                AddressingModes.RELATIVE,
                AddressingModes.IMMEDIATE,
                AddressingModes.ABSOLUTE,
                AddressingModes.ABSOLUTE_X,
                AddressingModes.ABSOLUTE_Y,
                AddressingModes.ZEROPAGE,
                AddressingModes.ZEROPAGE_X,
                AddressingModes.ZEROPAGE_Y,
                AddressingModes.INDIRECT,
                AddressingModes.INDEXED_INDIRECT,
                AddressingModes.INDIRECT_INDEXED };



        final HashMap<String, ArrayList<String>> opcodes = new HashMap<String, ArrayList<String>>();
        final int op = 0;

        for (String line = r.readLine(); line != null; line = r.readLine())
        {
            final String[] ts = line.trim().split(" ");
            final int opcode = Integer.parseInt(ts[0], 16);
            final String mn = ts[1].toUpperCase();

            if (opcodes.containsKey(mn))
            {
                opcodes.get(mn).add(oper);
            }
            else
            {
                final ArrayList<String> list = new ArrayList<String>();
                list.add(oper);
                opcodes.put(mn, list);
            }
        }
    }
}
