package com.example.multicalc.complex.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.ui.KeyboardButton;
import com.example.multicalc.basic_calc.ui.LargeEditText;
import com.example.multicalc.complex.math.ComplexMathSignQueue;
import com.example.multicalc.complex.math.ComplexNumber;
import com.example.multicalc.other.ComplexFAQActivity;

/**
 * 依然可以参考MainActivity，功能大致相近
 */
public class ComplexActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mSharedPreferences;
    private LargeEditText mEdt;
    private String mResult = "";

    private int mTextSize;
    private int mAngularUnit;
    private int mResultFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex);
        mSharedPreferences = getSharedPreferences("BasicSetting", MODE_PRIVATE);
        mEdt = (LargeEditText) findViewById(R.id.mainEditText);
        ((KeyboardButton) findViewById(R.id.btnDelete)).enableLongClickForRepeat();
        initCustomActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTextSize = mSharedPreferences.getInt("textSize", 20);
        mAngularUnit = mSharedPreferences.getInt("complex_angularUnit", 0);
        mResultFormat = mSharedPreferences.getInt("complex_resultFormat", 0);
        String previousInput = mSharedPreferences.getString("complex_previousInput", "");
        mEdt.setTextSize(mTextSize);
        mEdt.setText(previousInput);
        mEdt.setSelection(previousInput.length());
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("complex_previousInput", mEdt.getText().toString());
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

            case R.id.btnSin:
                mEdt.insert("sin()", -1);
                break;
            case R.id.btnCos:
                mEdt.insert("cos()", -1);
                break;
            case R.id.btnTan:
                mEdt.insert("tan()", -1);
                break;
            case R.id.btnConj:
                mEdt.insert("conj()", -1);
                break;
            case R.id.btnShift1:
                findViewById(R.id.rowFun1).setVisibility(View.GONE);
                findViewById(R.id.rowFun2).setVisibility(View.VISIBLE);
                break;

            case R.id.btnRe:
                mEdt.insert("Re()", -1);
                break;
            case R.id.btnIm:
                mEdt.insert("Im()", -1);
                break;
            case R.id.btnAbs:
                mEdt.insert("abs()", -1);
                break;
            case R.id.btnArg:
                mEdt.insert("arg()", -1);
                break;
            case R.id.btnShift2:
                findViewById(R.id.rowFun1).setVisibility(View.VISIBLE);
                findViewById(R.id.rowFun2).setVisibility(View.GONE);
                break;

            case R.id.btnLn:
                mEdt.insert("ln()", -1);
                break;
            case R.id.btnSolarSign:
                mEdt.insert("∠()", -1);
                break;
            case R.id.btnPi:
                mEdt.insert("π");
                break;
            case R.id.btnNaturalCon:
                mEdt.insert("e");
                break;
            case R.id.btnI:
                mEdt.insert("i");
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

            case R.id.btnMExpIM:
                mEdt.insert("e^(i)", -1);
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

    private void initCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_complex);
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
            AdapterView.OnItemSelectedListener select = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getId() == R.id.spnAngularUnit) {
                        mAngularUnit = position;
                        editor.putInt("complex_angularUnit", position);
                    } else if (parent.getId() == R.id.spnResultFormat) {
                        mResultFormat = position;
                        editor.putInt("complex_resultFormat", position);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ComplexActivity.this);
                    builder.setTitle("字体调节");
                    builder.setCancelable(true);
                    SeekBar seekBar = new SeekBar(ComplexActivity.this);
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
                    PopupMenu pop = new PopupMenu(ComplexActivity.this, v);
                    pop.getMenuInflater().inflate(R.menu.complex, pop.getMenu());
                    pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.faq:
                                    startActivity(
                                            new Intent(ComplexActivity.this, ComplexFAQActivity.class));
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

    private void startCalc() {
        String input = mEdt.handleCurrentLine();
        try {
            ComplexMathSignQueue queue = ComplexMathSignQueue.parse(input);
            queue.setAngularUnit(mAngularUnit == 0 ? ComplexMathSignQueue.ANGULAR_RADIAN :
                    mAngularUnit == 1 ? ComplexMathSignQueue.ANGULAR_DEGREE :
                            ComplexMathSignQueue.ANGULAR_RADIAN_WITH_PI);
            mResult = queue.queueValue().formatToString(queue.getAngularUnit(),
                    mResultFormat == 0 ? ComplexNumber.FORMAT_ALGEBRA : ComplexNumber.FORMAT_SOLAR);
            mEdt.insert(mResult + "\n");
        } catch (CalcException e) {
            Toast.makeText(this, e.getDetail(), Toast.LENGTH_SHORT).show();
            //EditText在某些状态下，会不能选中出错区域，最简单的解决办法：把光标隐藏再开启，恢复常态
            mEdt.setCursorVisible(false);
            mEdt.setCursorVisible(true);
            mEdt.setSelection(mEdt.lineStart() + e.start(), mEdt.lineStart() + e.end());
        } catch (Exception e) {
            Toast.makeText(this, "未知错误，计算失败", Toast.LENGTH_SHORT).show();
        }
    }
}
