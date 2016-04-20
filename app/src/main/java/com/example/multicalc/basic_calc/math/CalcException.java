package com.example.multicalc.basic_calc.math;

/**
 *异常类，为了实现自动寻错功能功能，让每个被解析的数学符号（MathSign）记住自己在原文本的位置。
 * 如此，抛出异常的时候把出错的MathSign作为参数传来，在异常类中自动处理为原文本出错的位置，
 * 最后得以在原输入文本中旋转出错区域。
 * 其中两个静态常量用来说明出错区域在符号左边或者右边。
 *
 * 当然同时作为参数传入的还有出错原因。
 */
public class CalcException extends Exception {

    public static final int AT_LEFT = 0;
    public static final int AT_RIGHT = 1;
    private String mDetail;
    private int mStartIndexOfInput = 0;
    private int mEndIndexOfInput = 0;

    //最简单的异常抛出，直接说明异常发生部分，用在文本还未解析为MathSign前
    public CalcException(String d, int start, int end) {
        super();
        mDetail = d;
        mStartIndexOfInput = start;
        mEndIndexOfInput = end;
    }

    //把一系列出错的MathSign抛出，最后得到的出错区域将囊括这些MathSign的最左与最右
    public CalcException(String d, MathSign... signs) {
        super();
        mDetail = d;
        if (signs.length != 0) {
            mStartIndexOfInput = signs[0].start();
            mEndIndexOfInput = signs[0].end();
            for (MathSign sign : signs) {
                if (mStartIndexOfInput > sign.start()) {
                    mStartIndexOfInput = sign.start();
                }
                if (mEndIndexOfInput < sign.end()) {
                    mEndIndexOfInput = sign.end();
                }
            }
        }
    }

    //只说明在某个MAthSign的左侧或者右侧出错，一般用在参数缺失的情况
    public CalcException(String d, MathSign sign, int whichSide) {
        super();
        mDetail = d;
        if (whichSide == AT_LEFT) {
            mStartIndexOfInput = mEndIndexOfInput = sign.start();
        } else if (whichSide == AT_RIGHT) {
            mStartIndexOfInput = mEndIndexOfInput = sign.end();
        }
    }

    /**
     * 下面三个函数都是获取有关错误信息的
     */
    public int start() {
        return mStartIndexOfInput;
    }

    public int end() {
        return mEndIndexOfInput;
    }

    public String getDetail() {
        return mDetail;
    }
}
