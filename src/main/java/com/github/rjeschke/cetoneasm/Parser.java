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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.rjeschke.cetoneasm.Opcodes.Opcode;
import com.github.rjeschke.cetoneasm.Token.Type;
import com.github.rjeschke.cetoneasm.actions.AssembleOpcodeAction;
import com.github.rjeschke.cetoneasm.actions.AssembleOpcodeAction.WidthType;
import com.github.rjeschke.cetoneasm.actions.BinaryOperatorAction;
import com.github.rjeschke.cetoneasm.actions.CallMacroAction;
import com.github.rjeschke.cetoneasm.actions.ConditionalJumpAction;
import com.github.rjeschke.cetoneasm.actions.CounterCompareAction;
import com.github.rjeschke.cetoneasm.actions.CounterDecrementAction;
import com.github.rjeschke.cetoneasm.actions.CounterSetAction;
import com.github.rjeschke.cetoneasm.actions.DefineMacroAction;
import com.github.rjeschke.cetoneasm.actions.GetVariableAction;
import com.github.rjeschke.cetoneasm.actions.JumpIdAction;
import com.github.rjeschke.cetoneasm.actions.JumpToIdAction;
import com.github.rjeschke.cetoneasm.actions.LoadNumberAction;
import com.github.rjeschke.cetoneasm.actions.MetaGotoAction;
import com.github.rjeschke.cetoneasm.actions.MetaLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetLabelAction;
import com.github.rjeschke.cetoneasm.actions.SetVariableAction;
import com.github.rjeschke.cetoneasm.actions.StoreDataAction;
import com.github.rjeschke.cetoneasm.actions.UnaryOperatorAction;
import com.github.rjeschke.cetoneasm.actions.WriteStringAction;

public class Parser
{
    private final List<Token>             tokens;
    private int                           position           = 0;
    private final Assembler               evalRuntime        = new Assembler();
    private boolean                       topLevel           = true;
    private boolean                       inMacroDefinition  = false;

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
        return new Parser(tokens).parse(new ArrayList<Action>());
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

    private FileLocation getFileLocation()
    {
        return this.peek().getLocation();
    }

    private String getStringValue()
    {
        return this.peek().getStringValue();
    }

    private void parseDataStore(final List<Action> actions, final MetaCommand mc) throws AssemblerException
    {
        final FileLocation ds = this.getFileLocation();
        final boolean isRep = mc == MetaCommand.REPB || mc == MetaCommand.REPW;
        final boolean isWord = mc == MetaCommand.DW || mc == MetaCommand.REPW;
        int cid = 0;
        JumpIdAction start = null, end = null;

        if (isRep)
        {
            actions.addAll(this.parseExpression());
            if (this.peek().getType() != Token.Type.COMMA)
            {
                throw new AssemblerException(this.getFileLocation(), "',' expected");
            }
            this.consume();
            cid = CounterState.get().newId();
            actions.add(new CounterSetAction(ds, cid));
            start = new JumpIdAction(ds);
            end = new JumpIdAction(ds);
            actions.add(start);
            actions.add(new CounterCompareAction(ds, cid, end.getID()));
        }

        for (;;)
        {
            final FileLocation efl = this.getFileLocation();
            if (this.peek().getType() == Token.Type.STRING || this.peek().getType() == Token.Type.SCR_STRING)
            {
                actions.add(new WriteStringAction(efl, this.getStringValue(), isWord,
                        this.peek().getType() == Token.Type.SCR_STRING));
                this.consume();
            }
            else
            {
                actions.addAll(this.parseExpression());
                actions.add(new StoreDataAction(efl, isWord));
            }
            if (this.peek().getType() == Token.Type.COMMA)
            {
                this.consume();
                continue;
            }
            break;
        }
        if (isRep)
        {
            actions.add(new CounterDecrementAction(ds, cid, start.getID()));
            actions.add(end);
        }
    }

