package com.example.multicalc.matrix.math;

import java.util.LinkedHashMap;

/**
 * MatrixVariable之于RationalNumber类似Constant之于RealNumber
 * 整个矩阵运算模都试图在界面风格与输入输出方面接近MATLAB，这里的Variable届类似MATLAB中的矩阵变量
 */
public class MatrixVariable extends MatrixOrNumber{

    public static LinkedHashMap<String, MatrixOrNumber> VARIABLES = new LinkedHashMap<>();
    private String mName;
    private boolean mInitialized;

    public MatrixVariable(String name) {
        super(VARIABLES.get(name));
        mInitialized = VARIABLES.get(name) != null;
        mName = name;
    }

    public boolean initialized() {
        return mInitialized;
    }

    @Override
    public String toString() {
        return mName;
    }
}
