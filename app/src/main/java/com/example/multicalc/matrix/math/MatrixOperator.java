package com.example.multicalc.matrix.math;

import com.example.multicalc.basic_calc.math.CalcException;

/**
 * 类中封装了有关MatrixSign的各种运算符的化简操作
 */
public class MatrixOperator implements MatrixSign {

    public static final String[] OPERATORS = {
            "(", ")",
            "^",
            "*", "/",
            "+", "-",
            "="};

    private String mWriting;

    public MatrixOperator(String writing) {
        mWriting = writing;
    }

    public static int findConjugateBracket(MatrixSignQueue queue, int left) {
        if (left < 0 || left >= queue.size() || !queue.get(left).toString().equals("(")) {
            return -1;
        }
        MatrixSign conjugate;
        int deep = 0;
        int right = left + 1;
        while (right < queue.size()) {
            conjugate = queue.get(right);
            if (conjugate.toString().equals(")")) {
                if (deep == 0) {
                    return right;
                } else {
                    deep--;
                }
            } else if (conjugate.toString().equals("(")) {
                deep++;
            }
            right++;
        }
        return -1;
    }

    public static void eliminateAllOperator(MatrixSignQueue queue) throws CalcException {
        eliminateBracket(queue);
        eliminatePower(queue);
        eliminateMultiply(queue);
        eliminateAdd(queue);
    }

    private static void eliminateBracket(MatrixSignQueue queue) throws CalcException {
        for (int left = 0; left < queue.size(); left++) {
            if (queue.get(left).toString().equals("(")) {
                int right = findConjugateBracket(queue, left);
                if (right == -1) {
                    throw new CalcException("无匹配的右括号");
                }
                queue.simplify(left, right + 1, queue.subQueue(left + 1, right).queueValue());
            }
        }
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals(")")) {
                throw new CalcException("无匹配的左括号");
            }
        }
    }

    private static void eliminatePower(MatrixSignQueue queue) throws CalcException {
        MatrixOrNumber bottom, exponent;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("^")) {
                if (i == 0 || !(queue.get(i - 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("乘方底数缺失");
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("乘方指数缺失");
                }
                bottom = (MatrixOrNumber) queue.get(i - 1);
                exponent = (MatrixOrNumber) queue.get(i + 1);
                queue.simplify(i - 1, i + 2, bottom.pow(exponent));
                i--;
            }
        }
    }

    private static void eliminateMultiply(MatrixSignQueue queue) throws CalcException {
        for (int i = 1; i < queue.size(); i++) {
            if (queue.get(i - 1) instanceof MatrixOrNumber && queue.get(i) instanceof MatrixOrNumber) {
                queue.simplify(i - 1, i + 1,
                        ((MatrixOrNumber) queue.get(i - 1)).multiply((MatrixOrNumber) queue.get(i)));
                i--;
            }
        }
        for (int i = 0; i < queue.size(); i++) {
            String s = queue.get(i).toString();
            if (s.equals("*")) {
                if (i == 0 || !(queue.get(i - 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("乘号左边缺失");
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("乘号右边缺失");
                }
                queue.simplify(i - 1, i + 2,
                        ((MatrixOrNumber) queue.get(i - 1)).multiply((MatrixOrNumber) queue.get(i + 1)));
                i--;
            } else if (s.equals("/")) {
                if (i == 0 || !(queue.get(i - 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("除号左边缺失");
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof MatrixOrNumber)) {
                    throw new CalcException("除号右边缺失");
                }
                queue.simplify(i - 1, i + 2,
                        ((MatrixOrNumber) queue.get(i - 1)).divide((MatrixOrNumber) queue.get(i + 1)));
                i--;
            }
        }
    }

    private static void eliminateAdd(MatrixSignQueue queue) throws CalcException {
        int isPlus = 1;
        MatrixOrNumber sum = null;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i) instanceof MatrixOrNumber) {
                MatrixOrNumber mOn = (MatrixOrNumber) queue.get(i);
                if (isPlus == 1) {
                    sum = sum == null ? mOn : sum.add(mOn);
                } else {
                    sum = sum == null ? mOn.multiply(new MatrixOrNumber(new RationalNumber(-1))) :
                            sum.subtract(mOn);
                    isPlus = 1;
                }
            } else if (queue.get(i).toString().equals("-")) {
                isPlus *= -1;
            }
        }
        if (sum == null) {
            throw new CalcException("缺少数据，无法计算");
        }
        queue.simplify(0, queue.size(), sum);
    }

    @Override
    public String toString() {
        return mWriting;
    }
}
