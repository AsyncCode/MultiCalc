package com.example.multicalc.matrix.math;

import com.example.multicalc.basic_calc.math.CalcException;

import java.io.Serializable;

/**
 * 构建了一个数学运算层面上的矩阵类，很自然地，用一个二维数组存储矩阵的数据
 */
public class Matrix implements Serializable {

    private RationalNumber[][] mElements;
    private int mRowCount;
    private int mColumnCount;

    public Matrix(Matrix from) {
        mRowCount = from.mRowCount;
        mColumnCount = from.mColumnCount;
        mElements = new RationalNumber[mRowCount][mColumnCount];
        for (int i = 0; i < mRowCount; i++) {
            mElements[i] = new RationalNumber[mColumnCount];
            System.arraycopy(from.mElements[i], 0, mElements[i], 0, mColumnCount);
        }
    }

    public Matrix(int row, int column) {
        mRowCount = row;
        mColumnCount = column;
        mElements = new RationalNumber[row][column];
    }

    public Matrix(RationalNumber[][] elements) {
        mElements = elements;
        mRowCount = elements.length;
        mColumnCount = elements[0].length;
    }

    //静态方法，返回一个n阶单位矩阵
    public static Matrix identityMatrix(int dimen) {
        Matrix identity = new Matrix(dimen, dimen);
        for (int i = 0; i < identity.mRowCount; i++) {
            for (int j = 0; j < identity.mColumnCount; j++) {
                identity.mElements[i][j] = (i == j ? RationalNumber.ONE : RationalNumber.ZERO);
            }
        }
        return identity;
    }

    //两个矩阵水平合成为一个大矩阵
    public static Matrix combine(Matrix a, Matrix b) throws CalcException {
        if (a.mRowCount != b.mRowCount) {
            throw new CalcException("行数不同不能结合为大矩阵");
        }
        Matrix big = new Matrix(a.mRowCount, a.mColumnCount + b.mColumnCount);
        for (int i = 0; i < a.mRowCount; i++) {
            System.arraycopy(a.mElements[i], 0, big.mElements[i], 0, a.mColumnCount);
        }
        for (int i = 0; i < b.mRowCount; i++) {
            System.arraycopy(b.mElements[i], 0, big.mElements[i], a.mColumnCount, b.mColumnCount);
        }
        return big;
    }

    public int row() {
        return mRowCount;
    }

    public int column() {
        return mColumnCount;
    }

    public RationalNumber element(int i, int j) {
        return mElements[i][j];
    }

    //返回处于特定行、列的一个子矩阵
    public Matrix subMatrix(int[] rows, int[] columns) {
        Matrix cofactor = new Matrix(rows.length, columns.length);
        for (int i = 0; i < cofactor.mRowCount; i++) {
            for (int j = 0; j < cofactor.mColumnCount; j++) {
                cofactor.mElements[i][j] = mElements[rows[i]][columns[j]];
            }
        }
        return cofactor;
    }

    //代数余子式
    public RationalNumber cofactor(int i, int j) throws CalcException {
        int[] rows = new int[mRowCount - 1];
        int[] columns = new int[mColumnCount - 1];
        for (int k = 0, delete = 0; k < mRowCount; k++) {
            if (k != i) {
                rows[k - delete] = k;
            } else {
                delete = 1;
            }
        }
        for (int k = 0, delete = 0; k < mColumnCount; k++) {
            if (k != j) {
                columns[k - delete] = k;
            } else {
                delete = 1;
            }
        }
        if ((i + j) % 2 == 0) {
            return subMatrix(rows, columns).det();
        } else {
            return subMatrix(rows, columns).det().negative();
        }
    }

    //矩阵的行列式
    public RationalNumber det() throws CalcException {
        if (mRowCount != mColumnCount) {
            throw new CalcException("求行列式必须是方阵");
        } else if (mRowCount == 1) {
            return mElements[0][0];
        } else if (mRowCount == 2) {
            return mElements[0][0].multiply(mElements[1][1])
                    .subtract(mElements[0][1].multiply(mElements[1][0]));
        } else {
            RationalNumber det = RationalNumber.ZERO;
            for (int j = 0; j < mColumnCount; j++) {
                det = det.add(mElements[0][j].multiply(cofactor(0, j)));
            }
            return det;
        }
    }

    //返回转置矩阵
    public Matrix trans() {
        Matrix trans = new Matrix(mColumnCount, mRowCount);
        for (int i = 0; i < trans.mRowCount; i++) {
            for (int j = 0; j < trans.mColumnCount; j++) {
                trans.mElements[i][j] = mElements[j][i];
            }
        }
        return trans;
    }

    //返回伴随矩阵
    public Matrix adjoint() throws CalcException {
        if (mRowCount != mColumnCount) {
            throw new CalcException("求伴随必须是方阵");
        } else {
            Matrix adj = new Matrix(mRowCount, mColumnCount);
            for (int i = 0; i < adj.mRowCount; i++) {
                for (int j = 0; j < adj.mColumnCount; j++) {
                    adj.mElements[i][j] = cofactor(i, j);
                }
            }
            return adj.trans();
        }
    }

