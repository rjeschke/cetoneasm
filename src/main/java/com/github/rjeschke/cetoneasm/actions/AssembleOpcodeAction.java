package com.github.rjeschke.cetoneasm.actions;

import java.util.List;

import com.github.rjeschke.cetoneasm.Action;
import com.github.rjeschke.cetoneasm.AssemblerException;
import com.github.rjeschke.cetoneasm.FileLocation;
import com.github.rjeschke.cetoneasm.Opcodes.Opcode;
import com.github.rjeschke.cetoneasm.Runtime;

public class AssembleOpcodeAction extends Action
{
    public enum WidthType
    {
        ABSOLUTE,
        ABSOLUTE_X,
        ABSOLUTE_Y
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    public AssembleOpcodeAction(final FileLocation location, final String opcode, final WidthType widthtype,
            final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    public AssembleOpcodeAction(final FileLocation location, final Opcode opc, final List<Action> expression)
    {
        super(location);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run(final Runtime runtime) throws AssemblerException
    {
        // TODO Auto-generated method stub

    }

}
