package com.example.multicalc.basic_calc.math;

import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实数类，既不是一般的double也不是BigDecimal,而是分两种情况，当数据是精确的，譬如输入的有理数（事实上，
 * 通过使用者键入的也只有有理数）以及加减乘除等，精确的数据被分别存储与分子和分母两个BigInteger中。
 * 当数据不精确时，譬如开不尽方，求三角函数等等，则自动转换为用double存储的一个无不精确数
 * 且有如下规则：只有多个被操作数与所进行的运算都是可精确的时候，才会返回一个精确的结果
 */
public class RealNumber extends MathSign {

    public static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    public static final int FORMAT_NORMAL = 0;
    public static final int FORMAT_FRACTION = 1;
    public static final int FORMAT_SCIENTIFIC = 2;

    private boolean mIsRational;
    private BigInteger mNumerator;
    private BigInteger mDenominator;
    private double mDoubleValue;

    private RealNumber() {
    }

    public RealNumber(double d) {
        mIsRational = false;
        mDoubleValue = d;
    }

    public RealNumber(String s) {
        Matcher m = Pattern.compile("([+-]?[0-9]+)(\\.[0-9]+)?(E[+-]?[0-9]+)?").matcher(s);
        if (m.matches()) {
            mIsRational = true;
            if (m.group(2) != null) {
                mNumerator = new BigInteger(m.group(1) + m.group(2).substring(1));
                mDenominator = BigInteger.TEN.pow(m.group(2).length() - 1);
            } else {
                mNumerator = new BigInteger(m.group(1));
                mDenominator = BigInteger.ONE;
            }
            String exp = m.group(3);
            if (exp != null) {
                if (exp.charAt(1) == '-') {
                    mDenominator = new BigDecimal(mDenominator)
                            .multiply(new BigDecimal("1E" + exp.substring(2))).toBigIntegerExact();
                } else {
                    mNumerator = new BigDecimal(mNumerator)
                            .multiply(new BigDecimal("1E" + exp.substring(1))).toBigIntegerExact();
                }
            }
        }
    }

    //试着求一个BigInteger的根，采用的方法是利用与之相近的double求根，然后再这个近似根试着求精确根
    // 若不存在返回null
    public static BigInteger integerRoot(BigInteger x, int y) {
        BigInteger approximateRoot =
                new BigDecimal(Math.pow(x.doubleValue(), 1.0 / y)).toBigInteger();
        switch (approximateRoot.pow(y).compareTo(x)) {
            case 0:
                return approximateRoot;
            case 1:
                do {
                    approximateRoot = approximateRoot.subtract(BigInteger.ONE);
                } while (approximateRoot.pow(y).compareTo(x) == 1);
                break;
            case -1:
                do {
                    approximateRoot = approximateRoot.add(BigInteger.ONE);
                } while (approximateRoot.pow(y).compareTo(x) == -1);
                break;
        }
        if (approximateRoot.pow(y).compareTo(x) == 0) {
            return approximateRoot;
        } else {
            return null;
        }
    }

    //求数的阶乘
    public static RealNumber chainMultiplication(int m, int n) {
        BigInteger product = BigInteger.valueOf(m);
        for (m++; m <= n; m++) {
            product = product.multiply(BigInteger.valueOf(m));
        }
        RealNumber result = new RealNumber();
        result.mIsRational = true;
        result.mNumerator = product;
        result.mDenominator = BigInteger.ONE;
        return result;
    }

    //返回double值，在调用java.lang.Math类中的许多方法会用到
    public double doubleValue() {
        if (mIsRational) {
            return new BigDecimal(mNumerator)
                    .divide(new BigDecimal(mDenominator), MathContext.DECIMAL64).doubleValue();
        } else {
            return mDoubleValue;
        }
    }

    //返回精确的int值，当数本身不精确或者不能精确为整数（包括为分数或超过最大整数）的，抛异常
    public int intValueExactly() throws CalcException {
        if (mIsRational && reduce().mDenominator.equals(BigInteger.ONE)) {
            if (mNumerator.abs().compareTo(MAX_INT) > 0) {
                throw new CalcException("数据过大", this);
            }
            return mNumerator.intValue();
        }
        throw new CalcException("数据不是整数", this);
    }

