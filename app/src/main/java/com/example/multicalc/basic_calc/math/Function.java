package com.example.multicalc.basic_calc.math;

import android.database.Cursor;

import java.util.HashMap;

/**
 * 函数类封装了函数在MathSignQueue中的执行方法，包括寻找参数，带入参数化简求值
 */
public class Function extends MathSign {

    //HashMap中保存了函数名与函数参数个数
    public final static HashMap<String, Integer> FUNCTIONS = new HashMap<>();
    private static int sAngularUnit;
    private static CustomDefinitionDbHelper sHelper = null;

    //一些内置的函数被加入HashMap中
    static {
        FUNCTIONS.put("P", 2);
        FUNCTIONS.put("C", 2);
        FUNCTIONS.put("mod", 2);
        FUNCTIONS.put("abs", 1);

        FUNCTIONS.put("sin", 1);
        FUNCTIONS.put("cos", 1);
        FUNCTIONS.put("tan", 1);
        FUNCTIONS.put("cot", 1);
        FUNCTIONS.put("arcsin", 1);
        FUNCTIONS.put("arccos", 1);
        FUNCTIONS.put("arctan", 1);
        FUNCTIONS.put("arccot", 1);
        FUNCTIONS.put("sec", 1);
        FUNCTIONS.put("csc", 1);
        FUNCTIONS.put("arcsec", 1);
        FUNCTIONS.put("arccsc", 1);

        FUNCTIONS.put("lg", 1);
        FUNCTIONS.put("ln", 1);
        FUNCTIONS.put("log", 2);
    }

    private String mName = null;

    public Function(String name) {
        mName = name;
    }

    //同样是绑定数据库的操作，获取之前定义并保存在数据库中的自定义函数。
    public static void bindDbHelper(CustomDefinitionDbHelper helper) {
        sHelper = helper;
        Cursor cursor = helper.getReadableDatabase()
                .rawQuery("SELECT name, paraCount FROM function", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int paraCount = cursor.getInt(cursor.getColumnIndex("paraCount"));
            FUNCTIONS.put(name, paraCount);
        }
        cursor.close();
    }

    //把MathSignQueue中的所有函数化简，这个函数作用在于找出函数与其参数（需考虑到函数嵌套以及括号的情况）
    public static void eliminateAllFunction(MathSignQueue queue) throws CalcException {
        sAngularUnit = queue.getAngularUnit();
        int start, end;
        for (start = 0; start < queue.size(); start++) {
            if (queue.get(start) instanceof Function) {
                if (start == queue.size() - 1) {
                    throw new CalcException("找不到参数", queue.get(start), CalcException.AT_RIGHT);
                }
                int paraNeed = FUNCTIONS.get(queue.get(start).toString());
                RealNumber[] paras = new RealNumber[paraNeed];
                if (paraNeed == 1 && queue.get(start + 1) instanceof RealNumber) {
                    paras[0] = (RealNumber) queue.get(start + 1);
                    end = start + 1;
                } else {
                    if (!queue.get(start + 1).toString().equals("(")) {
                        throw new CalcException("找不到参数列表左括号",
                                queue.get(start), CalcException.AT_RIGHT);
                    }
                    end = Operator.findConjugateBracket(queue, start + 1);
                    if (end == -1) {
                        throw new CalcException("找不到参数列表右括号", queue.get(start + 1));
                    }
                    int[] boundaries = new int[paraNeed + 1];
                    boundaries[0] = start + 1;
                    boundaries[paraNeed] = end;
                    int boundaryFound = 1;
                    for (int i = start + 2, deep = 0; i < end; i++) {
                        String thisSign = queue.get(i).toString();
                        if (deep == 0 && thisSign.equals(",")) {
                            boundaries[boundaryFound] = i;
                            if (++boundaryFound == paraNeed + 1) {
                                throw new CalcException("参数过量", queue.get(i), queue.get(end - 1));
                            }
                        } else if (thisSign.equals("(")) {
                            deep++;
                        } else if (thisSign.equals(")")) {
                            deep--;
                        }
                    }
                    if (boundaryFound < paraNeed) {
                        throw new CalcException("参数不足", queue.get(start), queue.get(end));
                    }
                    for (int i = 0; i < boundaries.length - 1; i++) {
                        if (boundaries[i] + 1 == boundaries[i + 1]) {
                            throw new CalcException("参数为空",
                                    queue.get(boundaries[i]), queue.get(boundaries[i + 1]));
                        }
                        paras[i] = queue.subQueue(boundaries[i] + 1, boundaries[i + 1]).queueValue();
                    }
                }
                queue.simplify(start, end + 1, functionValue((Function) queue.get(start), paras));
            }
        }
    }

