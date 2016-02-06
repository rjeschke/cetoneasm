package com.github.rjeschke.cetoneasm;

import com.github.rjeschke.cetoneasm.Tokenizer.Token;
import com.github.rjeschke.cetoneasm.Tokenizer.TokenizerException;

public class Main
{
    public static void main(final String[] args) throws TokenizerException
    {
        final Config config = new Config();
        final Tokenizer tok = new Tokenizer(config, "/home/rjeschke/Dropbox/testing.casm");
        try
        {
            tok.open();
            for (;;)
            {
                final Tokenizer.Token token = tok.read();
                System.out.println(token);
                if (token.getType() == Token.Type.EOF)
                {
                    break;
                }
            }
        }
        catch (final TokenizerException e)
        {
            System.out.println("Exception in " + e.getLocation() + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }
}