    //矩阵的逆
    public Matrix inv() throws CalcException {
        if (mRowCount != mColumnCount) {
            throw new CalcException("求逆矩阵必须是方阵");
        } else {
            RationalNumber det = det();
            if (det.compareToZero() == 0) {
                throw new CalcException("行列式为0的矩阵不可逆");
            }
            return adjoint().scale(det.reciprocal());
        }
    }

    //矩阵数乘
    public Matrix scale(RationalNumber num) {
        Matrix result = new Matrix(mRowCount, mColumnCount);
        for (int i = 0; i < result.mRowCount; i++) {
            for (int j = 0; j < result.mColumnCount; j++) {
                result.mElements[i][j] = mElements[i][j].multiply(num);
            }
        }
        return result;
    }

    //返回矩阵对应的行最简矩阵
    public Matrix rref() {
        Matrix simple = new Matrix(this);
        int row = 0;
        for (int column = 0; column < simple.mColumnCount; column++) {
            int nonZeroRow = row;
            while (nonZeroRow < simple.mRowCount &&
                    simple.mElements[nonZeroRow][column].compareToZero() == 0) {
                nonZeroRow++;
            }
            if (nonZeroRow >= simple.mRowCount) {
                continue;
            }
            if (nonZeroRow != row) {
                RationalNumber[] tmp = simple.mElements[nonZeroRow];
                simple.mElements[nonZeroRow] = simple.mElements[row];
                simple.mElements[row] = tmp;
            }
            RationalNumber factor = simple.mElements[row][column];
            for (int j = column; j < simple.mColumnCount; j++) {
                simple.mElements[row][j] = simple.mElements[row][j].divide(factor);
            }
            for (int i = 0; i < simple.mRowCount; i++) {
                if (i != row) {
                    factor = simple.mElements[i][column].negative();
                    for (int j = column; j < simple.mColumnCount; j++) {
                        simple.mElements[i][j] = simple.mElements[i][j]
                                .add(simple.mElements[row][j].multiply(factor));
                    }
                }
            }
            row++;
        }
        return simple;
    }

    //矩阵的秩
    public int rank() {
        int rank = mRowCount;
        Matrix rrefed = rref();
        for (int i = mRowCount - 1; i >= 0; i--, rank--) {
            for (int j = mColumnCount - 1; j >= 0; j--) {
                if (rrefed.mElements[i][j].compareToZero() != 0) {
                    return rank;
                }
            }
        }
        return 0;
    }

    //矩阵和
    public Matrix add(Matrix addend) throws CalcException {
        if (mRowCount != addend.mRowCount || mColumnCount != addend.mColumnCount) {
            throw new CalcException("行列数目不一致不能相加");
        }
        Matrix sum = new Matrix(mRowCount, mColumnCount);
        for (int i = 0; i < sum.mRowCount; i++) {
            for (int j = 0; j < sum.mColumnCount; j++) {
                sum.mElements[i][j] = mElements[i][j].add(addend.mElements[i][j]);
            }
        }
        return sum;
    }

    //矩阵相减
    public Matrix subtract(Matrix minuend) throws CalcException {
        if (mRowCount != minuend.mRowCount || mColumnCount != minuend.mColumnCount) {
            throw new CalcException("行列数目不一致不能相减");
        }
        Matrix difference = new Matrix(mRowCount, mColumnCount);
        for (int i = 0; i < difference.mRowCount; i++) {
            for (int j = 0; j < difference.mColumnCount; j++) {
                difference.mElements[i][j] = mElements[i][j].subtract(minuend.mElements[i][j]);
            }
        }
        return difference;
    }

    //矩阵相乘
    public Matrix multiply(Matrix multiplier) throws CalcException {
        if (mColumnCount != multiplier.mRowCount) {
            throw new CalcException("左矩阵列数等于右者行数才能相乘");
        }
        Matrix product = new Matrix(mRowCount, multiplier.mColumnCount);
        for (int i = 0; i < product.mRowCount; i++) {
            for (int j = 0; j < product.mColumnCount; j++) {
                product.mElements[i][j] = RationalNumber.ZERO;
                for (int k = 0; k < mColumnCount; k++) {
                    product.mElements[i][j] = product.mElements[i][j]
                            .add(mElements[i][k].multiply(multiplier.mElements[k][j]));
                }
            }
        }
        return product;
    }

    //矩阵的幂
    public Matrix pow(int exp) throws CalcException {
        if (mRowCount != mColumnCount) {
            throw new CalcException("方阵才能求幂");
        }
        Matrix pow = Matrix.identityMatrix(mRowCount);
        Matrix basic = this;
        if (exp < 0) {
            try {
                basic = this.inv();
            } catch (CalcException e) {
                throw new CalcException("需要可逆矩阵才能求负数次幂");
            }
            exp *= -1;
        }
        while (exp > 0) {
            if (exp % 2 == 1) {
                pow = pow.multiply(basic);
            }
            exp /= 2;
            basic = basic.multiply(basic);
        }
        return pow;
    }
}