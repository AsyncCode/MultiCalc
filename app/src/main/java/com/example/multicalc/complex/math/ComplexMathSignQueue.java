package com.example.multicalc.complex.math;

import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.math.MathSign;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *类似于basic_calc包中的MathSignQueue
 * 这个复数运算的很多类都与基础运算中的类相似，不过还是有不少差异，虽然这样封装为不同的类不利于代码的复用
 * 却有利于代码的解耦，防止代码过于复杂以致易出错
 */
public class ComplexMathSignQueue {

    public static final int ANGULAR_RADIAN = 0;
    public static final int ANGULAR_DEGREE = 1;
    public static final int ANGULAR_RADIAN_WITH_PI = 2;

    private String mTextInput = null;
    private int mParseIndex = 0;
    private int mAngularUnit;
    private LinkedList<MathSign> mMathSigns = new LinkedList<>();

    public static ComplexMathSignQueue parse(String input) throws CalcException {
        ComplexMathSignQueue queue = new ComplexMathSignQueue();
        if (input == null || input.length() == 0) {
            throw new CalcException("输入为空");
        }
        queue.mTextInput = input;
        while (queue.mParseIndex < input.length()) {
            if (!queue.parseOneSign()) {
                throw new CalcException("不能解析的部分", queue.mParseIndex, queue.mTextInput.length());
            }
        }
        return queue;
    }

    public boolean parseOneSign() {
        //跳过空白符号
        while (true) {
            char c = mTextInput.charAt(mParseIndex);
            if (c != ' ' && c != '\t' && c != '\n') {
                break;
            } else if (++mParseIndex >= mTextInput.length()) {
                return true;
            }
        }
        String residualPart = mTextInput.substring(mParseIndex);

        //尝试解析出一个函数
        for (String funName : ComplexFunction.FUNCTIONS) {
            if (residualPart.startsWith(funName)) {
                ComplexFunction funToAdd = new ComplexFunction(funName);
                funToAdd.setIndex(mParseIndex, mParseIndex += funName.length());
                mMathSigns.add(funToAdd);
                return true;
            }
        }

        //尝试解析出一个常数
        for (String conName : ComplexConstant.CONSTANTS.keySet()) {
            if (residualPart.startsWith(conName)) {
                ComplexConstant conToAdd = new ComplexConstant(conName);
                conToAdd.setIndex(mParseIndex, mParseIndex += conName.length());
                mMathSigns.add(conToAdd);
                return true;
            }
        }

        //尝试解析出一个运算符
        for (String op : ComplexOperator.OPERATORS) {
            if (residualPart.startsWith(op)) {
                ComplexOperator opToAdd = new ComplexOperator(op);
                opToAdd.setIndex(mParseIndex, mParseIndex += op.length());
                mMathSigns.add(opToAdd);
                return true;
            }
        }

        //尝试解析出一个数字
        Matcher matcher = Pattern.compile("[0-9]+(\\.[0-9]+)?(E[+-]?[0-9]+)?").matcher(residualPart);
        if (matcher.find() && matcher.start() == 0) {
            double d = Double.parseDouble(matcher.group());
            ComplexNumber numToAdd = new ComplexNumber(d, 0.0, true);
            numToAdd.setIndex(mParseIndex, mParseIndex += matcher.end());
            mMathSigns.add(numToAdd);
            return true;
        }
        return false;
    }

    public int getAngularUnit() {
        return mAngularUnit;
    }

    public void setAngularUnit(int unit) {
        mAngularUnit = unit;
    }

    public MathSign get(int index) {
        return mMathSigns.get(index);
    }

    public void add(MathSign sign) {
        mMathSigns.add(sign);
    }

    public void add(int location, MathSign sign) {
        mMathSigns.add(location, sign);
    }

    public int size() {
        return mMathSigns.size();
    }

    public void simplify(int start, int end, MathSign result) {
        result.setIndex(mMathSigns.get(start).start(), mMathSigns.get(end - 1).end());
        while (end-- > start) {
            mMathSigns.remove(start);
        }
        mMathSigns.add(start, result);
    }

    public ComplexMathSignQueue subQueue(int start, int end) {
        ComplexMathSignQueue subQueue = new ComplexMathSignQueue();
        subQueue.mAngularUnit = mAngularUnit;
        while (start < end) {
            subQueue.mMathSigns.add(this.mMathSigns.get(start++));
        }
        return subQueue;
    }

    public ComplexNumber queueValue() throws CalcException {
        if (size() == 0) {
            throw new CalcException("输入为空");
        }
        ComplexFunction.eliminateAllFunction(this);
        ComplexOperator.eliminateAllOperator(this);
        return (ComplexNumber) get(0);
    }
}