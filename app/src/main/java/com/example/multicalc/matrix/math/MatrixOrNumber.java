package com.example.multicalc.matrix.math;

import com.example.multicalc.basic_calc.math.CalcException;

import java.io.Serializable;

/**
 * 不管是矩阵还是数，都不时候直接交给运算符或者函数操作，有以下几个方面的考虑：
 * 经常或有矩阵与数的混合运算，如数乘矩阵，矩阵乘数，数数相乘，矩阵相乘，若直接判断在选择相应运算执行，相当复杂
 * 有时一个操作输入一个数却返回矩阵（如获得单位制），有时输入为矩阵却返回为数（如求秩）
 * 一阶方阵数学上定义为一个数，却又与矩阵有很多联系（譬如矩阵之积为矩阵，但行向量和列向量之积所得为数（一阶方阵））
 *
 * 所以构建这么一个类，矩阵与数的差异被封装在类中，外界调用的时候不加区分，此类自动根据左右操作数选择操作
 * 类中的方法基本是Matrix类的同名方法，即为对该方法的再包装
 */
public class MatrixOrNumber implements MatrixSign, Serializable {

    private boolean mIsMatrix;
    private Matrix mMatrix;
    private RationalNumber mNumber;

    private MatrixOrNumber() {
    }

    public MatrixOrNumber(Matrix m) {
        mIsMatrix = true;
        mMatrix = m;
    }

    public MatrixOrNumber(RationalNumber[][] ns) {
        mIsMatrix = true;
        mMatrix = new Matrix(ns);
    }

    public MatrixOrNumber(RationalNumber n) {
        mIsMatrix = false;
        mNumber = n;
    }

    public MatrixOrNumber(MatrixOrNumber mOn) {
        if (mOn != null) {
            mIsMatrix = mOn.mIsMatrix;
            mMatrix = mOn.mMatrix;
            mNumber = mOn.mNumber;
        }
    }

    public static MatrixOrNumber combine(MatrixOrNumber a, MatrixOrNumber b, boolean isHorizontal)
            throws CalcException {
        if (!a.mIsMatrix) {
            a.mMatrix = new Matrix(new RationalNumber[][]{{a.mNumber}});
        }
        if (!b.mIsMatrix) {
            b.mMatrix = new Matrix(new RationalNumber[][]{{b.mNumber}});
        }
        if (isHorizontal) {
            return new MatrixOrNumber(Matrix.combine(a.mMatrix, b.mMatrix));
        } else {
            return new MatrixOrNumber(
                    Matrix.combine(a.mMatrix.trans(), b.mMatrix.trans()).trans());
        }
    }

    public int row() {
        return mIsMatrix ? mMatrix.row() : 1;
    }

    public int column() {
        return mIsMatrix ? mMatrix.column() : 1;
    }

    public RationalNumber element(int i, int j) {
        return mIsMatrix ? mMatrix.element(i, j) : mNumber;
    }

    public void firstOrderMatrixToNumber() {
        if (mIsMatrix && mMatrix.row() == 1 && mMatrix.column() == 1) {
            mIsMatrix = false;
            mNumber = mMatrix.element(0, 0);
        }
    }