    private void parseConditionalBlock(final List<Action> actions) throws AssemblerException
    {
        final JumpIdAction endLabel = new JumpIdAction(this.getFileLocation());
        boolean hasElse = false;
        for (;;)
        {
            if (this.peek().getType() != Token.Type.META)
            {
                throw new AssemblerException(this.getFileLocation(), "Syntax error");
            }
            final MetaCommand mc = MetaCommand.byName(this.peek().getStringValue());
            this.consume();
            if (mc == MetaCommand.ENDIF)
            {
                break;
            }
            else if (mc == MetaCommand.ELSE)
            {
                if (hasElse)
                {
                    throw new AssemblerException(this.getFileLocation(), "Duplicate ELSE in IF clause");
                }
                hasElse = true;
                this.parse(actions, "ELIF", "ELSE", "ENDIF");
                actions.add(new JumpToIdAction(this.getFileLocation(), endLabel.getID()));
            }
            else
            {
                if (hasElse)
                {
                    throw new AssemblerException(this.getFileLocation(), mc + " after ELSE");
                }
                final JumpIdAction endBlock = new JumpIdAction(this.getFileLocation());
                actions.addAll(this.parseExpression());
                actions.add(new ConditionalJumpAction(this.getFileLocation(), endBlock.getID()));
                this.parse(actions, "ELIF", "ELSE", "ENDIF");
                actions.add(new JumpToIdAction(this.getFileLocation(), endLabel.getID()));
                actions.add(endBlock);
            }
        }
        actions.add(endLabel);
    }

    private void parseMacroDefinition(final List<Action> actions) throws AssemblerException
    {
        if (!this.topLevel)
        {
            throw new AssemblerException(this.getFileLocation(), "Macro definitions only allowed at top level");
        }
        final FileLocation def = this.getFileLocation();
        if (this.peek().getType() != Token.Type.WORD)
        {
            throw new AssemblerException(this.getFileLocation(), "Identifier expected");
        }
        final String macroName = this.getStringValue();
        final List<String> variableNames = new ArrayList<String>();
        this.consume();
        for (;;)
        {
            if (this.peek().getType() == Token.Type.COMMA)
            {
                this.consume();
                if (this.peek().getType() != Token.Type.WORD)
                {
                    throw new AssemblerException(this.getFileLocation(), "Identifier expected");
                }
                variableNames.add(this.getStringValue());
                this.consume();
            }
            else
            {
                break;
            }
        }
        final List<Action> expression = this.parse(new ArrayList<Action>(), "ENDMACRO");
        if (this.peek().getType() != Token.Type.META || !"ENDMACRO".equals(this.getStringValue()))
        {
            throw new AssemblerException(this.getFileLocation(), "ENDMACRO expected");
        }
        this.consume();
        actions.add(new DefineMacroAction(def, macroName, variableNames, expression));
    }

    private void parseCallMacro(final List<Action> actions) throws AssemblerException
    {
        if (this.inMacroDefinition)
        {
            throw new AssemblerException(this.getFileLocation(), ".CALL inside .MACRO is forbidden");
        }
        final FileLocation def = this.getFileLocation();
        if (this.peek().getType() != Token.Type.WORD)
        {
            throw new AssemblerException(this.getFileLocation(), "Identifier expected");
        }
        final String macroName = this.getStringValue();
        final List<List<Action>> arguments = new ArrayList<List<Action>>();
        this.consume();
        for (;;)
        {
            if (this.peek().getType() == Token.Type.COMMA)
            {
                this.consume();
                arguments.add(this.parseExpression());
            }
            else
            {
                break;
            }
        }
        actions.add(new CallMacroAction(def, macroName, arguments));
    }

