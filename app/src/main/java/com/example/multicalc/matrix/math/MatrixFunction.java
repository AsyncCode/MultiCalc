package com.example.multicalc.matrix.math;

import com.example.multicalc.basic_calc.math.CalcException;

import java.util.HashSet;

public class MatrixFunction implements MatrixSign {

    public final static HashSet<String> FUNCTIONS = new HashSet<>();

    static {
        FUNCTIONS.add("det");
        FUNCTIONS.add("inv");
        FUNCTIONS.add("trans");
        FUNCTIONS.add("rank");
        FUNCTIONS.add("rref");
        FUNCTIONS.add("E");
    }

    private String mName;

    public MatrixFunction(String name) {
        mName = name;
    }

    public static void eliminateAllFunction(MatrixSignQueue queue) throws CalcException {
        for (int start = 0; start < queue.size(); start++) {
            if (queue.get(start) instanceof MatrixFunction) {
                MatrixFunction function = (MatrixFunction) queue.get(start);
                if (start == queue.size() - 1) {
                    throw new CalcException("参数缺失");
                } else if (queue.get(start + 1) instanceof MatrixOrNumber) {
                    queue.simplify(start, start + 2,
                            functionValue(function, (MatrixOrNumber) queue.get(start + 1)));
                } else if (queue.get(start + 1).toString().equals("(")) {
                    int end = MatrixOperator.findConjugateBracket(queue, start + 1);
                    if (end == -1) {
                        throw new CalcException("缺失参数列表右括号");
                    }
                    MatrixOrNumber para = queue.subQueue(start + 2, end).queueValue();
                    queue.simplify(start, end + 1, functionValue(function, para));
                } else {
                    throw new CalcException("参数缺失");
                }
            }
        }
    }

    public static MatrixOrNumber functionValue(MatrixFunction function, MatrixOrNumber para)
            throws CalcException {
        switch (function.mName) {
            case "det":
                return para.det();
            case "inv":
                return para.inv();
            case "trans":
                return para.trans();
            case "rank":
                return para.rank();
            case "rref":
                return para.rref();
            case "E":
                if (para.row() != 1 || para.column() != 1) {
                    throw new CalcException("参数列表中不是数，无法构建单位阵");
                }
                try {
                    int dimen = para.element(0, 0).intValue();
                    if (dimen <= 0) {
                        throw new CalcException(null);
                    }
                    return new MatrixOrNumber(Matrix.identityMatrix(dimen));
                } catch (CalcException e) {
                    throw new CalcException("参数列表中的数不是正整数，无法构建单位阵");
                }
            default:
                throw new CalcException("未知函数");
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}
