package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.errors.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

public class FCVTLUD extends BasicInstruction {
    public FCVTLUD() {
        super("fcvt.lu.d t1, f1, dyn", "Convert unsigned 64 bit integer from double: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100001 00011 sssss ttt fffff 1010011", true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2], statement);
        Float64 in = new Float64(FloatingPointRegisterFile.getValueLong(operands[1]));
        long out = com.github.unaimillan.jsoftfloat.operations.Conversions.convertToUnsignedLong(in, e, false);
        Floating.setfflags(e);
        RegisterFile.updateRegister(operands[0], out);
    }
}