    //化简，即约分操作
    public RealNumber reduce() {
        if (mIsRational) {
            BigInteger gcd = mNumerator.gcd(mDenominator);
            gcd = (gcd.compareTo(BigInteger.ZERO) == 0 ? BigInteger.ONE : gcd);
            gcd = mDenominator.compareTo(BigInteger.ZERO) < 0 ? gcd.negate() : gcd;
            mDenominator = mDenominator.divide(gcd);
            mNumerator = mNumerator.divide(gcd);
            return this;
        }
        return this;
    }

    public int compareToZero() {
        if (mIsRational) {
            if (mDenominator.compareTo(BigInteger.ZERO) > 0) {
                return mNumerator.compareTo(BigInteger.ZERO);
            } else {
                return (-1) * mNumerator.compareTo(BigInteger.ZERO);
            }
        } else {
            return Double.compare(mDoubleValue, 0.0);
        }
    }

    public RealNumber add(RealNumber addend) {
        RealNumber sum = new RealNumber();
        if (mIsRational && addend.mIsRational) {
            sum.mIsRational = true;
            sum.mDenominator = mDenominator.multiply(addend.mDenominator);
            sum.mNumerator = mNumerator.multiply(addend.mDenominator)
                    .add(mDenominator.multiply(addend.mNumerator));
        } else {
            sum.mIsRational = false;
            sum.mDoubleValue = doubleValue() + addend.doubleValue();
        }
        return sum.reduce();
    }

    public RealNumber subtract(RealNumber minuend) {
        RealNumber difference = new RealNumber();
        if (mIsRational && minuend.mIsRational) {
            difference.mIsRational = true;
            difference.mDenominator = mDenominator.multiply(minuend.mDenominator);
            difference.mNumerator = mNumerator.multiply(minuend.mDenominator)
                    .subtract(mDenominator.multiply(minuend.mNumerator));
        } else {
            difference.mIsRational = false;
            difference.mDoubleValue = doubleValue() - minuend.doubleValue();
        }
        return difference.reduce();
    }

    public RealNumber multiply(RealNumber multiplier) {
        RealNumber product = new RealNumber();
        if (mIsRational && multiplier.mIsRational) {
            product.mIsRational = true;
            product.mDenominator = mDenominator.multiply(multiplier.mDenominator);
            product.mNumerator = mNumerator.multiply(multiplier.mNumerator);
        } else {
            product.mIsRational = false;
            product.mDoubleValue = doubleValue() * multiplier.doubleValue();
        }
        return product.reduce();
    }

    public RealNumber divide(RealNumber divisor) throws CalcException {
        RealNumber quotient = new RealNumber();
        if (mIsRational && divisor.mIsRational) {
            if (divisor.mNumerator.equals(BigInteger.ZERO)) {
                throw new CalcException("除数为0", divisor);
            }
            quotient.mIsRational = true;
            quotient.mDenominator = mDenominator.multiply(divisor.mNumerator);
            quotient.mNumerator = mNumerator.multiply(divisor.mDenominator);
        } else {
            quotient.mIsRational = false;
            quotient.mDoubleValue = doubleValue() / divisor.doubleValue();
        }
        return quotient.reduce();
    }

