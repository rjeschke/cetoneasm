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

public class Token
{
    public enum Type
    {
        EOF,
        META,
        WORD,
        SUB_WORD,
        OPCODE,
        LABEL,
        SUB_LABEL,
        STRING,
        SCR_STRING,
        PC,
        IMMEDIATE,
        NUMBER,
        ASSIGN,
        EQUAL,
        PLUS,
        MINUS,
        MUL,
        DIV,
        AND,
        OR,
        XOR,
        NOT,
        NOT_EQUAL,
        NEG,
        LESS_THAN,
        LESS_EQUAL,
        SHIFT_LEFT,
        GREATER_THAN,
        GREATER_EQUAL,
        SHIFT_RIGHT,
        COMMA,
        BRACE_OPEN,
        BRACE_CLOSE
    }

    private final Token.Type   type;
    private final FileLocation location;
    private String             stringValue;
    private long               longValue = 0;

    private Token(final Token.Type type, final FileLocation location)
    {
        this.type = type;
        this.location = location;
        if (type == Type.PC)
        {
            this.stringValue = "@";
        }
    }

    static Token tGeneric(final Tokenizer t, final Token.Type type)
    {
        return new Token(type, t.getLocation());
    }

    private static Token genString(final Tokenizer t, final Token.Type type, final String value)
    {
        final Token tok = new Token(type, t.getLocation());
        tok.stringValue = value;
        return tok;
    }

    static Token tMETA(final Tokenizer t, final String value)
    {
        return genString(t, Type.META, value);
    }

    static Token tWORD(final Tokenizer t, final String value)
    {
        return genString(t, Type.WORD, value);
    }

    static Token tSUB_WORD(final Tokenizer t, final String value)
    {
        return genString(t, Type.SUB_WORD, value);
    }

    static Token tOPCODE(final Tokenizer t, final String value)
    {
        return genString(t, Type.OPCODE, value);
    }

    static Token tLABEL(final Tokenizer t, final String value)
    {
        return genString(t, Type.LABEL, value);
    }

    static Token tSUB_LABEL(final Tokenizer t, final String value)
    {
        return genString(t, Type.SUB_LABEL, value);
    }

    static Token tSTRING(final Tokenizer t, final String value)
    {
        return genString(t, Type.STRING, value);
    }

    static Token tSCR_STRING(final Tokenizer t, final String value)
    {
        return genString(t, Type.SCR_STRING, value);
    }

    static Token tNUMBER(final Tokenizer t, final long value)
    {
        final Token tok = new Token(Type.NUMBER, t.getLocation());
        tok.longValue = value;
        return tok;
    }

    public Token.Type getType()
    {
        return this.type;
    }

    public FileLocation getLocation()
    {
        return this.location;
    }

    public String getStringValue()
    {
        return this.stringValue;
    }

    public long getLongValue()
    {
        return this.longValue;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.type.toString());
        switch (this.type)
        {
        case WORD:
        case SUB_WORD:
        case LABEL:
        case SUB_LABEL:
        case META:
        case STRING:
        case SCR_STRING:
        case OPCODE:
            sb.append(':');
            sb.append(this.stringValue);
            break;
        case NUMBER:
            sb.append(':');
            sb.append(String.format("$%x,%d", this.longValue, this.longValue));
            break;
        default:
            break;
        }
        return sb.toString();
    }
}
