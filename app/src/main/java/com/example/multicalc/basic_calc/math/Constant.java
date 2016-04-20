package com.example.multicalc.basic_calc.math;

import android.database.Cursor;

import java.util.HashMap;

/**
 * 常量类，继承自数字类，因为常量可以像数一样运算，又有自身的常量名之类的属性。
 * 另外常量除了内置部分，还应该支持扩充，通过数据库获取用户自定义值。
 * 事实上，几乎所有数学物理常量都是无理数，所以大可都用不精确的形式表示，以double存储即可。
 */
public class Constant extends RealNumber {

    //将Constant的名与值储存在HashMap中
    public final static HashMap<String, Double> CONSTANTS = new HashMap<>();

    //内置支持常量圆周率，自然数，真空光速，普朗克常量，分别是数学和物理学中最基础重要的两个常量
    static {
        CONSTANTS.put("π", Math.PI);
        CONSTANTS.put("e", Math.E);
        CONSTANTS.put("c", 2.99792458E8);
        CONSTANTS.put("h", 6.6260755E-34);
    }

    private String mName = null;

    public Constant(String str) {
        super(CONSTANTS.get(str));
        mName = str;
    }

    //绑定数据库的静态函数，将被要求在MainActivity的OnCreate方法中调用，由此获得此前保存的自定义常数
    public static void bindDbHelper(CustomDefinitionDbHelper helper) {
        Cursor cursor = helper.getReadableDatabase()
                .rawQuery("SELECT name, value FROM constant", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            double value = cursor.getDouble(cursor.getColumnIndex("value"));
            CONSTANTS.put(name, value);
        }
        cursor.close();
    }

    @Override
    public String toString() {
        return mName;
    }
}