    public MatrixOrNumber adjoint() throws CalcException {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(mMatrix.adjoint());
        } else {
            return new MatrixOrNumber(RationalNumber.ONE);
        }
    }

    public MatrixOrNumber subMatrix(int[] rows, int[] columns) throws CalcException {
        if (rows != null && rows.length > 0 && columns != null && columns.length > 0) {
            return mIsMatrix ? new MatrixOrNumber(mMatrix.subMatrix(rows, columns)) :
                    new MatrixOrNumber(mNumber);
        } else {
            throw new CalcException("至少需要选择一行且一列");
        }
    }

    public MatrixOrNumber cofactor(int i, int j) throws CalcException {
        if (!mIsMatrix || mMatrix.row() < 2 || mMatrix.row() != mMatrix.column()) {
            throw new CalcException("只有二阶以上方阵才能求代数余子式");
        } else {
            return new MatrixOrNumber(mMatrix.cofactor(i, j));
        }
    }

    public MatrixOrNumber add(MatrixOrNumber addend) throws CalcException {
        firstOrderMatrixToNumber();
        addend.firstOrderMatrixToNumber();
        if (mIsMatrix && !addend.mIsMatrix || !mIsMatrix && addend.mIsMatrix) {
            throw new CalcException("矩阵不能与数相加");
        } else {
            MatrixOrNumber sum = new MatrixOrNumber();
            if (mIsMatrix) {
                sum.mIsMatrix = true;
                sum.mMatrix = mMatrix.add(addend.mMatrix);
            } else {
                sum.mIsMatrix = false;
                sum.mNumber = mNumber.add(addend.mNumber);
            }
            return sum;
        }
    }

    public MatrixOrNumber subtract(MatrixOrNumber minuend) throws CalcException {
        firstOrderMatrixToNumber();
        minuend.firstOrderMatrixToNumber();
        if (mIsMatrix && !minuend.mIsMatrix || !mIsMatrix && minuend.mIsMatrix) {
            throw new CalcException("矩阵不能与数相减");
        } else {
            MatrixOrNumber difference = new MatrixOrNumber();
            if (mIsMatrix) {
                difference.mIsMatrix = true;
                difference.mMatrix = mMatrix.subtract(minuend.mMatrix);
            } else {
                difference.mIsMatrix = false;
                difference.mNumber = mNumber.subtract(minuend.mNumber);
            }
            return difference;
        }
    }

    public MatrixOrNumber multiply(MatrixOrNumber multiplier) throws CalcException {
        firstOrderMatrixToNumber();
        multiplier.firstOrderMatrixToNumber();
        MatrixOrNumber product = new MatrixOrNumber();
        if (mIsMatrix && multiplier.mIsMatrix) {
            product.mIsMatrix = true;
            product.mMatrix = mMatrix.multiply(multiplier.mMatrix);
        }
        if (mIsMatrix && !multiplier.mIsMatrix) {
            product.mIsMatrix = true;
            product.mMatrix = mMatrix.scale(multiplier.mNumber);
        }
        if (!mIsMatrix && multiplier.mIsMatrix) {
            product.mIsMatrix = true;
            product.mMatrix = multiplier.mMatrix.scale(mNumber);
        }
        if (!mIsMatrix && !multiplier.mIsMatrix) {
            product.mIsMatrix = false;
            product.mNumber = mNumber.multiply(multiplier.mNumber);
        }
        return product;
    }

    public MatrixOrNumber divide(MatrixOrNumber divisor) throws CalcException {
        firstOrderMatrixToNumber();
        divisor.firstOrderMatrixToNumber();
        MatrixOrNumber quotient = new MatrixOrNumber();
        if (mIsMatrix && divisor.mIsMatrix) {
            quotient.mIsMatrix = true;
            try {
                quotient.mMatrix = mMatrix.multiply(divisor.mMatrix.inv());
            } catch (CalcException e) {
                throw new CalcException("矩阵相除需要后者可逆且二者列数相等");
            }
        }
        if (mIsMatrix && !divisor.mIsMatrix) {
            quotient.mIsMatrix = true;
            quotient.mMatrix = mMatrix.scale(divisor.mNumber.reciprocal());
        }
        if (!mIsMatrix && divisor.mIsMatrix) {
            quotient.mIsMatrix = true;
            try {
                quotient.mMatrix = divisor.mMatrix.inv().scale(mNumber);
            } catch (CalcException e) {
                throw new CalcException("数除以矩阵需要后者可逆" + e.getDetail());
            }
        }
        if (!mIsMatrix && !divisor.mIsMatrix) {
            quotient.mIsMatrix = false;
            quotient.mNumber = mNumber.divide(divisor.mNumber);
        }
        return quotient;
    }

    public MatrixOrNumber pow(MatrixOrNumber exp) throws CalcException {
        firstOrderMatrixToNumber();
        exp.firstOrderMatrixToNumber();
        if (exp.mIsMatrix) {
            throw new CalcException("乘方指数位不能是矩阵");
        } else {
            int intExp;
            try {
                intExp = exp.mNumber.intValue();
            } catch (CalcException e) {
                throw new CalcException("乘方指数位的数不是整数");
            }
            MatrixOrNumber pow = new MatrixOrNumber();
            if (mIsMatrix) {
                pow.mIsMatrix = true;
                pow.mMatrix = mMatrix.pow(intExp);
            } else {
                pow.mIsMatrix = false;
                pow.mNumber = mNumber.pow(intExp);
            }
            return pow;
        }
    }

    public MatrixOrNumber det() throws CalcException {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(mMatrix.det());
        } else {
            return new MatrixOrNumber(mNumber);
        }
    }

    public MatrixOrNumber inv() throws CalcException {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(mMatrix.inv());
        } else {
            return new MatrixOrNumber(mNumber.reciprocal());
        }
    }

    public MatrixOrNumber trans() {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(mMatrix.trans());
        } else {
            return new MatrixOrNumber(mNumber);
        }
    }

    public MatrixOrNumber rank() {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(new RationalNumber(mMatrix.rank()));
        } else {
            return new MatrixOrNumber(new RationalNumber(mNumber.compareToZero() == 0 ? 0 : 1));
        }
    }

    public MatrixOrNumber rref() {
        firstOrderMatrixToNumber();
        if (mIsMatrix) {
            return new MatrixOrNumber(mMatrix.rref());
        } else {
            return new MatrixOrNumber(new RationalNumber(mNumber.compareToZero() == 0 ? 0 : 1));
        }
    }

    public String[][] toStrings() {
        int rowCount = row();
        int columnCount = column();
        String[][] strings = new String[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                strings[i][j] = element(i, j).toString();
            }
        }
        return strings;
    }
}