    private void parseRep(final List<Action> actions) throws AssemblerException
    {
        final FileLocation fl = this.getFileLocation();
        actions.addAll(this.parseExpression());
        final int cid = CounterState.get().newId();
        final JumpIdAction start = new JumpIdAction(fl);
        final JumpIdAction end = new JumpIdAction(fl);
        actions.add(new CounterSetAction(fl, cid));
        actions.add(start);
        actions.add(new CounterCompareAction(fl, cid, end.getID()));
        this.parse(actions, "ENDREP");
        actions.add(new CounterDecrementAction(this.getFileLocation(), cid, start.getID()));
        actions.add(end);
        if (this.peek().getType() != Token.Type.META || !"ENDREP".equals(this.getStringValue()))
        {
            throw new AssemblerException(this.getFileLocation(), "ENDREP expected");
        }
        this.consume();
    }

    private void parseWhile(final List<Action> actions) throws AssemblerException
    {
        final FileLocation fl = this.getFileLocation();
        final JumpIdAction start = new JumpIdAction(fl);
        final JumpIdAction end = new JumpIdAction(fl);
        actions.add(start);
        actions.addAll(this.parseExpression());
        actions.add(new ConditionalJumpAction(fl, end.getID()));
        this.parse(actions, "ENDWHILE");
        actions.add(new JumpToIdAction(this.getFileLocation(), start.getID()));
        actions.add(end);
        if (this.peek().getType() != Token.Type.META || !"ENDWHILE".equals(this.getStringValue()))
        {
            throw new AssemblerException(this.getFileLocation(), "ENDWHILE expected");
        }
        this.consume();
    }

    private void parseMeta(final List<Action> actions) throws AssemblerException
    {
        final MetaCommand mc = MetaCommand.byName(this.peek().getStringValue());
        if (mc == null)
        {
            throw new AssemblerException(this.getFileLocation(), "Illegal meta command '"
                    + this.getStringValue() + "'");
        }
        switch (mc)
        {
        case IF:
            this.parseConditionalBlock(actions);
            break;
        case MACRO:
            this.consume();
            this.parseMacroDefinition(actions);
            break;
        case CALL:
            this.consume();
            this.parseCallMacro(actions);
            break;
        case REP:
            this.consume();
            this.parseRep(actions);
            break;
        case WHILE:
            this.consume();
            this.parseWhile(actions);
            break;
        case ELIF:
            throw new AssemblerException(this.getFileLocation(), "ELIF without IF");
        case ELSE:
            throw new AssemblerException(this.getFileLocation(), "ELSE without IF");
        case ENDIF:
            throw new AssemblerException(this.getFileLocation(), "ENDIF without IF");
        case ENDMACRO:
            throw new AssemblerException(this.getFileLocation(), "ENDMACRO without MACRO");
        case ENDREP:
            throw new AssemblerException(this.getFileLocation(), "ENDREP without REP");
        case ENDWHILE:
            throw new AssemblerException(this.getFileLocation(), "ENDWHILE without WHILE");
        case DB:
        case DW:
        case REPB:
        case REPW:
            this.consume();
            this.parseDataStore(actions, mc);
            break;
        case LABEL:
            this.consume();
            if (this.peek().getType() != Token.Type.WORD)
            {
                throw new AssemblerException(this.getFileLocation(), "Identifier expected");
            }
            actions.add(new MetaLabelAction(this.getFileLocation(), this.getStringValue()));
            this.consume();
            break;
        case GOTO:
            this.consume();
            if (this.peek().getType() != Token.Type.WORD)
            {
                throw new AssemblerException(this.getFileLocation(), "Identifier expected");
            }
            actions.add(new MetaGotoAction(this.getFileLocation(), this.getStringValue()));
            this.consume();
            break;
        default:
            throw new AssemblerException(this.getFileLocation(), "Unimplemented meta command '" + mc + "'");
        }
    }

    private boolean isExpression()
    {
        final UnaryOperator uop = UnaryOperator.fromToken(this.peek());
        if (uop != null)
        {
            return true;
        }
        switch (this.peek().getType())
        {
        case WORD:
        case SUB_WORD:
        case PC:
        case NUMBER:
        case BRACE_OPEN:
            return true;
        default:
            return false;
        }
    }

