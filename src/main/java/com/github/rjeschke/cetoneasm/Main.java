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

import java.util.List;

public class Main
{
    private static void reportTokenizerError(final TokenizerException te)
    {
        if (te.getCause() == null)
        {
            System.err.println("Tokenizer error at " + te.getLocation() + " -> " + te.getMessage());
        }
        else
        {
            System.err.println("Exception in " + te.getLocation() + " -> " + te.getMessage()
                    + " (" + te.getCause().getMessage() + ")");
            te.printStackTrace();
        }
        System.exit(1);
    }

    private static void reportAssemblerError(final AssemblerException ae)
    {
        if (ae.getCause() == null)
        {
            System.err.println("Error at " + ae.getLocation() + " -> " + ae.getMessage());
        }
        else
        {
            if (ae.getCause() instanceof TokenizerException)
            {
                reportTokenizerError((TokenizerException)ae.getCause());
            }
            System.err.println("Exception in " + ae.getLocation() + " -> " + ae.getMessage()
                    + " (" + ae.getCause().getMessage() + ")");
            ae.printStackTrace();
        }
        System.exit(2);
    }

    public static void main(final String[] args) throws TokenizerException
    {
        final Config config = new Config();
        final Tokenizer tok = new Tokenizer(config, "/home/rjeschke/Dropbox/testing.casm");
        try
        {
            tok.open();
            final List<Action> actions = Parser.parse(tok);
        }
        catch (final TokenizerException te)
        {
            reportTokenizerError(te);
        }
        catch (final AssemblerException ae)
        {
            reportAssemblerError(ae);
        }
        System.exit(0);
    }
}
