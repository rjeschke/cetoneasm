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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.rjeschke.cetoneasm.Token.Type;

public class Tokenizer
{
    private final String        filename;
    private int                 lineNo     = 1;
    private BufferedReader      reader;
    private final Config        config;
    private final int[]         lookAhead  = new int[16];
    private int                 aheadRead  = 0;
    private int                 aheadWrite = 0;
    private int                 aheadAvail = 0;
    private final StringBuilder sb         = new StringBuilder();

    public Tokenizer(final Config config, final String filename)
    {
        this.config = config;
        this.filename = filename;
    }

    public void open(final InputStream instream) throws TokenizerException
    {
        try
        {
            this.reader = new BufferedReader(new InputStreamReader(instream, this.config.fileEncoding));
            this.fill();
        }
        catch (final IOException e)
        {
            throw new TokenizerException(this, "Failed to open stream", e);
        }
    }

    public Tokenizer open() throws TokenizerException
    {
        try
        {
            this.open(new FileInputStream(this.filename));
        }
        catch (final IOException e)
        {
            throw new TokenizerException(this, "Failed to open file '" + this.filename + "'", e);
        }
        return this;
    }

    public void close()
    {
        try
        {
            this.reader.close();
        }
        catch (final Throwable t)
        {
            // ignored
        }
    }

    private Token readString(final boolean isScr) throws TokenizerException, IOException
    {
        this.sb.setLength(0);
        this.consume();
        while (this.peek() != '"' && this.peek() != -1)
        {
            if (this.peek() == '\\')
            {
                switch (this.peek(1))
                {
                case 'r':
                    this.sb.append('\r');
                    break;
                case 'n':
                    this.sb.append('\n');
                    break;
                case 't':
                    this.sb.append('\t');
                    break;
                case '\\':
                    this.sb.append('\\');
                    break;
                case '"':
                    this.sb.append('"');
                    break;
                default:
                    throw new TokenizerException(this, "Illegal escape sequence '\\" + ((char)this.peek(1)) + "'");
                }
                this.consume(2);
            }
            else
            {
                this.sb.append((char)this.consume());
            }
        }
        if (this.peek() == -1)
        {
            throw new TokenizerException(this, "Open string");
        }
        this.consume();
        return isScr ? Token.tSCR_STRING(this, this.sb.toString()) : Token.tSTRING(this, this.sb.toString());
    }

    private Token readWord(final Token.Type type) throws TokenizerException, IOException
    {
        this.sb.setLength(0);
        switch (type)
        {
        case META:
            this.consume();
            break;
        default:
            break;
        }
        for (;;)
        {
            final int ch = this.peek();
            if (Character.isLetterOrDigit((char)ch) || ch == '_')
            {
                this.sb.append((char)ch);
            }
            else
            {
                break;
            }
            this.consume();
        }

        final String str = this.sb.toString().toUpperCase();

        if (type == Type.META)
        {
            if (str.isEmpty())
            {
                throw new TokenizerException(this, "Empty META");
            }
            return Token.tMETA(this, str);
        }
        if (this.peek() == ':')
        {
            this.consume();
            return str.charAt(0) == '_' ? Token.tSUB_LABEL(this, str) : Token.tLABEL(this, str);
        }
        if (Opcodes.BY_NAME.containsKey(str))
        {
            return Token.tOPCODE(this, str);
        }
        if (str.startsWith("__"))
        {
            throw new TokenizerException(this, "Usage of '__' variables is forbidden");
        }
        return str.charAt(0) == '_' ? Token.tSUB_WORD(this, str) : Token.tWORD(this, str);
    }

    private static boolean isBaseNChar(final int ch, final int base)
    {
        if (base <= 10)
        {
            return ch >= '0' && ch <= ('0' + base - 1);
        }
        if (Character.isLetter((char)ch))
        {
            final int test = Character.toLowerCase((char)ch) - 'a';
            return test >= 0 && test <= (base - 11);
        }
        return Character.isDigit((char)ch);
    }

    private Token readNumber() throws IOException, TokenizerException
    {
        int base = 10;
        if (this.peek() == '$')
        {
            this.consume();
            base = 16;
        }
        else
        {
            if (this.peek() == '0' && Character.isLetter((char)this.peek(1)))
            {
                switch (this.peek(1))
                {
                case 'x':
                case 'X':
                    base = 16;
                    this.consume(2);
                    break;
                case 'o':
                case 'O':
                    base = 8;
                    this.consume(2);
                    break;
                case 'b':
                case 'B':
                    base = 2;
                    this.consume(2);
                    break;
                }
            }
        }
        this.sb.setLength(0);
        while (isBaseNChar(this.peek(), base))
        {
            this.sb.append((char)this.consume());
        }
        if (this.sb.length() == 0)
        {
            throw new TokenizerException(this, "Syntax error, number format");
        }
        return Token.tNUMBER(this, Integer.parseInt(this.sb.toString(), base));
    }

