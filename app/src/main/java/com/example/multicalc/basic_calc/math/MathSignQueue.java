package com.example.multicalc.basic_calc.math;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 整个运算过程的关键，正如MathSign中所描述，MathSign是对数学符号的抽象，这个类包含了含有一系列MathSign的List
 * 就好比由数学符号构成的数学式，并且提供一系列有关这个List容器的操作方法，将会用到数学式的化简等操作中
 */
public class MathSignQueue {

    //分别代表角度值和弧度制
    public static final int ANGULAR_RADIAN = 0;
    public static final int ANGULAR_DEGREE = 1;

    private String mTextInput = null;
    private int mParseIndex = 0;
    private int mAngularUnit;
    private LinkedList<MathSign> mMathSigns = new LinkedList<>();

    //从文本输入中解析出一个MathSignQueue
    public static MathSignQueue parse(String input) throws CalcException {
        MathSignQueue queue = new MathSignQueue();
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

    //从文本输入中解析出一个MathSign加入到List容器中，如果由于碰到不能解析的符号而解析失败。则返回false
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

        //尝试解析出一个常数或函数
        String name = null;
        int type = 0;
        for (String conName : Constant.CONSTANTS.keySet()) {
            if (residualPart.startsWith(conName)) {
                if (name == null || conName.length() > name.length()) {
                    name = conName;
                    type = 1;
                }
            }
        }
        for (String funName : Function.FUNCTIONS.keySet()) {
            if (residualPart.startsWith(funName)) {
                if (name == null || funName.length() > name.length()) {
                    name = funName;
                    type = 2;
                }
            }
        }
        switch (type) {
            case 1:
                Constant conToAdd = new Constant(name);
                conToAdd.setIndex(mParseIndex, mParseIndex += name.length());
                mMathSigns.add(conToAdd);
                return true;
            case 2:
                Function funToAdd = new Function(name);
                funToAdd.setIndex(mParseIndex, mParseIndex += name.length());
                mMathSigns.add(funToAdd);
                return true;
        }

        //尝试解析出一个运算符
        for (String op : Operator.OPERATORS) {
            if (residualPart.startsWith(op)) {
                Operator opToAdd = new Operator(op);
                opToAdd.setIndex(mParseIndex, mParseIndex += op.length());
                mMathSigns.add(opToAdd);
                return true;
            }
        }

        //尝试解析出一个数字
        Matcher matcher = Pattern.compile("[0-9]+(\\.[0-9]+)?(E[+-]?[0-9]+)?").matcher(residualPart);
        if (matcher.find() && matcher.start() == 0) {
            RealNumber numToAdd = new RealNumber(matcher.group());
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

    public MathSign remove(int location) {
        return mMathSigns.remove(location);
    }

    public int size() {
        return mMathSigns.size();
    }

    //一个经常使用的函数，当MathSignQueue的一部分由于得出值被化简，则这一部分将被一个具体的实数值替代
    public void simplify(int start, int end, MathSign result) {
        result.setIndex(mMathSigns.get(start).start(), mMathSigns.get(end - 1).end());
        while (end-- > start) {
            mMathSigns.remove(start);
        }
        mMathSigns.add(start, result);
    }

    public MathSignQueue subQueue(int start, int end) {
        MathSignQueue subQueue = new MathSignQueue();
        subQueue.mAngularUnit = mAngularUnit;
        while (start < end) {
            subQueue.mMathSigns.add(this.mMathSigns.get(start++));
        }
        return subQueue;
    }

    public MathSignQueue append(MathSignQueue tail) {
        for (int i = 0; i < tail.size(); i++) {
            this.mMathSigns.add(tail.mMathSigns.get(i));
        }
        return this;
    }

    //求一个MashSignQueue的值，函数的优先级高于运算符，而不同运算符优先级各异，这将在Operator类中实现
    public RealNumber queueValue() throws CalcException {
        if (size() == 0) {
            throw new CalcException("输入为空");
        }
        Function.eliminateAllFunction(this);
        Operator.eliminateAllOperator(this);
        return (RealNumber) get(0);
    }
}