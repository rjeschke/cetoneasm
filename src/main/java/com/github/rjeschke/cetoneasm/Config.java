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

import com.github.rjeschke.neetutils.cmd.CmdArgument;

public class Config
{
    @CmdArgument(l = "help", s = 'h', isSwitch = true, desc = "Prints a summary of command line arguments.")
    public boolean printHelp           = false;

    @CmdArgument(l = "file-encoding", printDefault = true, desc = "Sets the file encoding for source files.")
    public String  fileEncoding        = "UTF-8";

    @CmdArgument(s = 'o', l = "output", printDefault = false, desc = "Specifies the output file name.")
    public String  outputFile          = "";

    @CmdArgument(s = 'd', l = "disassembly", isSwitch = true, printDefault = false, desc = "Generates a disassembly listing after assembly.")
    public boolean createDisassembly   = false;

    @CmdArgument(s = 'e', l = "emulate", isSwitch = true, printDefault = false, desc = "Execute the resulting binary in the 6510 emulator.")
    public boolean emulate             = false;

    @CmdArgument(l = "emulate-verbose", isSwitch = true, printDefault = false, desc = "Prints exhaustive runtime information during emulation, includes --emulate.")
    public boolean verboseEmulation    = false;

    @CmdArgument(l = "emulate-xverbose", isSwitch = true, printDefault = false, desc = "Also prints CPU reads @PC, includes --emulate-verbose.")
    public boolean xverboseEmulation   = false;

    @CmdArgument(s = 'x', l = "execute", isSwitch = true, printDefault = false, desc = "Runs the generated PRG inside an external emulator (currently VICE/x64).")
    public boolean runExternalEmulator = false;
}