    private List<Action> parseExpression() throws AssemblerException
    {
        final List<Action> expr = new ArrayList<Action>();
        this.parseExpression(-1, expr);
        this.simplifyStaticExpression(expr);
        return expr;
    }

    private List<Action> parseExpression(final int prio, final List<Action> lActions) throws AssemblerException
    {
        final UnaryOperator uop = UnaryOperator.fromToken(this.peek());
        if (uop != null)
        {
            final Action una = new UnaryOperatorAction(this.getFileLocation(), uop);
            this.consume();
            this.parseExpression(uop.getPriority(), lActions);
            lActions.add(una);
        }
        else
        {
            if (!this.isExpression())
            {
                throw new AssemblerException(this.getFileLocation(), "Expression expected");
            }
            switch (this.peek().getType())
            {
            case PC:
            case WORD:
            {
                final GetVariableAction gva = new GetVariableAction(this.getFileLocation(), this.getStringValue());
                lActions.add(gva);
                this.consume();
                break;
            }
            case NUMBER:
                lActions.add(new LoadNumberAction(this.getFileLocation(), this.peek().getLongValue()));
                this.consume();
                break;
            case BRACE_OPEN:
                this.consume();
                this.parseExpression(-1, lActions);
                if (this.peek().getType() != Token.Type.BRACE_CLOSE)
                {
                    throw new AssemblerException(this.getFileLocation(), "')' expected");
                }
                this.consume();
                break;
            default:
                throw new AssemblerException(this.getFileLocation(), "Syntax error");
            }

            for (;;)
            {
                final BinaryOperator bop = BinaryOperator.fromToken(this.peek());
                if (bop == null || bop.getPriority() < prio)
                {
                    break;
                }
                final Action boa = new BinaryOperatorAction(this.getFileLocation(), bop);
                this.consume();
                this.parseExpression(bop.getPriority(), lActions);
                lActions.add(boa);
            }
        }
        return lActions;
    }

    private void parseAssigment(final List<Action> actions) throws AssemblerException
    {
        if (this.peek(1).getType() != Token.Type.ASSIGN)
        {
            throw new AssemblerException(this.getFileLocation(), "'=' expected");
        }
        final String varName = this.peek().getType() == Token.Type.PC ? "@" : this.getStringValue();
        this.consume(2);
        actions.addAll(this.parseExpression());
        actions.add(new SetVariableAction(this.getFileLocation(), varName));
    }

    private Long evaluateStaticExpression(final List<Action> expr)
    {
        try
        {
            for (final Action a : expr)
            {
                if (a instanceof GetVariableAction)
                {
                    return null;
                }
            }
            return Long.valueOf(this.evalRuntime.evalExpression(expr));
        }
        catch (final Throwable t) // If test run fails, keep silent
        {
            return null;
        }
    }

    private boolean simplifyStaticExpression(final List<Action> expr)
    {
        final Long result = this.evaluateStaticExpression(expr);
        if (result != null)
        {
            final Action first = expr.get(0);
            expr.clear();
            expr.add(new LoadNumberAction(first.getLocation(), result.longValue()));
            return true;
        }
        return false;
    }

