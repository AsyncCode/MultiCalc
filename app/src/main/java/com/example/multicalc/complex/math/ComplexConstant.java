package com.example.multicalc.complex.math;

import java.util.HashMap;

/**
 * ComplexConstant之于ComplexNumber就如Constant之于RealNumber，不过不支持自定义
 */
public class ComplexConstant extends ComplexNumber {

    public final static HashMap<String, ComplexNumber> CONSTANTS = new HashMap<>();

    static {
        CONSTANTS.put("i", I);
        CONSTANTS.put("e", E);
        CONSTANTS.put("π", PI);
    }

    private String mName = null;

    public ComplexConstant(String str) {
        super(CONSTANTS.get(str));
        mName = str;
    }

    @Override
    public String toString() {
        return mName;
    }
}
