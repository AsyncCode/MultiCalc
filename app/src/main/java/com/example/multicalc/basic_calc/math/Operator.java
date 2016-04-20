package com.example.multicalc.basic_calc.math;

/**
 * 运算符类，包装了已实现的所有运算符以及这些运算符在MathSignQueue中的化简方法
 */
public class Operator extends MathSign {

    public static final String[] OPERATORS = {
            "(", ")",
            "!",
            "%",
            "^", "√",
            "*", "/",
            "×", "÷",
            "+", "-",
            ","};

    private String mWriting = null;

    public Operator(String writing) {
        mWriting = writing;
    }

    //寻找队列中的左括号的匹配右括号
    public static int findConjugateBracket(MathSignQueue queue, int left) {
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

    //按优先级顺序化简运算符，依次为括号，指数，阶乘，分号，乘号和加号
    public static void eliminateAllOperator(MathSignQueue queue) throws CalcException {
        eliminateBracket(queue);
        eliminatePower(queue);
        eliminateFactorial(queue);
        eliminatePercent(queue);
        eliminateMultiply(queue);
        eliminateAdd(queue);
    }

    private static void eliminateBracket(MathSignQueue queue) throws CalcException {
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

    private static void eliminatePower(MathSignQueue queue) throws CalcException {
        RealNumber bottom, exponent;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("^")) {
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    throw new CalcException("乘方底数缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof RealNumber)) {
                    throw new CalcException("乘方指数缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                bottom = (RealNumber) queue.get(i - 1);
                exponent = (RealNumber) queue.get(i + 1);
                queue.simplify(i - 1, i + 2, bottom.pow(exponent));
                i--;
            } else if (queue.get(i).toString().equals("√")) {
                if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof RealNumber)) {
                    throw new CalcException("被开方数缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                bottom = (RealNumber) queue.get(i + 1);
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    exponent = new RealNumber("0.5");
                    queue.simplify(i, i + 2, bottom.pow(exponent));
                } else {
                    if (((RealNumber) queue.get(i - 1)).compareToZero() == 0) {
                        throw new CalcException("开0方无意义", queue.get(i - 1));
                    } else {
                        exponent = new RealNumber("1").divide((RealNumber) queue.get(i - 1));
                        exponent.setIndex(queue.get(i - 1));
                    }
                    queue.simplify(i - 1, i + 2, bottom.pow(exponent));
                    i--;
                }
            }
        }
    }

    private static void eliminateFactorial(MathSignQueue queue) throws CalcException {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("!")) {
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    throw new CalcException("阶乘数缺失", queue.get(i), CalcException.AT_LEFT);
                } else {
                    int n = ((RealNumber) queue.get(i - 1)).intValueExactly();
                    if (n <= -1) {
                        throw new CalcException("阶乘数不是整数", queue.get(i - 1));
                    } else {
                        queue.simplify(i - 1, i + 1, RealNumber.chainMultiplication(1, n));
                        i--;
                    }
                }
            }
        }
    }

    private static void eliminatePercent(MathSignQueue queue) throws CalcException {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).toString().equals("%")) {
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    throw new CalcException("百分号左边缺失", queue.get(i), CalcException.AT_LEFT);
                } else {
                    queue.simplify(i - 1, i + 1,
                            ((RealNumber) queue.get(i - 1)).divide(new RealNumber("100")));
                    i--;
                }
            }
        }
    }

    private static void eliminateMultiply(MathSignQueue queue) throws CalcException {
        for (int i = 1; i < queue.size(); i++) {
            if (queue.get(i - 1) instanceof RealNumber && queue.get(i) instanceof RealNumber) {
                queue.simplify(i - 1, i + 1,
                        ((RealNumber) queue.get(i - 1)).multiply((RealNumber) queue.get(i)));
                i--;
            }
        }

        for (int i = 0; i < queue.size(); i++) {
            String s = queue.get(i).toString();
            if (s.equals("*") || s.equals("×")) {
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    throw new CalcException("乘号左边缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof RealNumber)) {
                    throw new CalcException("乘号右边缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                queue.simplify(i - 1, i + 2,
                        ((RealNumber) queue.get(i - 1)).multiply((RealNumber) queue.get(i + 1)));
                i--;
            } else if (s.equals("/") || s.equals("÷")) {
                if (i == 0 || !(queue.get(i - 1) instanceof RealNumber)) {
                    throw new CalcException("除号左边缺失", queue.get(i), CalcException.AT_LEFT);
                } else if (i == queue.size() - 1 || !(queue.get(i + 1) instanceof RealNumber)) {
                    throw new CalcException("除号右边缺失", queue.get(i), CalcException.AT_RIGHT);
                }
                queue.simplify(i - 1, i + 2,
                        ((RealNumber) queue.get(i - 1)).divide((RealNumber) queue.get(i + 1)));
                i--;
            }
        }
    }

    private static void eliminateAdd(MathSignQueue queue) throws CalcException {
        RealNumber sum = new RealNumber("0");
        int isPlus = 1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i) instanceof RealNumber) {
                if (isPlus == 1) {
                    sum = sum.add((RealNumber) queue.get(i));
                } else {
                    sum = sum.subtract((RealNumber) queue.get(i));
                    isPlus = 1;
                }
            } else if (queue.get(i).toString().equals("-")) {
                isPlus *= -1;
            } else if (!queue.get(i).toString().equals("+")) {
                throw new CalcException("非法的符号", queue.get(i));
            }
        }
        queue.simplify(0, queue.size(), sum);
    }

    @Override
    public String toString() {
        return mWriting;
    }
}