package com.example.multicalc.basic_calc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.math.Constant;
import com.example.multicalc.basic_calc.math.CustomDefinitionDbHelper;
import com.example.multicalc.basic_calc.math.Function;
import com.example.multicalc.basic_calc.math.MathSignQueue;
import com.example.multicalc.basic_calc.math.RealNumber;
import com.example.multicalc.complex.ui.ComplexActivity;
import com.example.multicalc.matrix.ui.MatrixActivity;
import com.example.multicalc.other.CopyrightActivity;
import com.example.multicalc.other.DialogCheckUpdate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mSharedPreferences;
    private LargeEditText mEdt;

    private boolean mSucceedToCalc;
    private Exception mException;
    private String mResult = "";

    private int mTextSize;
    private int mAngularUnit;
    private int mResultFormat;

    private int mCurrentRowResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //为函数常数类绑定数据库，使其可以获得其中的用户自定义函数、常数
        Function.bindDbHelper(new CustomDefinitionDbHelper(this));
        Constant.bindDbHelper(new CustomDefinitionDbHelper(this));

        mSharedPreferences = getSharedPreferences("BasicSetting", MODE_PRIVATE);
        mEdt = (LargeEditText) findViewById(R.id.mainEditText);
        mCurrentRowResId = R.id.rowTriFun1;

        //删除键允许长按等效多次重复按下
        ((KeyboardButton) findViewById(R.id.btnDelete)).enableLongClickForRepeat();

        //自定义的ActionBar
        initCustomActionBar();
    }

    //读取已保存的字体，角度/弧度制,结果输出形式，上次的输入
    @Override
    protected void onStart() {
        super.onStart();
        mTextSize = mSharedPreferences.getInt("textSize", 20);
        mAngularUnit = mSharedPreferences.getInt("angularUnit", 0);
        mResultFormat = mSharedPreferences.getInt("resultFormat", 0);
        String previousInput = mSharedPreferences.getString("previousInput", "");
        mEdt.setTextSize(mTextSize);
        mEdt.setText(previousInput);
        mEdt.setSelection(previousInput.length());
    }

    //保存数据
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("previousInput", mEdt.getText().toString());
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter:
                mEdt.insert("\n");
                break;
            case R.id.btnGoLeft:
                mEdt.cursorMove(-1);
                break;
            case R.id.btnGoRight:
                mEdt.cursorMove(1);
                break;
            case R.id.btnClear:
                mEdt.setText("");
                break;
            case R.id.btnDelete:
                mEdt.delete();
                break;

            case R.id.btnFunPlus:
                changeVisibleRow(R.id.rowFunPlus);
                break;
            case R.id.btnConPlus:
                changeVisibleRow(R.id.rowConPlus);
                break;
            case R.id.btnTriFun:
                changeVisibleRow(R.id.rowTriFun1);
                break;
            case R.id.btnExpFun:
                changeVisibleRow(R.id.rowExpFun);
                break;
            case R.id.btnSign:
                changeVisibleRow(R.id.rowSign);
                break;

            case R.id.btnPermutation:
                mEdt.insert("P(,)", -2);
                break;
            case R.id.btnCombination:
                mEdt.insert("C(,)", -2);
                break;
            case R.id.btnMod:
                mEdt.insert("mod(,)", -2);
                break;
            case R.id.btnAbs:
                mEdt.insert("abs()", -1);
                break;
            case R.id.btnCustomFun:
                dialogCustomFunction();
                break;

            case R.id.btnPi:
                mEdt.insert("π");
                break;
            case R.id.btnNaturalCon:
                mEdt.insert("e");
                break;
            case R.id.btnSpeedOfLight:
                mEdt.insert("c");
                break;
            case R.id.btnPlanckCon:
                mEdt.insert("h");
                break;
            case R.id.btnCustomCon:
                dialogCustomConstant();
                break;

            case R.id.btnSin:
                mEdt.insert("sin");
                break;
            case R.id.btnCos:
                mEdt.insert("cos");
                break;
            case R.id.btnTan:
                mEdt.insert("tan");
                break;
            case R.id.btnCot:
                mEdt.insert("cot");
                break;
            case R.id.btnShift1:
                changeVisibleRow(R.id.rowTriFun2);
                break;

            case R.id.btnArcsin:
                mEdt.insert("arcsin");
                break;
            case R.id.btnArccos:
                mEdt.insert("arccos");
                break;
            case R.id.btnArctan:
                mEdt.insert("arctan");
                break;
            case R.id.btnArccot:
                mEdt.insert("arccot");
                break;
            case R.id.btnShift2:
                changeVisibleRow(R.id.rowTriFun3);
                break;

            case R.id.btnSec:
                mEdt.insert("sec");
                break;
            case R.id.btnCsc:
                mEdt.insert("csc");
                break;
            case R.id.btnArcsec:
                mEdt.insert("arcsec");
                break;
            case R.id.btnArccsc:
                mEdt.insert("arccsc");
                break;
            case R.id.btnShift3:
                changeVisibleRow(R.id.rowTriFun1);
                break;

            case R.id.btnTenExp:
                mEdt.insert("10^");
                break;
            case R.id.btnNaturalExp:
                mEdt.insert("e^");
                break;
            case R.id.btnLg:
                mEdt.insert("lg");
                break;
            case R.id.btnLn:
                mEdt.insert("ln");
                break;
            case R.id.btnLog:
                mEdt.insert("log(,)", -2);
                break;

            case R.id.btnFactorial:
                mEdt.insert("!");
                break;
            case R.id.btnComma:
                mEdt.insert(",");
                break;
            case R.id.btnPercent:
                mEdt.insert("%");
                break;
            case R.id.btnLeftBracket:
                mEdt.insert("(");
                break;
            case R.id.btnRightBracket:
                mEdt.insert(")");
                break;

            case R.id.btnBrackets:
                mEdt.insert("()", -1);
                break;
            case R.id.btnOne:
                mEdt.insert("1");
                break;
            case R.id.btnTwo:
                mEdt.insert("2");
                break;
            case R.id.btnThree:
                mEdt.insert("3");
                break;
            case R.id.btnAddition:
                if (mEdt.isLineHead()) {
                    mEdt.insert(mResult);
                }
                mEdt.insert("+");
                break;

            case R.id.btnExponent:
                mEdt.insert("E");
                break;
            case R.id.btnFour:
                mEdt.insert("4");
                break;
            case R.id.btnFive:
                mEdt.insert("5");
                break;
            case R.id.btnSix:
                mEdt.insert("6");
                break;
            case R.id.btnSubtraction:
                if (mEdt.isLineHead()) {
                    mEdt.insert(mResult);
                }
                mEdt.insert("-");
                break;

            case R.id.btnPow:
                mEdt.insert("^");
                break;
            case R.id.btnSeven:
                mEdt.insert("7");
                break;
            case R.id.btnEight:
                mEdt.insert("8");
                break;
            case R.id.btnNine:
                mEdt.insert("9");
                break;
            case R.id.btnMultiplication:
                if (mEdt.isLineHead()) {
                    mEdt.insert(mResult);
                }
                mEdt.insert("×");
                break;

            case R.id.btnRoot:
                mEdt.insert("√");
                break;
            case R.id.btnPoint:
                mEdt.insert(".");
                break;
            case R.id.btnZero:
                mEdt.insert("0");
                break;
            case R.id.btnEquality:
                startCalc();
                break;
            case R.id.btnDivision:
                if (mEdt.isLineHead()) {
                    mEdt.insert(mResult);
                }
                mEdt.insert("÷");
                break;
        }
    }

    //初始化自定义的ActionBar
    private void initCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_main);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            Spinner spnAngularUnit =
                    (Spinner) actionBar.getCustomView().findViewById(R.id.spnAngularUnit);
            Spinner spnResultFormat =
                    (Spinner) actionBar.getCustomView().findViewById(R.id.spnResultFormat);
            ImageView btnTextSize =
                    (ImageView) actionBar.getCustomView().findViewById(R.id.btnTextSize);
            ImageView btnFindMore =
                    (ImageView) actionBar.getCustomView().findViewById(R.id.btnFindMore);
            final SharedPreferences.Editor editor = mSharedPreferences.edit();

            spnAngularUnit.setSelection(mAngularUnit);
            spnResultFormat.setSelection(mResultFormat);
            OnItemSelectedListener select = new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getId() == R.id.spnAngularUnit) {
                        mAngularUnit = position;
                        editor.putInt("angularUnit", position);
                    } else if (parent.getId() == R.id.spnResultFormat) {
                        mResultFormat = position;
                        editor.putInt("resultFormat", position);
                    }
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
            spnAngularUnit.setOnItemSelectedListener(select);
            spnResultFormat.setOnItemSelectedListener(select);

            btnTextSize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("字体调节");
                    builder.setCancelable(true);
                    SeekBar seekBar = new SeekBar(MainActivity.this);
                    seekBar.setProgress((mTextSize - 15) * 4);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            mTextSize = progress / 4 + 15;
                            mEdt.setTextSize(mTextSize);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            editor.putInt("textSize", mTextSize).apply();
                        }
                    });
                    builder.setView(seekBar);
                    builder.show();
                }
            });

            btnFindMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu pop = new PopupMenu(MainActivity.this, v);
                    pop.getMenuInflater().inflate(R.menu.main, pop.getMenu());
                    pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.complexCalc:
                                    startActivity(
                                            new Intent(MainActivity.this, ComplexActivity.class));
                                    break;
                                case R.id.matrix:
                                    startActivity(
                                            new Intent(MainActivity.this, MatrixActivity.class));
                                    break;
                                case R.id.copyright:
                                    startActivity(
                                            new Intent(MainActivity.this, CopyrightActivity.class));
                                    break;
                                case R.id.update:
                                    new DialogCheckUpdate(MainActivity.this);
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    pop.show();
                }
            });
        }
    }

    //由于按键太大，界面键盘第二行是可以切换的，这里用来切换行
    private void changeVisibleRow(int id) {
        switch (mCurrentRowResId) {
            case R.id.rowFunPlus:
                ((ImageView) findViewById(R.id.btnFunPlus))
                        .setImageResource(R.drawable.btn_fun_plus_1);
                break;
            case R.id.rowConPlus:
                ((ImageView) findViewById(R.id.btnConPlus))
                        .setImageResource(R.drawable.btn_con_plus_1);
                break;
            case R.id.rowTriFun1:
            case R.id.rowTriFun2:
            case R.id.rowTriFun3:
                ((ImageView) findViewById(R.id.btnTriFun))
                        .setImageResource(R.drawable.btn_tri_fun_1);
                break;
            case R.id.rowExpFun:
                ((ImageView) findViewById(R.id.btnExpFun))
                        .setImageResource(R.drawable.btn_exp_fun_1);
                break;
            case R.id.rowSign:
                ((ImageView) findViewById(R.id.btnSign))
                        .setImageResource(R.drawable.btn_sign_1);
                break;
            default:
                break;
        }
        findViewById(mCurrentRowResId).setVisibility(View.GONE);
        mCurrentRowResId = id;
        findViewById(id).setVisibility(View.VISIBLE);
        switch (mCurrentRowResId) {
            case R.id.rowFunPlus:
                ((ImageView) findViewById(R.id.btnFunPlus))
                        .setImageResource(R.drawable.btn_fun_plus_2);
                break;
            case R.id.rowConPlus:
                ((ImageView) findViewById(R.id.btnConPlus))
                        .setImageResource(R.drawable.btn_con_plus_2);
                break;
            case R.id.rowTriFun1:
            case R.id.rowTriFun2:
            case R.id.rowTriFun3:
                ((ImageView) findViewById(R.id.btnTriFun))
                        .setImageResource(R.drawable.btn_tri_fun_2);
                break;
            case R.id.rowExpFun:
                ((ImageView) findViewById(R.id.btnExpFun))
                        .setImageResource(R.drawable.btn_exp_fun_2);
                break;
            case R.id.rowSign:
                ((ImageView) findViewById(R.id.btnSign))
                        .setImageResource(R.drawable.btn_sign_2);
                break;
            default:
                break;
        }
    }

    //对话框用来展示课选取的自定义函数并由使用者选取输入
    private void dialogCustomFunction() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义函数");
        builder.setCancelable(true);

        SQLiteDatabase db = new CustomDefinitionDbHelper(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, paraList, paraCount FROM function", null);
        final String[] names = new String[cursor.getCount()];
        String[] declarations = new String[cursor.getCount()];
        final int[] paraCounts = new int[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); i++) {
            names[i] = cursor.getString(cursor.getColumnIndex("name"));
            String paraList = cursor.getString(cursor.getColumnIndex("paraList"));
            declarations[i] = names[i] + "(" + paraList + ")";
            paraCounts[i] = cursor.getInt(cursor.getColumnIndex("paraCount"));
        }
        cursor.close();
        builder.setItems(declarations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEdt.insert(names[which] + "(");
                for (int i = 1; i < paraCounts[which]; i++) {
                    mEdt.insert(",");
                }
                mEdt.insert(")", -1 * paraCounts[which]);
            }
        });

        Button btn = new Button(this);
        btn.setText("管理");
        btn.setBackgroundColor(0x00000000);
        builder.setView(btn);
        final Dialog dialog = builder.show();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomManageActivity.class);
                intent.putExtra("whichFragment", "function");
                MainActivity.this.startActivity(intent);
                dialog.dismiss();
            }
        });
    }
    //对话框用来展示课选取的自定义常数并由使用者选取输入
    private void dialogCustomConstant() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义常数");
        builder.setCancelable(true);

        SQLiteDatabase db = new CustomDefinitionDbHelper(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, value FROM constant", null);
        final String[] names = new String[cursor.getCount()];
        String[] nameAndValues = new String[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); i++) {
            names[i] = cursor.getString(cursor.getColumnIndex("name"));
            double value = cursor.getDouble(cursor.getColumnIndex("value"));
            nameAndValues[i] = names[i] + " = " + value;
        }
        cursor.close();
        builder.setItems(nameAndValues, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEdt.insert(names[which]);
            }
        });

        Button btn = new Button(this);
        btn.setText("管理");
        btn.setBackgroundColor(0x00000000);
        builder.setView(btn);
        final Dialog dialog = builder.show();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomManageActivity.class);
                intent.putExtra("whichFragment", "constant");
                MainActivity.this.startActivity(intent);
                dialog.dismiss();
            }
        });
    }

    //开始计算，并输出结果或输出异常提示，这里新开一个现象，防止计算量过大卡死
    private void startCalc() {
        mSucceedToCalc = false;
        final String input = mEdt.handleCurrentLine();
        Thread threadToCalc = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MathSignQueue queue = MathSignQueue.parse(input);
                    queue.setAngularUnit(mAngularUnit == 0 ? MathSignQueue.ANGULAR_RADIAN :
                            MathSignQueue.ANGULAR_DEGREE);
                    String result = queue.queueValue().formatToString(
                            mResultFormat == 0 ? RealNumber.FORMAT_NORMAL :
                                    mResultFormat == 1 ? RealNumber.FORMAT_FRACTION :
                                            RealNumber.FORMAT_SCIENTIFIC);
                    if (!Thread.currentThread().isInterrupted()) {
                        mResult = result;
                        mSucceedToCalc = true;
                    }
                } catch (Exception e) {
                    mException = e;
                }
            }
        });
        threadToCalc.setPriority(Thread.MAX_PRIORITY);
        threadToCalc.start();
        for (int i = 0; i < 60 && threadToCalc.isAlive(); i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.d(getClass().getName(), Log.getStackTraceString(e));
            }
        }
        if (threadToCalc.isAlive()) {
            threadToCalc.interrupt();
            mSucceedToCalc = false;
            mException = new CalcException("计算量过大");
        }
        if (mSucceedToCalc) {
            mEdt.insert(mResult + "\n");
        } else {
            if (mException instanceof CalcException) {
                CalcException e = (CalcException) mException;
                Toast.makeText(MainActivity.this, e.getDetail(), Toast.LENGTH_SHORT).show();
                //EditText在某些状态下，会不能选中出错区域，最简单的解决办法：把光标隐藏再开启，恢复常态
                mEdt.setCursorVisible(false);
                mEdt.setCursorVisible(true);
                mEdt.setSelection(mEdt.lineStart() + e.start(), mEdt.lineStart() + e.end());
            } else {
                Toast.makeText(MainActivity.this, "未知错误，计算失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}