    //求幂操作，这个函数会试着求精确结果，而实践表明，某些情况下试着求精确根，当数据太长，会由于计算量太大卡死
    //故而新开一个线程计算之，若过于耗时，则放弃求精确结果的尝试，转而获得一个不精确的double结果
    public RealNumber pow(final RealNumber exponent) throws CalcException {
        final RealNumber result = new RealNumber();
        if (mIsRational && exponent.mIsRational) {
            switch (exponent.compareToZero()) {
                case 0:
                    if (this.compareToZero() == 0) {
                        throw new CalcException("0的0次方无意义", this, exponent);
                    } else {
                        result.mIsRational = true;
                        result.mDenominator = result.mNumerator = BigInteger.ONE;
                        return result;
                    }
                case -1:
                    BigInteger tmp = mNumerator;
                    mNumerator = mDenominator;
                    mDenominator = tmp;
                    exponent.mNumerator = exponent.mNumerator.abs();
                    exponent.mDenominator = exponent.mDenominator.abs();
                    break;
                default:
                    break;
            }
            this.reduce();
            exponent.reduce();
            if (this.compareToZero() == -1
                    && exponent.mDenominator.mod(new BigInteger("2")).equals(BigInteger.ZERO)) {
                throw new CalcException("负数的偶次方无意义", this);
            }
            Thread tryToGetPreciseResult = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (exponent.mDenominator.compareTo(MAX_INT) > 0 ||
                            exponent.mNumerator.compareTo(MAX_INT) > 0) {
                        result.mIsRational = false;
                        result.mDoubleValue = Math.pow(doubleValue(), exponent.doubleValue());
                    } else {
                        BigInteger basicNumRoot =
                                integerRoot(mNumerator, exponent.mDenominator.intValue());
                        BigInteger basicDenRoot =
                                integerRoot(mDenominator, exponent.mDenominator.intValue());
                        if (basicNumRoot == null || basicDenRoot == null) {
                            result.mDoubleValue = Math.pow(doubleValue(), exponent.doubleValue());
                            result.mIsRational = false;
                        } else {
                            result.mNumerator = basicNumRoot.pow(exponent.mNumerator.intValue());
                            result.mDenominator = basicDenRoot.pow(exponent.mNumerator.intValue());
                            result.mIsRational = true;
                        }
                    }
                }
            });
            tryToGetPreciseResult.setPriority(Thread.MAX_PRIORITY);
            tryToGetPreciseResult.start();
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.d(getClass().getName(), Log.getStackTraceString(e));
                }
                if (!tryToGetPreciseResult.isAlive()) {
                    break;
                }
            }
            if (tryToGetPreciseResult.isAlive()) {
                tryToGetPreciseResult.interrupt();
                result.mIsRational = false;
                result.mDoubleValue = Math.pow(doubleValue(), exponent.doubleValue());
            }
            return result;
        } else {
            result.mIsRational = false;
            result.mDoubleValue = Math.pow(doubleValue(), exponent.doubleValue());
            return result;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(doubleValue());
    }

    //按照要求的格式返回数字的表达形式，结果取12位精度且消除尾部的0
    //double的有效为数为16位，考虑计算过程的误差，设置12位精度的结果，是可靠且足够的
    public String formatToString(int format) {
        MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
        String output;
        if (mIsRational) {
            switch (format) {
                case FORMAT_FRACTION:
                    reduce();
                    return mNumerator.toString() + (mDenominator.equals(BigInteger.ONE) ? "" :
                            "/" + mDenominator.toString());
                case FORMAT_SCIENTIFIC:
                    //精确结果(mIsRational == true)，若数据指数位比较大，不可转double，否则会损失为0或无穷大
                    output = new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator), mc)
                            .stripTrailingZeros().toString();
                    //但BigDecimal的toString有一个局限性，就是指数位不大时不用科学计数法（表现位s不含'E'）
                    // 不过这时指数位一定不大，可以获得double结果，如同mIsPrecise == false一样处理
                    if (!output.contains("E")) {
                        output = new DecimalFormat("#.###########E0").format(doubleValue());
                    }
                    break;
                case FORMAT_NORMAL:
                default:
                    output = new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator), mc)
                            .toString();
                    break;
            }
        } else {
            //先处理double中一些特殊意义的数
            if (mDoubleValue == Double.POSITIVE_INFINITY) {
                return "+∞";
            } else if (mDoubleValue == Double.NEGATIVE_INFINITY) {
                return "-∞";
            } else if ((Double.isNaN(mDoubleValue))) {
                return "无意义";
            }
            switch (format) {
                case FORMAT_SCIENTIFIC:
                    output = new DecimalFormat("#.###########E0").format(doubleValue());
                    break;
                case FORMAT_FRACTION:
                case FORMAT_NORMAL:
                default:
                    output = new BigDecimal(mDoubleValue).round(mc).toString();
                    break;
            }
        }
        Matcher matcher = Pattern.compile("(.+)(\\.)([0-9]*[1-9])?(0+)(E.+)?").matcher(output);
        if (matcher.matches()) {
            output = matcher.group(1) +
                    (matcher.group(3) == null ? "" : "." + matcher.group(3)) +
                    (matcher.group(5) == null ? "" : matcher.group(5));
        }
        return output;
    }
}