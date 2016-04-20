package com.example.multicalc.complex.math;

import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.math.MathSign;

import java.util.HashSet;

/**
 * 类似basic_calc包中的Function，不过也不支持自定义
 */
public class ComplexFunction extends MathSign {

    public final static HashSet<String> FUNCTIONS = new HashSet<>();
    private static int sAngularUnit;

    static {
        FUNCTIONS.add("Re");
        FUNCTIONS.add("Im");
        FUNCTIONS.add("abs");
        FUNCTIONS.add("arg");
        FUNCTIONS.add("conj");
        FUNCTIONS.add("sin");
        FUNCTIONS.add("cos");
        FUNCTIONS.add("tan");
        FUNCTIONS.add("ln");
    }

    private String mName;

    public ComplexFunction(String name) {
        mName = name;
    }

    public static void eliminateAllFunction(ComplexMathSignQueue queue) throws CalcException {
        sAngularUnit = queue.getAngularUnit();
        for (int start = 0; start < queue.size(); start++) {
            if (queue.get(start) instanceof ComplexFunction) {
                ComplexFunction function = (ComplexFunction) queue.get(start);
                if (start == queue.size() - 1) {
                    throw new CalcException("参数缺失", queue.get(start), CalcException.AT_RIGHT);
                } else if (queue.get(start + 1) instanceof ComplexNumber) {
                    queue.simplify(start, start + 2,
                            functionValue(function, (ComplexNumber) queue.get(start + 1)));
                } else if (queue.get(start + 1).toString().equals("(")) {
                    int end = ComplexOperator.findConjugateBracket(queue, start + 1);
                    if (end == -1) {
                        throw new CalcException("缺失参数列表右括号", queue.get(start + 1));
                    }
                    ComplexNumber para = queue.subQueue(start + 2, end).queueValue();
                    queue.simplify(start, end + 1, functionValue(function, para));
                } else {
                    throw new CalcException("参数缺失", queue.get(start), CalcException.AT_RIGHT);
                }
            }
        }
    }

    public static ComplexNumber functionValue(ComplexFunction function, ComplexNumber para)
            throws CalcException {
        switch (function.mName) {
            case "Re":
                return new ComplexNumber(para.getReal(), 0.0, true);
            case "Im":
                return new ComplexNumber(para.getImaginary(), 0.0, true);
            case "abs":
                return new ComplexNumber(para.getModulus(), 0.0, true);
            case "arg":
                double arg = para.getArgument();
                if (sAngularUnit == ComplexMathSignQueue.ANGULAR_DEGREE) {
                    arg = arg / Math.PI * 180.0;
                }
                return new ComplexNumber(arg, 0.0, true);
            case "conj":
                return para.conjugate();
            case "sin":
                return para.sin();
            case "cos":
                return para.cos();
            case "tan":
                return para.sin().divide(para.cos());
            case "ln":
                return para.ln();
            default:
                throw new CalcException("未知函数", function);
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}
