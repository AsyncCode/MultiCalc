package com.example.multicalc.basic_calc.math;

/**
 * 一个抽象类，是数学符号的一个抽象
 * 当人类看见一个数学表达式，会把数学式先解析为一个个数学符号：数字，加号，括号，常数，函数……
 * 这个类模拟的正式那一个个数学符号，之类包括RealValue, Operator, Function, Constant等
 *
 * 另外，MathSignQueue内部含有对应符号在原输入文本的位置信息，当作为异常类的参数抛出后，这个信息将会用的自动寻错
 */
public abstract class MathSign {

    private int mStartIndexInInput = 0;
    private int mEndIndexInInput = 0;

    public void setIndex(int start, int end) {
        mStartIndexInInput = start;
        mEndIndexInInput = end;
    }

    public void setIndex(MathSign sign) {
        mStartIndexInInput = sign.mStartIndexInInput;
        mEndIndexInInput = sign.mEndIndexInInput;
    }

    public int start() {
        return mStartIndexInInput;
    }

    public int end() {
        return mEndIndexInInput;
    }

    //Object具有toString()，这里将之覆写为虚函数，强制要求子类覆写为实函数
    @Override
    public abstract String toString();
}
