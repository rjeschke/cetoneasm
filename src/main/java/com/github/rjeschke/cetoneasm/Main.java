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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.rjeschke.cetoneasm.emu.Machine;
import com.github.rjeschke.neetutils.cmd.CmdLineParser;
import com.github.rjeschke.neetutils.collections.Colls;

public class Main
{
    private static void exit(final int code)
    {
        if (code == 0)
        {
            Con.info("READY.");
        }
        else
        {
            Con.error("FAILED.");
        }
        System.exit(code);
    }

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
        exit(1);
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
        exit(3);
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
            else if (ae.getCause() instanceof AssemblerException)
            {
                reportAssemblerError(((AssemblerException)ae.getCause()));
            }
            Con.error("Exception in " + ae.getLocation() + " -> " + ae.getMessage()
                    + " (" + ae.getCause().getMessage() + ")");
            Con.error(getStackTrace(ae));
        }
        exit(2);
    }

    private static void startExternalEmulator(final String emulator, final List<String> commandLine, final String prg)
    {
        final ArrayList<String> command = new ArrayList<String>();
        command.add(emulator);
        command.addAll(commandLine);
        command.add(prg);
        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        try
        {
            pb.start();
        }
        catch (final IOException e)
        {
            Con.error("Failed to start emulator: " + e.getMessage());
        }
    }

    private static void writeString(final String filename, final String str)
    {
        try
        {
            final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
            try
            {
                w.write(str);
            }
            finally
            {
                w.close();
            }
        }
        catch (final IOException e)
        {
            Con.error("Could not write to file '%s'", filename);
            Con.error(getStackTrace(e));
            exit(10);
        }
    }

    private static void writeBinary(final String filename, final byte[] bytes)
    {
        try
        {
            final FileOutputStream out = new FileOutputStream(filename);
            try
            {
                out.write(bytes);
            }
            finally
            {
                out.close();
            }
        }
        catch (final IOException e)
        {
            Con.error("Could not write to file '%s'", filename);
            Con.error(getStackTrace(e));
            exit(11);
        }
    }

    public static void printHelp()
    {
        try
        {
            Con.info("Usage: casm [options] file...");
            Con.info(CmdLineParser.generateHelp(80, true, new Config()));
        }
        catch (final IOException e)
        {
            Con.error("Cmdline error: " + e.getMessage() + "\n");
            exit(5);
        }
    }

    public static void main(final String[] args) throws TokenizerException
    {
        Con.initialize();
        Con.info("cetoneasm v1.0, (c) 2016 René 'Neotec/Cetone' Jeschke");
        Con.info("-----------------------------------------------------");

        final Config config = new Config();
        List<String> rest = null;
        try
        {
            rest = CmdLineParser.parse(args, config);
        }
        catch (final IOException e)
        {
            Con.error(e.getMessage() + "\n");
            printHelp();
            exit(5);
        }

        if (config.printHelp)
        {
            printHelp();
            exit(0);
        }

        if (rest == null || rest.isEmpty())
        {
            Con.error("Missing input file(s)\n");
            printHelp();
            exit(6);
        }

        if (config.outputFile.isEmpty())
        {
            config.outputFile = new File("a.prg").getAbsolutePath();
        }
        else
        {
            config.outputFile = U.addPrgFileExtension(config.outputFile);
        }

        if (config.xverboseEmulation)
        {
            config.verboseEmulation = true;
        }

        if (config.verboseEmulation)
        {
            config.emulate = true;
        }

        CounterState.get().reset();
        final Assembler assembler = new Assembler(config);

        final List<Action> actions = Colls.list();

        for (final String file : rest)
        {
            final String inputFile = U.normalizePath(U.addCasmFileExtension(new File(file).getAbsolutePath()));
            final Tokenizer tok = new Tokenizer(config, inputFile);
            try
            {
                tok.open();
                assembler.addIncludeFromFilename(inputFile);
                Con.info("Parsing '%s'...", inputFile);
                actions.addAll(Parser.parse(tok));
            }
            catch (final TokenizerException te)
            {
                reportTokenizerError(te);
            }
            catch (final AssemblerException ae)
            {
                reportAssemblerError(ae);
            }
            finally
            {
                tok.close();
            }
        }

        try
        {
            Con.info("Generating code");
            final List<CodeContainer> containers = assembler.assemble(config, actions);
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
                if (config.createDisassembly)
                {
                    Con.info("Generating disassembly");
                    final HashMap<Integer, String> labelMap = assembler.getLabelMap();
                    final StringBuilder disasm = new StringBuilder();
                    for (final CodeContainer cc : containers)
                    {
                        disasm.append(cc.toString(labelMap));
                        disasm.append('\n');
                    }
                    final String disFile = U.replaceExtension(config.outputFile, ".disasm");
                    Con.info(" Writing to '%s'", disFile);
                    writeString(disFile, disasm.toString());
                }
                Con.info("Linking");
                final byte[] prg = Linker.link(config, containers);
                Con.info(" Writing to '%s'", config.outputFile);
                writeBinary(config.outputFile, prg);
                Con.info(" Code size: $%1$04x(%1$d) bytes", codeSize);
                Con.info(" Data size: $%1$04x(%1$d) bytes", dataSize);
                Con.info(" Padding:   $%1$04x(%1$d) bytes", prg.length - 2 - codeSize - dataSize);
                Con.info(" PRG start: $%04x", containers.get(0).getStartAddress());
                Con.info(" PRG end:   $%04x", containers.get(containers.size() - 1).getEndAddress() - 1);
                Con.info(" PRG size:  $%1$04x(%1$d) bytes, %2$d blocks", prg.length, (prg.length + 253) / 254);

                if (config.emulate)
                {
                    Con.info("Starting emulation (abort with RETURN)");
                    final Machine machine = new Machine();
                    machine.reset();
                    final int loadAddress = machine.loadPrg(prg);
                    machine.execute(loadAddress, config.verboseEmulation, config.xverboseEmulation);
                    Con.info("Finished emulation");
                }

                if (config.runExternalEmulator)
                {
                    Con.info("Starting external emulator");
                    startExternalEmulator("/usr/bin/x64", new ArrayList<String>(), config.outputFile);
                }
            }
        }
        catch (final AssemblerException ae)
        {
            reportAssemblerError(ae);
        }
        catch (final LinkerException le)
        {
            reportLinkerError(le);
        }
        exit(0);
    }
}