    // XXX: This needs cleaning
    private void parseOpcode(final List<Action> actions) throws AssemblerException
    {
        final FileLocation opfl = this.getFileLocation();
        final String opcs = this.getStringValue();
        final HashMap<AddressingMode, Opcode> opcm = Opcodes.BY_NAME_MAPPED.get(opcs);
        this.consume();

        final int actionsBefore = actions.size();

        // Is next token unrelated?
        AddressingMode addrMode = null;
        if (this.peek().getType() == Token.Type.IMMEDIATE)
        {
            addrMode = AddressingMode.IMMEDIATE;
            this.consume();
        }
        else if (opcm.size() == 1 && opcm.containsKey(AddressingMode.RELATIVE))
        {
            addrMode = AddressingMode.RELATIVE;
        }
        else
        {
            boolean nextUnrelated = false;
            switch (this.peek().getType())
            {
            case OPCODE:
            case LABEL:
            case SUB_LABEL:
            case META:
            case EOF:
            case STRING:
            case SCR_STRING:
                nextUnrelated = true;
                break;
            case PC:
            case WORD:
            case SUB_WORD:
                nextUnrelated = this.peek(1).getType() == Token.Type.ASSIGN;
                break;
            default:
                break;
            }
            if (nextUnrelated)
            {
                addrMode = AddressingMode.IMPLIED;
            }
        }

        if (addrMode != null) // Implied, Immediate
        {
            final Opcode opc = opcm.get(addrMode);
            if (opc == null)
            {
                throw new AssemblerException(opfl, "Illegal addressing mode");
            }
            switch (addrMode)
            {
            case IMMEDIATE:
            case RELATIVE:
                actions.addAll(this.parseExpression());
                actions.add(new AssembleOpcodeAction(opfl, opc));
                break;
            case IMPLIED:
                actions.add(new AssembleOpcodeAction(opfl, opc));
                break;
            default:
                throw new AssemblerException(opfl, "Internal error, uncatched addressing mode: " + addrMode);
            }
        }
        else
        {
            // Check for indirect modes
            if (this.peek().getType() == Type.BRACE_OPEN)
            {
                this.consume();
                actions.addAll(this.parseExpression());
                if (this.peek().getType() == Token.Type.COMMA)
                {
                    this.consume();
                    if (this.peek().getType() != Token.Type.WORD || !this.peek().getStringValue().equals("X"))
                    {
                        throw new AssemblerException(opfl, "Illegal addressing mode, ',X' expected");
                    }
                    this.consume();
                    if (this.peek().getType() != Token.Type.BRACE_CLOSE)
                    {
                        throw new AssemblerException(this.getFileLocation(), "')' expected");
                    }
                    this.consume();
                    addrMode = AddressingMode.INDIRECT_INDEXED;
                }
                else if (this.peek().getType() == Token.Type.BRACE_CLOSE)
                {
                    this.consume();
                    if (this.peek().getType() == Token.Type.COMMA)
                    {
                        this.consume();
                        if (this.peek().getType() != Token.Type.WORD || !this.peek().getStringValue().equals("Y"))
                        {
                            throw new AssemblerException(opfl, "Illegal addressing mode, 'Y' expected");
                        }
                        this.consume();
                        addrMode = AddressingMode.INDEXED_INDIRECT;
                    }
                    else
                    {
                        addrMode = AddressingMode.INDIRECT;
                    }
                }
                else
                {
                    throw new AssemblerException(this.getFileLocation(), "Syntax error");
                }
                final Opcode opc = opcm.get(addrMode);
                if (opc == null)
                {
                    throw new AssemblerException(opfl, "Illegal addressing mode");
                }
                actions.add(new AssembleOpcodeAction(opfl, opc));
            }
            else
            {
                final List<Action> expr = this.parseExpression();
                actions.addAll(expr);
                final Long exprValue = this.evaluateStaticExpression(expr);
                final int width = exprValue == null ? 0 : ((exprValue.longValue() & 0xffff) < 256 ? 1 : 2);
                final int index;
                if (this.peek().getType() == Token.Type.COMMA)
                {
                    this.consume();
                    if (this.peek().getType() == Token.Type.WORD && this.peek().getStringValue().equals("Y"))
                    {
                        index = 2;
                    }
                    else if (this.peek().getType() == Token.Type.WORD && this.peek().getStringValue().equals("X"))
                    {
                        index = 1;
                    }
                    else
                    {
                        throw new AssemblerException(opfl, "Illegal addressing mode, 'X' or 'Y' expected");
                    }
                }
                else
                {
                    index = 0;
                }

                int mask = 0;
                switch (index)
                {
                case 0:
                    mask = (opcm.containsKey(AddressingMode.ZEROPAGE) ? 1 : 0)
                            | (opcm.containsKey(AddressingMode.ABSOLUTE) ? 2 : 0);
                    break;
                case 1:
                    mask = (opcm.containsKey(AddressingMode.ZEROPAGE_X) ? 1 : 0)
                            | (opcm.containsKey(AddressingMode.ABSOLUTE_X) ? 2 : 0);
                    break;
                case 2:
                    mask = (opcm.containsKey(AddressingMode.ZEROPAGE_Y) ? 1 : 0)
                            | (opcm.containsKey(AddressingMode.ABSOLUTE_Y) ? 2 : 0);
                    break;
                }
                if (mask == 0)
                {
                    throw new AssemblerException(opfl, "Illegal addressing mode");
                }

                switch (width)
                {
                case 0:
                    if (mask == 3)
                    {
                        switch (index)
                        {
                        case 0:
                            actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE));
                            break;
                        case 1:
                            actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE_X));
                            break;
                        case 2:
                            actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE_Y));
                            break;
                        }
                    }
                    else
                    {
                        switch (index)
                        {
                        case 0:
                            addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE : AddressingMode.ABSOLUTE;
                            break;
                        case 1:
                            addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE_X : AddressingMode.ABSOLUTE_X;
                            break;
                        case 2:
                            addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE_Y : AddressingMode.ABSOLUTE_Y;
                            break;
                        }
                        actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode)));
                    }
                    break;
                case 1:
                    switch (index)
                    {
                    case 0:
                        addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE : AddressingMode.ABSOLUTE;
                        break;
                    case 1:
                        addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE_X : AddressingMode.ABSOLUTE_X;
                        break;
                    case 2:
                        addrMode = (mask & 1) != 0 ? AddressingMode.ZEROPAGE_Y : AddressingMode.ABSOLUTE_Y;
                        break;
                    }
                    actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode)));
                    break;
                case 2:
                    switch (index)
                    {
                    case 0:
                        addrMode = AddressingMode.ABSOLUTE;
                        break;
                    case 1:
                        addrMode = AddressingMode.ABSOLUTE_X;
                        break;
                    case 2:
                        addrMode = AddressingMode.ABSOLUTE_Y;
                        break;
                    }
                    if (addrMode == null || !opcm.containsKey(addrMode))
                    {
                        throw new AssemblerException(opfl, "Illegal addressing mode");
                    }
                    actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode)));
                    break;
                }
            }
        }
        // TODO ... *sigh*
        if (actionsBefore == actions.size())
        {
            throw new AssemblerException(opfl, "Internal error, no opcode action written");
        }
    }

    private List<Action> parse(final List<Action> actions, final String... endMetaMarker) throws AssemblerException
    {
        final boolean wasTopLevel = this.topLevel;
        final boolean wasMacroDefinition = this.inMacroDefinition;
        if (endMetaMarker.length > 0)
        {
            this.inMacroDefinition = endMetaMarker.length == 1 && endMetaMarker[0].equals("ENDMACRO");
            this.topLevel = false;
        }
        boolean finished = false;
        while (this.position < this.tokens.size() && !finished)
        {
            switch (this.peek().getType())
            {
            case META:
                if (endMetaMarker.length != 0)
                {
                    for (final String m : endMetaMarker)
                    {
                        if (this.getStringValue().equals(m))
                        {
                            finished = true;
                            break;
                        }
                    }
                }
                if (!finished)
                {
                    this.parseMeta(actions);
                }
                break;
            case LABEL:
            case SUB_LABEL:
                actions.add(new SetLabelAction(this.getFileLocation(), this.getStringValue()));
                this.consume();
                break;
            case WORD:
            case PC:
                this.parseAssigment(actions);
                break;
            case OPCODE:
                this.parseOpcode(actions);
                break;
            case EOF:
                finished = true;
                break;
            default:
                if (!finished)
                {
                    throw new AssemblerException(this.peek().getLocation(), "Syntax error");
                }
                break;
            }
        }
        this.topLevel = wasTopLevel;
        this.inMacroDefinition = wasMacroDefinition;
        return actions;
    }
}
