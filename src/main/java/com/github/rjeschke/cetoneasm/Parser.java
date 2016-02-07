package com.github.rjeschke.cetoneasm;

import java.util.ArrayList;
import java.util.List;

public class Parser
{
    private final List<Token>  tokens;
    private final List<Action> actions  = new ArrayList<Action>();
    private int                position = 0;

    private Parser(final List<Token> tokens)
    {
        this.tokens = tokens;
    }

    public static List<Action> parse(final Tokenizer tokenizer) throws TokenizerException, AssemblerException
    {
        final ArrayList<Token> tokens = new ArrayList<Token>();
        for (;;)
        {
            final Token t = tokenizer.read();
            tokens.add(t);
            if (t.getType() == Token.Type.EOF)
            {
                break;
            }
        }
        return parse(tokens);
    }

    public static List<Action> parse(final List<Token> tokens) throws AssemblerException
    {
        return new Parser(tokens).parse();
    }

    private Token peek(final int offset)
    {
        return this.tokens.get(Math.min(this.position + offset, this.tokens.size() - 1));
    }

    private Token peek()
    {
        return this.peek(0);
    }

    private Token consume()
    {
        final Token token = this.peek();
        this.position++;
        return token;
    }

    private void consume(final int amount)
    {
        this.position += amount;
    }

    private List<Action> parse() throws AssemblerException
    {
        while (this.position < this.tokens.size())
        {
            System.out.println(this.peek());
            this.consume();
        }
        return this.actions;
    }
}
