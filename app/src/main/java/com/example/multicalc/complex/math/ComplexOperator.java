package com.example.multicalc.complex.math;

import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.math.MathSign;

/**
 * 又一个类似，复数运算的运算符包装类
 */
public class ComplexOperator extends MathSign {

    public static final String[] OPERATORS = {
            "(", ")",
            "∠",
            "^", "√",
            "*", "/",
            "×", "÷",
            "+", "-"};

    private String mWriting;

    public ComplexOperator(String writing) {
        mWriting = writing;
    }

    public static int findConjugateBracket(ComplexMathSignQueue queue, int left) {
        if (left < 0 || left >= queue.size() || !queue.get(left).toString().equals("(")) {
            return -1;
        }
        MathSign conjugate;
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

    public static void eliminateAllOperator(ComplexMathSignQueue queue) throws CalcException {
        eliminateBracket(queue);
        eliminatePolarForm(queue);
        eliminatePower(queue);
        eliminateMultiply(queue);
        eliminateAdd(queue);
    }

    private static void eliminateBracket(ComplexMathSignQueue queue) throws CalcException {
        for (int left = 0; left < queue.size(); left++) {
            if (queue.get(left).toString().equals("(")) {
                int right = findConjugateBracket(queue, left);
                if (right == -1) {
                    throw new CalcException("无匹配的右括号", queue.get(left));
                }
                queue.simplify(left, right + 1, queue.subQueue(left + 1, right).queueValue());
            }
        }
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals(")")) {
                throw new CalcException("无匹配的左括号", queue.get(i));
            }
        }
    }

    private static void eliminatePolarForm(ComplexMathSignQueue queue) throws CalcException {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("∠")) {
                if (i == 0 || !(queue.get(i - 1) instanceof ComplexNumber)) {
                    throw new CalcException("极坐标表示左侧缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof ComplexNumber)) {
                    throw new CalcException("极坐标表示右侧缺失", queue.get(i), CalcException.AT_RIGHT);
                } else {
                    ComplexNumber mod = (ComplexNumber) queue.get(i - 1);
                    ComplexNumber arg = (ComplexNumber) queue.get(i + 1);
                    if (queue.getAngularUnit() == ComplexMathSignQueue.ANGULAR_DEGREE) {
                        arg = arg.multiply(ComplexNumber.PI).divide(new ComplexNumber(180.0, 0.0, true));
                    }
                    ComplexNumber result = mod.multiply(ComplexNumber.exp(ComplexNumber.I.multiply(arg)));
                    queue.simplify(i - 1, i + 2, result);
                    i--;
                }
            }
        }
    }

    private static void eliminatePower(ComplexMathSignQueue queue) throws CalcException {
        ComplexNumber bottom, exponent;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("^")) {
                if (i == 0 || !(queue.get(i - 1) instanceof ComplexNumber)) {
                    throw new CalcException("乘方底数缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof ComplexNumber)) {
                    throw new CalcException("乘方指数缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                bottom = (ComplexNumber) queue.get(i - 1);
                exponent = (ComplexNumber) queue.get(i + 1);
                queue.simplify(i - 1, i + 2, bottom.pow(exponent));
                i--;
            } else if (queue.get(i).toString().equals("√")) {
                if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof ComplexNumber)) {
                    throw new CalcException("被开方数缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                bottom = (ComplexNumber) queue.get(i + 1);
                if (i == 0 || !(queue.get(i - 1) instanceof ComplexNumber)) {
                    exponent = new ComplexNumber(0.5, 0.0, true);
                    queue.simplify(i, i + 2, bottom.pow(exponent));
                } else {
                    exponent = new ComplexNumber(1.0, 0.0, true)
                            .divide((ComplexNumber) queue.get(i - 1));
                    exponent.setIndex(queue.get(i - 1));
                    queue.simplify(i - 1, i + 2, bottom.pow(exponent));
                    i--;
                }
            }
        }
    }

    private static void eliminateMultiply(ComplexMathSignQueue queue) throws CalcException {
        for (int i = 1; i < queue.size(); i++) {
            if (queue.get(i - 1) instanceof ComplexNumber && queue.get(i) instanceof ComplexNumber) {
                queue.simplify(i - 1, i + 1,
                        ((ComplexNumber) queue.get(i - 1)).multiply((ComplexNumber) queue.get(i)));
                i--;
            }
        }

        for (int i = 0; i < queue.size(); i++) {
            String s = queue.get(i).toString();
            if (s.equals("*") || s.equals("×")) {
                if (i == 0 || !(queue.get(i - 1) instanceof ComplexNumber)) {
                    throw new CalcException("乘号左边缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof ComplexNumber)) {
                    throw new CalcException("乘号右边缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                queue.simplify(i - 1, i + 2,
                        ((ComplexNumber) queue.get(i - 1)).multiply((ComplexNumber) queue.get(i + 1)));
                i--;
            } else if (s.equals("/") || s.equals("÷")) {
                if (i == 0 || !(queue.get(i - 1) instanceof ComplexNumber)) {
                    throw new CalcException("除号左边缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof ComplexNumber)) {
                    throw new CalcException("除号右边缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                queue.simplify(i - 1, i + 2,
                        ((ComplexNumber) queue.get(i - 1)).divide((ComplexNumber) queue.get(i + 1)));
                i--;
            }
        }
    }

    private static void eliminateAdd(ComplexMathSignQueue queue) throws CalcException {
        ComplexNumber sum = new ComplexNumber(0.0, 0.0, true);
        int isPlus = 1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i) instanceof ComplexNumber) {
                if (isPlus == 1) {
                    sum = sum.add((ComplexNumber) queue.get(i));
                } else {
                    sum = sum.subtract((ComplexNumber) queue.get(i));
                    isPlus = 1;
                }
            } else if (queue.get(i).toString().equals("-")) {
                isPlus *= -1;
            }
        }
        queue.simplify(0, queue.size(), sum);
    }

    @Override
    public String toString() {
        return mWriting;
    }
}
