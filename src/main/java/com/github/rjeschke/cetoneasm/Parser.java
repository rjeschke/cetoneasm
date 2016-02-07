package com.github.rjeschke.cetoneasm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.rjeschke.cetoneasm.Opcodes.Opcode;
import com.github.rjeschke.cetoneasm.Token.Type;
import com.github.rjeschke.cetoneasm.actions.AssembleOpcodeAction;
import com.github.rjeschke.cetoneasm.actions.AssembleOpcodeAction.WidthType;
import com.github.rjeschke.cetoneasm.actions.AssignAction;
import com.github.rjeschke.cetoneasm.actions.BinaryOperatorAction;
import com.github.rjeschke.cetoneasm.actions.GetVariableAction;
import com.github.rjeschke.cetoneasm.actions.LoadNumberAction;
import com.github.rjeschke.cetoneasm.actions.UnaryOperatorAction;

public class Parser
{
    private final List<Token>  tokens;
    private final List<Action> actions       = new ArrayList<Action>();
    private int                position      = 0;
    private final int[]        positionStack = new int[1024];
    private int                positionSp    = 0;
    private final Runtime      evalRuntime   = new Runtime();

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

    private void pushPosition() throws AssemblerException
    {
        if (this.positionSp == this.positionStack.length)
        {
            throw new AssemblerException(this.peek().getLocation(), "Internal position stack overflow");
        }
        this.positionStack[this.positionSp++] = this.position;
    }

    private void popPosition() throws AssemblerException
    {
        if (this.positionSp == 0)
        {
            throw new AssemblerException(this.peek().getLocation(), "Internal position stack underrun");
        }
        this.position = this.positionStack[--this.positionSp];
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

    private void parseMeta() throws AssemblerException
    {
        final MetaCommand mc = MetaCommand.byName(this.peek().getStringValue());
        if (mc == null)
        {
            throw new AssemblerException(this.getFileLocation(), "Illegal meta command '"
                    + this.getStringValue() + "'");
        }
        this.consume();
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
        return this.parseExpression(-1, new ArrayList<Action>());
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
                lActions.add(new GetVariableAction(this.getFileLocation(), "@"));
                this.consume();
                break;
            case WORD:
                lActions.add(new GetVariableAction(this.getFileLocation(), "@"));
                this.consume();
                break;
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

    private void parseAssigment() throws AssemblerException
    {
        if (this.peek(1).getType() != Token.Type.ASSIGN)
        {
            throw new AssemblerException(this.getFileLocation(), "Syntax erro, '=' expected");
        }
        final String varName = this.peek().getType() == Token.Type.PC ? "@" : this.getStringValue();
        this.consume(2);
        this.actions.addAll(this.parseExpression());
        this.actions.add(new AssignAction(this.getFileLocation(), varName));
    }

    private Integer evaluateStaticExpression(final List<Action> expr) throws AssemblerException
    {
        for (final Action a : expr)
        {
            if (a instanceof GetVariableAction)
            {
                return null;
            }
        }
        this.evalRuntime.testRun(expr);
        return Integer.valueOf((int)this.evalRuntime.pop() & 65535);
    }

    private void parseOpcode() throws AssemblerException
    {
        final FileLocation opfl = this.getFileLocation();
        final String opcs = this.getStringValue();
        final HashMap<AddressingMode, Opcode> opcm = Opcodes.BY_NAME_MAPPED.get(opcs);
        this.consume();

        final int actionsBefore = this.actions.size();

        // Is next token unrelated?
        AddressingMode addrMode = null;
        if (this.peek().getType() == Token.Type.IMMEDIATE)
        {
            addrMode = AddressingMode.IMMEDIATE;
            this.consume();
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

        if (addrMode != null) // Immediate, implied
        {
            final Opcode opc = opcm.get(addrMode);
            if (opc == null)
            {
                throw new AssemblerException(opfl, "Illegal addressing mode");
            }
            this.actions.add(new AssembleOpcodeAction(opfl, opc, this.parseExpression()));
        }
        else
        {
            List<Action> expression = null;
            // Check for indirect modes
            if (this.peek().getType() == Type.BRACE_OPEN)
            {
                this.consume();
                expression = this.parseExpression();
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
                this.actions.add(new AssembleOpcodeAction(opfl, opc, expression));
            }
            else
            {
                expression = this.parseExpression();
                final Integer exprValue = this.evaluateStaticExpression(expression);
                final int width = exprValue == null ? 0 : (exprValue.intValue() < 256 ? 1 : 2);
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
                            this.actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE, expression));
                            break;
                        case 1:
                            this.actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE_X, expression));
                            break;
                        case 2:
                            this.actions.add(new AssembleOpcodeAction(opfl, opcs, WidthType.ABSOLUTE_Y, expression));
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
                        this.actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode), expression));
                    }
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
                    this.actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode), expression));
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
                    this.actions.add(new AssembleOpcodeAction(opfl, opcm.get(addrMode), expression));
                    break;
                }
            }
        }
        // TODO ... *sigh*
        if (actionsBefore == this.actions.size())
        {
            throw new AssemblerException(opfl, "Internal error, no opcode action written");
        }
    }

    private List<Action> parse() throws AssemblerException
    {
        while (this.position < this.tokens.size())
        {
            System.out.println(this.peek());
            switch (this.peek().getType())
            {
            case META:
                this.parseMeta();
                break;
            case WORD:
            case PC:
                this.parseAssigment();
                break;
            case OPCODE:
                this.parseOpcode();
                break;
            case EOF:
                this.consume();
                break;
            default:
                throw new AssemblerException(this.peek().getLocation(), "Syntax error");
            }
        }
        return this.actions;
    }
}
