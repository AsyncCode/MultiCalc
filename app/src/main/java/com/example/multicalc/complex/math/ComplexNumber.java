package com.example.multicalc.complex.math;

import com.example.multicalc.basic_calc.math.MathSign;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//一般实际复数运算应用罕见大数值、高精度情况，用double存储内部数据即可
// 另一方面复数运算频繁使用欧拉公式，也就是要进行指对数运算，也大抵只能使用double
public class ComplexNumber extends MathSign {


    public static final int FORMAT_ALGEBRA = 0;
    public static final int FORMAT_SOLAR = 1;

    public final static ComplexNumber I = new ComplexNumber(0.0, 1.0, true);
    public final static ComplexNumber E = new ComplexNumber(Math.E, 0.0, true);
    public final static ComplexNumber PI = new ComplexNumber(Math.PI, 0.0, true);

    private double mReal, mImaginary;
    private double mModulus, mArgument;

    public ComplexNumber(ComplexNumber num) {
        mReal = num.mReal;
        mImaginary = num.mImaginary;
        mModulus = num.mModulus;
        mArgument = num.mArgument;
    }

    public ComplexNumber(double a, double b, boolean isReAndIm) {
        if (isReAndIm) {
            mReal = a;
            mImaginary = b;
            mModulus = Math.hypot(a, b);
            mArgument = Math.atan2(b, a);
        } else {
            mReal = a * Math.cos(b);
            mImaginary = a * Math.sin(b);
            mModulus = a >= 0.0 ? a : -1.0 * a;
            b = a >= 0 ? b : b + Math.PI;
            b -= 2.0 * Math.PI * Math.ceil(b / (2.0 * Math.PI));
            mArgument = b <= Math.PI ? b : b - 2.0 * Math.PI;
        }
    }

    public static ComplexNumber exp(ComplexNumber z) {
        return new ComplexNumber(Math.exp(z.mReal), z.mImaginary, false);
    }

    private static String doubleToString(double d) {
        if (d == Double.POSITIVE_INFINITY) {
            return "+∞";
        } else if (d == Double.NEGATIVE_INFINITY) {
            return "-∞";
        } else if ((Double.isNaN(d))) {
            return "无意义";
        }
        MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
        String output = new BigDecimal(d).round(mc).toString();
        Matcher matcher = Pattern.compile("(.+)(\\.)([0-9]*[1-9])?(0+)(E.+)?").matcher(output);
        if (matcher.matches()) {
            output = matcher.group(1) +
                    (matcher.group(3) == null ? "" : "." + matcher.group(3)) +
                    (matcher.group(5) == null ? "" : matcher.group(5));
        }
        return output;
    }

    public double getReal() {
        return mReal;
    }

    public double getImaginary() {
        return mImaginary;
    }

    public double getModulus() {
        return mModulus;
    }

    public double getArgument() {
        return mArgument;
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(mReal, (-1.0) * mImaginary, true);
    }

    public ComplexNumber add(ComplexNumber z) {
        return new ComplexNumber(mReal + z.mReal, mImaginary + z.mImaginary, true);
    }

    public ComplexNumber subtract(ComplexNumber z) {
        return new ComplexNumber(mReal - z.mReal, mImaginary - z.mImaginary, true);
    }

    public ComplexNumber multiply(ComplexNumber z) {
        return new ComplexNumber(mReal * z.mReal - mImaginary * z.mImaginary,
                mReal * z.mImaginary + mImaginary * z.mReal, true);
    }

    public ComplexNumber divide(ComplexNumber z) {
        double modSquare = z.mModulus * z.mModulus;
        return new ComplexNumber((mReal * z.mReal + mImaginary * z.mImaginary) / modSquare,
                (mImaginary * z.mReal - mReal * z.mImaginary) / modSquare, true);
    }

    public ComplexNumber pow(ComplexNumber z) {
        double mod = Math.exp(Math.log(mModulus) * z.mReal - mArgument * z.mImaginary);
        double arg = Math.log(mModulus) * z.mImaginary + mArgument * z.mReal;
        return new ComplexNumber(mod, arg, false);
    }

    public ComplexNumber ln() {
        return new ComplexNumber(Math.log(mModulus), mArgument, true);
    }

    public ComplexNumber sin() {
        double re = (Math.exp(mImaginary) + Math.exp(-1.0 * mImaginary)) / 2.0 * Math.sin(mReal);
        double im = (Math.exp(mImaginary) - Math.exp(-1.0 * mImaginary)) / 2.0 * Math.cos(mReal);
        return new ComplexNumber(re, im, true);
    }

    public ComplexNumber cos() {
        double re = (Math.exp(mImaginary) + Math.exp(-1.0 * mImaginary)) / 2.0 * Math.cos(mReal);
        double im = (Math.exp(-1.0 * mImaginary) - Math.exp(mImaginary)) / 2.0 * Math.sin(mReal);
        return new ComplexNumber(re, im, true);
    }

    @Override
    public String toString() {
        return mReal + "+i" + mImaginary;
    }

    public String formatToString(int angularUnit, int resultFormat) {
        switch (resultFormat) {
            case FORMAT_SOLAR:
                if (angularUnit == ComplexMathSignQueue.ANGULAR_DEGREE) {
                    double d = mArgument / Math.PI * 180.0;
                    return doubleToString(mModulus) + "∠(" + doubleToString(d) + ")";
                } else if (angularUnit == ComplexMathSignQueue.ANGULAR_RADIAN) {
                    return doubleToString(mModulus) + "∠(" + doubleToString(mArgument) + ")";
                } else {
                    double d = mArgument / Math.PI;
                    return doubleToString(mModulus) + "∠(" + doubleToString(d) + "π)";
                }
            case FORMAT_ALGEBRA:
            default:
                String im = doubleToString(mImaginary);
                if (im.equals("0")) {
                    return doubleToString(mReal);
                } else {
                    return doubleToString(mReal) + "+i(" + im + ")";
                }
        }
    }
}
