package com.example.multicalc.basic_calc.math;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * 自定义数据（指自定义的函数和常数）的数据库帮助类，把有关的增删操作封装在内部
 */
public class CustomDefinitionDbHelper extends SQLiteOpenHelper {

    private static HashMap<String, CustomFunctionExecutor> FUNCTION_CACHE = new HashMap<>();

    public CustomDefinitionDbHelper(Context context) {
        super(context, "CustomDefinition", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE function (name TEXT PRIMARY KEY, paraList TEXT, paraCount INTEGER,"
                + " expression TEXT, detail TEXT)");
        db.execSQL("CREATE TABLE constant (name TEXT PRIMARY KEY, value REAL, detail TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addFunction(String name, String paraList, String expression, String detail) {
        ContentValues content = new ContentValues();
        content.put("name", name.replaceAll("\\s", ""));
        content.put("paraList", paraList.replaceAll("\\s", ""));
        int paraCount = paraList.split(",").length;
        content.put("paraCount", paraCount);
        content.put("expression", expression.replaceAll("\\s", ""));
        content.put("detail", detail);
        getWritableDatabase().delete("function", "name = ?", new String[]{name});
        Function.FUNCTIONS.remove(name);
        getWritableDatabase().insert("function", null, content);
        Function.FUNCTIONS.put(name, paraCount);
    }

    public void removeFunction(String name) {
        if (name != null) {
            getWritableDatabase().delete("function", "name = ?", new String[]{name});
            FUNCTION_CACHE.remove(name);
            Function.FUNCTIONS.remove(name);
        }
    }

    //此处需参见CustomFunctionExecute的注释
    //此外，这里用到了类的静态HashMap，这个HashMap其实是有关这个函数的一个缓存，减少数据库读取和
    //CustomFunctionExecute的“编译”操作
    public CustomFunctionExecutor getExe(String name) throws CalcException {
        if (FUNCTION_CACHE.containsKey(name)) {
            return FUNCTION_CACHE.get(name).copy();
        }
        CustomFunctionExecutor exe = null;
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT paraList, expression FROM function WHERE name = ?", new String[]{name});
        if (cursor.moveToFirst()) {
            String expression = cursor.getString(cursor.getColumnIndex("expression"));
            String paraList = cursor.getString(cursor.getColumnIndex("paraList"));
            exe = CustomFunctionExecutor.compile(expression, paraList);
            FUNCTION_CACHE.put(name, exe.copy());
        }
        cursor.close();
        return exe;
    }

    public void addConstant(String name, double value, String detail) {
        name = name.replaceAll("\\s", "");
        ContentValues content = new ContentValues();
        content.put("name", name);
        content.put("value", value);
        content.put("detail", detail);
        getWritableDatabase().delete("constant", "name = ?", new String[]{name});
        getWritableDatabase().insert("constant", null, content);
        Constant.CONSTANTS.put(name, value);
    }

    public void removeConstant(String name) {
        if (name != null) {
            getWritableDatabase().delete("constant", "name = ?", new String[]{name});
            Constant.CONSTANTS.remove(name);
        }
    }
}
