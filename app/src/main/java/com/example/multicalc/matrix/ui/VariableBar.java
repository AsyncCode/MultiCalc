package com.example.multicalc.matrix.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.multicalc.R;
import com.example.multicalc.matrix.math.MatrixOrNumber;
import com.example.multicalc.matrix.math.MatrixVariable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * VariableBar(或称之为变量条)即界面中处于键盘上部的一个区域，方便使用者定义，管理和使用矩阵变量。
 * 主要功能有：
 * 1、点击新建按钮新建一个变量，点击普通变量按钮输入一个变量（这部分功能在MatrixActivity中实现）
 * 2、长按一个变量按钮可以删除或者修改值（如果其已被赋值过）
 * 3、View销毁时把数据存放在数据库中，下次建立时再从文件中读取恢复
 */
public class VariableBar extends HorizontalScrollView implements View.OnLongClickListener {

    private LinearLayout.LayoutParams mBtnParams;
    private LinearLayout mContainer;
    private Context mContext;

    public VariableBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setHorizontalScrollBarEnabled(false);
        setBackgroundColor(0xff818181);
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mContainer);
        mBtnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mBtnParams.setMargins(0, 0, 2, 2);
    }

    //增加（或者是修改）变量值的同时，如果该变量在变量条上没有对应按钮，则添加对应按钮
    public void addVarWithBtn(String name, MatrixOrNumber value) {
        if (!MatrixVariable.VARIABLES.containsKey(name)) {
            mContainer.addView(new VariableButton(name));
        }
        MatrixVariable.VARIABLES.put(name, value);
    }

    //全清变量条，包括把变量销毁以及其上所有的按钮（除了“新建”按钮）全部移除
    public void clear() {
        MatrixVariable.VARIABLES.clear();
        mContainer.removeAllViews();
        VariableButton varBtn = new VariableButton("新建");
        varBtn.setOnLongClickListener(null);
        mContainer.addView(varBtn);
    }

    //View建立时，从数据库中读取上次的数据并调整变量条
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        VariableButton varBtn = new VariableButton("新建");
        varBtn.setOnLongClickListener(null);
        mContainer.addView(varBtn);
        new VariableDbHelper().readAllVarFromDb();
        for (String name : MatrixVariable.VARIABLES.keySet()) {
            mContainer.addView(new VariableButton(name));
        }
    }

    //View销毁时，把数据写入数据库
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        new VariableDbHelper().writeAllVarToDb();
    }


    //长按事件，实现删除或者修改一个变量
    @Override
    public boolean onLongClick(final View v) {
        final String name = ((VariableButton) v).name();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("变量：" + name).setCancelable(true);
        String[] items = MatrixVariable.VARIABLES.get(name) == null ?
                new String[]{"删除"} : new String[]{"删除", "修改"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    MatrixVariable.VARIABLES.remove(name);
                    mContainer.removeView(v);
                } else if (which == 1) {
                    ((WorkWindow) ((Activity) mContext).findViewById(R.id.workWindow)).
                            startMatrixModify(name, MatrixVariable.VARIABLES.get(name));
                }
            }
        });
        builder.show();
        return true;
    }

    /**
     * 变量条上的Button，保存有变量名，字体根据按钮大小自动调整，有按下效果
     */
    public class VariableButton extends Button {
        private String mName;

        public VariableButton(String name) {
            super(mContext);
            setLayoutParams(mBtnParams);
            mName = name;
            setText(name);
            setTextColor(Color.WHITE);
            setBackgroundColor(0xff555555);
            setPadding(15, 0, 15, 0);
            setGravity(Gravity.CENTER);
            setSingleLine(true);
            setOnClickListener((OnClickListener) mContext);
            setOnLongClickListener(VariableBar.this);
        }

        public String name() {
            return mName;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setBackgroundColor(0xff0b294f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setBackgroundColor(0xff555555);
                    break;
                default:
                    break;
            }
            return super.onTouchEvent(event);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float textSize = MeasureSpec.getSize(heightMeasureSpec) * 0.40f;
            if (getTextSize() != textSize) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 变量条的数据库帮助类，把变量条上的所有变量存入数据库
     * 其中的变量值：MatrixOrNumber是转化为字节数组然后以BLOB形式存入数据库
     */
    private class VariableDbHelper extends SQLiteOpenHelper {

        public VariableDbHelper() {
            super(mContext, "MatrixVariable", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE variables (name TEXT PRIMARY KEY, value BLOB)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public void writeAllVarToDb() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM variables");
            ContentValues content = new ContentValues();
            for (Map.Entry<String, MatrixOrNumber> entry : MatrixVariable.VARIABLES.entrySet()) {
                content.put("name", entry.getKey());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(entry.getValue());
                    content.put("value", baos.toByteArray());
                    oos.close();
                } catch (IOException e) {
                    content.put("value", new byte[0]);
                    Log.d(getClass().getName(), Log.getStackTraceString(e));
                }
                db.insert("variables", null, content);
            }
        }

        public void readAllVarFromDb() {
            SQLiteDatabase db = getReadableDatabase();
            MatrixVariable.VARIABLES.clear();
            Cursor cursor = db.rawQuery("SELECT * FROM variables", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex("value"));
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                try {
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    MatrixOrNumber value = (MatrixOrNumber) ois.readObject();
                    MatrixVariable.VARIABLES.put(name, value);
                    ois.close();
                } catch (IOException | ClassNotFoundException e) {
                    MatrixVariable.VARIABLES.put(name, null);
                    Log.d(getClass().getName(), Log.getStackTraceString(e));
                }
            }
            cursor.close();
        }
    }
}