    //已知函数与其参数，求单个数学函数的函数值
    private static RealNumber functionValue(Function function, RealNumber[] paras)
            throws CalcException {
        int m, n;
        double d2r = 1.0, r2d = 1.0;
        if (sAngularUnit == MathSignQueue.ANGULAR_DEGREE) {
            double pi = Math.PI;
            d2r = pi / 180;
            r2d = 180 / pi;
        }
        switch (function.mName) {
            case "P":
                n = paras[0].intValueExactly();
                m = paras[1].intValueExactly();
                if (m < 0) {
                    throw new CalcException("排列取出元素数必须是整数", paras[1]);
                } else if (n < m) {
                    throw new CalcException("排列总元素数必须是大于取出数", paras[0]);
                }
                return RealNumber.chainMultiplication(1, n)
                        .divide(RealNumber.chainMultiplication(1, n - m));
            case "C":
                n = paras[0].intValueExactly();
                m = paras[1].intValueExactly();
                if (m < 0) {
                    throw new CalcException("组合取出元素数必须是整数", paras[1]);
                } else if (n < m) {
                    throw new CalcException("组合总元素数必须是大于取出数", paras[0]);
                }
                return RealNumber.chainMultiplication(1, n)
                        .divide(RealNumber.chainMultiplication(1, n - m))
                        .divide(RealNumber.chainMultiplication(1, m));
            case "mod":
                n = paras[0].intValueExactly();
                m = paras[1].intValueExactly();
                if (m == 0) {
                    throw new CalcException("不能对0取模");
                } else {
                    int i = n / m;
                    if (i * m != n && (m > 0 && n < 0 || m < 0 && n > 0)) {
                        i--;
                    }
                    return new RealNumber(String.valueOf(n - i * m));
                }
            case "abs":
                if (paras[0].compareToZero() < 0) {
                    return paras[0].multiply(new RealNumber("-1"));
                } else {
                    return paras[0];
                }

            case "sin":
                return new RealNumber(Math.sin(paras[0].doubleValue() * d2r));
            case "cos":
                return new RealNumber(Math.cos(paras[0].doubleValue() * d2r));
            case "tan":
                return new RealNumber(Math.tan(paras[0].doubleValue() * d2r));
            case "cot":
                return new RealNumber(1.0 / Math.tan(paras[0].doubleValue() * d2r));
            case "sec":
                return new RealNumber(1.0 / Math.cos(paras[0].doubleValue() * d2r));
            case "csc":
                return new RealNumber(1.0 / Math.sin(paras[0].doubleValue() * d2r));
            case "arcsin":
                return new RealNumber(r2d * Math.asin(paras[0].doubleValue()));
            case "arccos":
                return new RealNumber(r2d * Math.acos(paras[0].doubleValue()));
            case "arctan":
                return new RealNumber(r2d * Math.atan(paras[0].doubleValue()));
            case "arccot":
                return new RealNumber(r2d * (Math.PI / 2.0 - Math.atan(paras[0].doubleValue())));
            case "arcsec":
                return new RealNumber(r2d * Math.acos(1.0 / paras[0].doubleValue()));
            case "arccsc":
                return new RealNumber(r2d * Math.asin(1.0 / paras[0].doubleValue()));

            case "lg":
                return new RealNumber(Math.log10(paras[0].doubleValue()));
            case "ln":
                return new RealNumber(Math.log(paras[0].doubleValue()));
            case "log":
                return new RealNumber(
                        Math.log(paras[1].doubleValue()) / Math.log(paras[0].doubleValue()));

            default:
                if (sHelper != null) {
                    return sHelper.getExe(function.mName).execute(sAngularUnit, paras);
                } else {
                    throw new CalcException("未知的函数", function);
                }
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}
