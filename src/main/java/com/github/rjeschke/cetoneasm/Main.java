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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Main
{
    private static String getStackTrace(final Throwable t)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintWriter w = new PrintWriter(out, true);
        t.printStackTrace(w);
        try
        {
            return new String(out.toByteArray(), "UTF-8");
        }
        catch (final UnsupportedEncodingException e)
        {
            return "";
        }
    }

    private static void reportTokenizerError(final TokenizerException te)
    {
        if (te.getCause() == null)
        {
            Con.error(te.getLocation() + ": " + te.getMessage());
        }
        else
        {
            Con.error("Exception in " + te.getLocation() + " -> " + te.getMessage()
                    + " (" + te.getCause().getMessage() + ")");
            Con.error(getStackTrace(te));
        }
        System.exit(1);
    }

    private static void reportLinkerError(final LinkerException le)
    {
        if (le.getCause() == null)
        {
            Con.error(le.getMessage());
        }
        else
        {
            Con.error("Exception -> " + le.getMessage() + " (" + le.getCause().getMessage() + ")");
            Con.error(getStackTrace(le));
        }
        System.exit(3);
    }

    private static void reportAssemblerError(final AssemblerException ae)
    {
        if (ae.getCause() == null)
        {
            Con.error(ae.getLocation() + ": " + ae.getMessage());
        }
        else
        {
            if (ae.getCause() instanceof TokenizerException)
            {
                reportTokenizerError((TokenizerException)ae.getCause());
            }
            Con.error("Exception in " + ae.getLocation() + " -> " + ae.getMessage()
                    + " (" + ae.getCause().getMessage() + ")");
            Con.error(getStackTrace(ae));
        }
        System.exit(2);
    }

    public static void main(final String[] args) throws TokenizerException
    {
        Con.initialize();
        Con.info("cetoneasm v1.0, (c) 2016 René 'Neotec/Cetone' Jeschke");
        Con.info("--------------");
        final Config config = new Config();
        final Assembler rt = new Assembler();
        final Tokenizer tok = new Tokenizer(config, "/home/rjeschke/Dropbox/testing.casm");
        try
        {
            tok.open();
            Con.info("Parsing");
            final List<Action> actions = Parser.parse(tok);
            System.out.println(actions);
            Con.info("Generating code");
            final List<CodeContainer> containers = rt.compile(config, actions);
            if (containers.isEmpty())
            {
                Con.warn(" No code generated");
            }
            else
            {
                int codeSize = 0;
                int dataSize = 0;
                for (final CodeContainer cc : containers)
                {
                    if (cc.isDataContainer())
                    {
                        dataSize += cc.getSize();
                    }
                    else
                    {
                        codeSize += cc.getSize();
                    }
                }
                Con.info("Generating disassembly");
                final StringBuilder disasm = new StringBuilder();
                for (final CodeContainer cc : containers)
                {
                    disasm.append(cc.toString());
                }
                Con.info("Linking");
                final byte[] prg = Linker.link(config, containers);
                Con.info(" Code size: $%1$04x(%1$d) bytes", codeSize);
                Con.info(" Data size: $%1$04x(%1$d) bytes", dataSize);
                Con.info(" Padding:   $%1$04x(%1$d) bytes", prg.length - 2 - codeSize - dataSize);
                Con.info(" PRG start: $%04x", containers.get(0).getStartAddress());
                Con.info(" PRG end:   $%04x", containers.get(containers.size() - 1).getEndAddress() - 1);
                Con.info(" PRG size:  $%1$04x(%1$d) bytes, %2$d blocks", prg.length, (prg.length + 253) / 254);
                Con.info("Disassembly");
                Con.info(disasm.toString());
            }
        }
        catch (final TokenizerException te)
        {
            reportTokenizerError(te);
        }
        catch (final AssemblerException ae)
        {
            reportAssemblerError(ae);
        }
        catch (final LinkerException le)
        {
            reportLinkerError(le);
        }
        Con.info("READY.");
        System.exit(0);
    }
}