    public FileLocation getLocation()
    {
        return new FileLocation(this.filename, this.lineNo);
    }

    private void fill() throws IOException
    {
        for (int i = 0; i < this.lookAhead.length - this.aheadAvail; i++)
        {
            this.lookAhead[this.aheadWrite] = this.reader.read();
            this.aheadWrite = (this.aheadWrite + 1) % this.lookAhead.length;
        }
        this.aheadAvail = this.lookAhead.length;
    }

    private int peek(final int offset) throws IOException, TokenizerException
    {
        if (offset > this.lookAhead.length)
        {
            throw new TokenizerException(this, "Internal error, offset > lookAhead.length");
        }
        if (offset >= this.aheadAvail)
        {
            this.fill();
        }
        return this.lookAhead[(this.aheadRead + offset) % this.lookAhead.length];
    }

    private int peek() throws IOException, TokenizerException
    {
        return this.peek(0);
    }

    private int consume() throws TokenizerException, IOException
    {
        final int ch = this.peek();
        this.consume(1);
        return ch;
    }

    private void consume(final int amount) throws TokenizerException
    {
        this.aheadRead = (this.aheadRead + amount) % this.lookAhead.length;
        this.aheadAvail -= amount;
        if (this.aheadAvail < 0)
        {
            throw new TokenizerException(this, "Illegal internal state, aheadAvail < 0");
        }
    }

    public Token read() throws TokenizerException
    {
        try
        {
            for (;;)
            {
                switch (this.peek())
                {
                case -1:
                    return Token.tGeneric(this, Type.EOF);
                case '\n':
                    this.lineNo++;
                    //$FALL-THROUGH$
                case ' ':
                    //$FALL-THROUGH$
                case '\t':
                    //$FALL-THROUGH$
                case '\r':
                    this.consume();
                    continue;
                case ';':
                    while (this.peek() != '\n' && this.peek() != -1)
                    {
                        this.consume();
                    }
                    continue;
                case '.':
                    return this.readWord(Type.META);
                case '_':
                    return this.readWord(Type.WORD);
                case '@':
                    this.consume();
                    return Token.tGeneric(this, Type.PC);
                case '=':
                    this.consume();
                    if (this.peek() == '=')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.EQUAL);
                    }
                    return Token.tGeneric(this, Type.ASSIGN);
                case '#':
                    this.consume();
                    return Token.tGeneric(this, Type.IMMEDIATE);
                case '+':
                    this.consume();
                    return Token.tGeneric(this, Type.PLUS);
                case '-':
                    this.consume();
                    return Token.tGeneric(this, Type.MINUS);
                case '*':
                    this.consume();
                    return Token.tGeneric(this, Type.MUL);
                case '/':
                    this.consume();
                    return Token.tGeneric(this, Type.DIV);
                case '&':
                    this.consume();
                    return Token.tGeneric(this, Type.AND);
                case '|':
                    this.consume();
                    return Token.tGeneric(this, Type.OR);
                case '^':
                    this.consume();
                    return Token.tGeneric(this, Type.XOR);
                case '(':
                    this.consume();
                    return Token.tGeneric(this, Type.BRACE_OPEN);
                case ')':
                    this.consume();
                    return Token.tGeneric(this, Type.BRACE_CLOSE);
                case ',':
                    this.consume();
                    return Token.tGeneric(this, Type.COMMA);
                case '!':
                    this.consume();
                    if (this.peek() == '=')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.NOT_EQUAL);
                    }
                    return Token.tGeneric(this, Type.NOT);
                case '~':
                    this.consume();
                    return Token.tGeneric(this, Type.NEG);
                case '<':
                    this.consume();
                    if (this.peek() == '<')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.SHIFT_LEFT);
                    }
                    if (this.peek() == '=')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.LESS_EQUAL);
                    }
                    return Token.tGeneric(this, Type.LESS_THAN);
                case '>':
                    this.consume();
                    if (this.peek() == '>')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.SHIFT_RIGHT);
                    }
                    if (this.peek() == '=')
                    {
                        this.consume();
                        return Token.tGeneric(this, Type.GREATER_EQUAL);
                    }
                    return Token.tGeneric(this, Type.GREATER_THAN);
                case '$':
                    return this.readNumber();
                case '"':
                    return this.readString(false);
                default:
                    if (Character.isLetter((char)this.peek()))
                    {
                        if (this.peek(1) == '"')
                        {
                            switch (Character.toLowerCase((char)this.peek()))
                            {
                            case 'a':
                                this.consume();
                                return this.readString(false);
                            case 's':
                                this.consume();
                                return this.readString(true);
                            }
                        }
                        return this.readWord(Type.WORD);
                    }
                    if (Character.isDigit((char)this.peek()))
                    {
                        return this.readNumber();
                    }
                    throw new TokenizerException(this, "Illegal character '" + ((char)this.peek()) + "'");
                }
            }
        }
        catch (final IOException e)
        {
            throw new TokenizerException(this, "IO error", e);
        }
    }
}
