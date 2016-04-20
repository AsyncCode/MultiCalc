package com.example.multicalc.matrix.math;

import com.example.multicalc.basic_calc.math.CalcException;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从使用者输入的角度讲，只可能输入有理数，因为有限长度的数必定是有理数
 * 而线性运算不可能从有理数产生无理数，所以只设计有理数类用于矩阵运算
 * 另外从实际使用的角度，线代中也大都以整数或分数为数据，故而讲数据设计为有理数形式为最优方案
 *
 * 至于选用BigInteger而非long作为存储分子分母的数据类型，从性能和可靠性两方面考虑如下：
 * 诚然BigInteger运算速度不如long,不过在绝大多数，大家基本是计算3到6阶左右的矩阵，此时性能影响察觉不到
 * 而假如有某些特殊情况，需要计算大矩阵或者大元素矩阵，long则会溢出,BigInteger虽然慢但是是可靠的
 * 综上，使用BigInteger存储分子分母的数据
 *
 * 在此每个函数的功能不再一一赘述，基本上函数名就解释了所做运算
 */
public class RationalNumber implements Serializable {

    public final static RationalNumber ZERO = new RationalNumber(0L);
    public final static RationalNumber ONE = new RationalNumber(1L);

    private BigInteger mNumerator;
    private BigInteger mDenominator;

    private RationalNumber() {
    }

    public RationalNumber(long num) {
        mNumerator = BigInteger.valueOf(num);
        mDenominator = BigInteger.ONE;
    }

    public static RationalNumber parse(String s) throws CalcException {
        String[] parts = s.split("/");
        if (parts.length > 1) {
            RationalNumber num = parse(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                num = num.divide(parse(parts[i]));
            }
            return num;
        } else {
            Matcher m = Pattern.compile("([+-]?[0-9]+)(\\.[0-9]+)?").matcher(s.replaceAll("\\s", ""));
            if (m.matches()) {
                RationalNumber num = new RationalNumber();
                if (m.group(2) != null) {
                    num.mNumerator = new BigInteger(m.group(1) + m.group(2).substring(1));
                    num.mDenominator = BigInteger.TEN.pow(m.group(2).length() - 1);
                } else {
                    num.mNumerator = new BigInteger(m.group(1));
                    num.mDenominator = BigInteger.ONE;
                }
                return num.reduce();
            } else {
                throw new CalcException("输入数字为空，或者格式不正确，只允许小数或分数形式");
            }
        }
    }

    public RationalNumber reduce() {
        BigInteger gcd = mNumerator.gcd(mDenominator);
        gcd = (gcd.compareTo(BigInteger.ZERO) == 0 ? BigInteger.ONE : gcd);
        gcd = mDenominator.compareTo(BigInteger.ZERO) < 0 ? gcd.negate() : gcd;
        mDenominator = mDenominator.divide(gcd);
        mNumerator = mNumerator.divide(gcd);
        return this;
    }

    public int intValue() throws CalcException {
        if (reduce().mDenominator.compareTo(BigInteger.ONE) == 0) {
            return mNumerator.intValue();
        } else {
            throw new CalcException("数据不是整数");
        }
    }

    public RationalNumber negative() {
        return ZERO.subtract(this);
    }

    public RationalNumber reciprocal() {
        return ONE.divide(this);
    }

    public RationalNumber add(RationalNumber addend) {
        RationalNumber sum = new RationalNumber();
        sum.mDenominator = mDenominator.multiply(addend.mDenominator);
        sum.mNumerator = mNumerator.multiply(addend.mDenominator)
                .add(mDenominator.multiply(addend.mNumerator));
        return sum.reduce();
    }

    public RationalNumber subtract(RationalNumber minuend) {
        RationalNumber difference = new RationalNumber();
        difference.mDenominator = mDenominator.multiply(minuend.mDenominator);
        difference.mNumerator = mNumerator.multiply(minuend.mDenominator)
                .subtract(mDenominator.multiply(minuend.mNumerator));
        return difference.reduce();
    }

    public RationalNumber multiply(RationalNumber multiplier) {
        RationalNumber product = new RationalNumber();
        product.mDenominator = mDenominator.multiply(multiplier.mDenominator);
        product.mNumerator = mNumerator.multiply(multiplier.mNumerator);
        return product.reduce();
    }

    public RationalNumber divide(RationalNumber divisor) {
        RationalNumber quotient = new RationalNumber();
        quotient.mDenominator = mDenominator.multiply(divisor.mNumerator);
        quotient.mNumerator = mNumerator.multiply(divisor.mDenominator);
        return quotient.reduce();
    }

    public RationalNumber pow(int exp) {
        RationalNumber result = new RationalNumber();
        if (exp < 0) {
            result.mDenominator = mNumerator;
            result.mNumerator = mDenominator;
            exp *= -1;
        } else {
            result.mDenominator = mDenominator;
            result.mNumerator = mNumerator;
        }
        result.mNumerator = result.mNumerator.pow(exp);
        result.mDenominator = result.mDenominator.pow(exp);
        return result;
    }

    public int compareToZero() {
        return mDenominator.compareTo(BigInteger.ZERO) * mNumerator.compareTo(BigInteger.ZERO);
    }

    @Override
    public String toString() {
        reduce();
        if (mNumerator.compareTo(BigInteger.ZERO) == 0) {
            return "0";
        } else if (mDenominator.compareTo(BigInteger.ONE) == 0) {
            return mNumerator.toString();
        } else {
            return mNumerator.toString() + "/" + mDenominator.toString();
        }
    }
}