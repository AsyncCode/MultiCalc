package com.example.multicalc.matrix.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.ui.KeyboardButton;
import com.example.multicalc.matrix.math.MatrixFunction;
import com.example.multicalc.matrix.math.MatrixOrNumber;
import com.example.multicalc.matrix.math.MatrixVariable;
import com.example.multicalc.other.MatrixFAQActivity;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 矩阵运算主界面的Activity
 */
public class MatrixActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mSharedPreferences;
    private ViewGroup mKeyboard;
    private WorkWindow mWorkWindow;
    private VariableBar mVariableBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //删除键允许长按代替连续删除
        ((KeyboardButton) findViewById(R.id.btnDelete)).enableLongClickForRepeat();
        //长按AC键。调出对话框选择更多选项
        findViewById(R.id.btnClear).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogClear();
                return false;
            }
        });

        mSharedPreferences = getSharedPreferences("BasicSetting", MODE_PRIVATE);
        mVariableBar = ((VariableBar) findViewById(R.id.variableBar));
        mWorkWindow = ((WorkWindow) (findViewById(R.id.workWindow)));
        mWorkWindow.setTextSize(mSharedPreferences.getInt("matrix_textSize", 20));
        mKeyboard = ((ViewGroup) findViewById(R.id.keyboard));
        //WorkWindow双击全屏的实现
        mWorkWindow.setOnTouchListener(new View.OnTouchListener() {
            private boolean mClicked = false;
            private long mFirstTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mClicked) {
                        long secondTime = System.currentTimeMillis();
                        if (secondTime - mFirstTime < 250) {
                            mKeyboard.setVisibility(mKeyboard.getVisibility() == View.VISIBLE ?
                                    View.GONE : View.VISIBLE);
                            mClicked = false;
                        } else {
                            mFirstTime = secondTime;
                        }
                    } else {
                        mClicked = true;
                        mFirstTime = System.currentTimeMillis();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        mWorkWindow = ((WorkWindow) findViewById(R.id.workWindow));
        if (v instanceof VariableBar.VariableButton) {
            VariableBar.VariableButton varBtn = (VariableBar.VariableButton) v;
            if (varBtn.name().equals("新建")) {
                dialogNewVar();
            } else {
                mWorkWindow.insert(varBtn.name());
            }
        } else {
            switch (v.getId()) {
                case R.id.btnEnter:
                    mWorkWindow.finishInput();
                    break;
                case R.id.btnGoLeft:
                    mWorkWindow.move(-1);
                    break;
                case R.id.btnGoRight:
                    mWorkWindow.move(1);
                    break;
                case R.id.btnClear:
                    mWorkWindow.clearInput();
                    break;
                case R.id.btnDelete:
                    View view = mWorkWindow.findFocus();
                    if (view != null && view instanceof SmallEditText) {
                        ((SmallEditText) view).delete();
                    }
                    break;
                case R.id.btnDet:
                    mWorkWindow.insert("det()", -1);
                    break;
                case R.id.btnInv:
                    mWorkWindow.insert("inv()", -1);
                    break;
                case R.id.btnTrans:
                    mWorkWindow.insert("trans()", -1);
                    break;
                case R.id.btnRank:
                    mWorkWindow.insert("rank()", -1);
                    break;
                case R.id.btnRref:
                    mWorkWindow.insert("rref()", -1);
                    break;
                case R.id.btnBrackets:
                    mWorkWindow.insert("()", -1);
                    break;
                case R.id.btnOne:
                    mWorkWindow.insert("1");
                    break;
                case R.id.btnTwo:
                    mWorkWindow.insert("2");
                    break;
                case R.id.btnThree:
                    mWorkWindow.insert("3");
                    break;
                case R.id.btnAddition:
                    mWorkWindow.insert("+");
                    break;
                case R.id.btnIdentityMatrix:
                    mWorkWindow.insert("E()", -1);
                    break;
                case R.id.btnFour:
                    mWorkWindow.insert("4");
                    break;
                case R.id.btnFive:
                    mWorkWindow.insert("5");
                    break;
                case R.id.btnSix:
                    mWorkWindow.insert("6");
                    break;
                case R.id.btnSubtraction:
                    mWorkWindow.insert("-");
                    break;
                case R.id.btnPow:
                    mWorkWindow.insert("^");
                    break;
                case R.id.btnSeven:
                    mWorkWindow.insert("7");
                    break;
                case R.id.btnEight:
                    mWorkWindow.insert("8");
                    break;
                case R.id.btnNine:
                    mWorkWindow.insert("9");
                    break;
                case R.id.btnMultiplication:
                    mWorkWindow.insert("*");
                    break;
                case R.id.btnFindMore:
                    dialogFindMore();
                    break;
                case R.id.btnPoint:
                    mWorkWindow.insert(".");
                    break;
                case R.id.btnZero:
                    mWorkWindow.insert("0");
                    break;
                case R.id.btnEquality:
                    mWorkWindow.insert("=");
                    break;
                case R.id.btnDivision:
                    mWorkWindow.insert("/");
                    break;
            }
        }
    }

    //弹出对话框，执行新建一个变量的有关操作
    public void dialogNewVar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root =
                getLayoutInflater().inflate(R.layout.dialog_new_var, mWorkWindow, false);
        builder.setCancelable(false);
        builder.setView(root);
        final EditText edtVarName = ((EditText) root.findViewById(R.id.edtVarName));
        final CheckBox chkAssignNow = ((CheckBox) root.findViewById(R.id.chkAssignNow));
        final View tableAssign = root.findViewById(R.id.tableAssign);
        final EditText edtDefaultString = (EditText) root.findViewById(R.id.edtDefaultString);
        final EditText edtRow = ((EditText) root.findViewById(R.id.edtRow));
        final EditText edtColumn = ((EditText) root.findViewById(R.id.edtColumn));
        final NumberPicker pickerRow = (NumberPicker) root.findViewById(R.id.pickerRow);
        final NumberPicker pickerColumn = (NumberPicker) root.findViewById(R.id.pickerColumn);
        final Button btnCancel = (Button) root.findViewById(R.id.btnCancel);
        final Button btnOK = (Button) root.findViewById(R.id.btnOK);
        final Dialog dialog = builder.show();

        chkAssignNow.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tableAssign.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });


        NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (picker == pickerRow) {
                    edtRow.setText(String.valueOf(newVal));
                } else {
                    edtColumn.setText(String.valueOf(newVal));
                }
            }
        };
        pickerRow.setMinValue(1);
        pickerRow.setMaxValue(10);
        pickerRow.setValue(4);
        pickerRow.setOnValueChangedListener(listener);
        edtRow.setText("4");
        pickerColumn.setMinValue(1);
        pickerColumn.setMaxValue(10);
        pickerColumn.setValue(4);
        pickerColumn.setOnValueChangedListener(listener);
        edtColumn.setText("4");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String varName = edtVarName.getText().toString();
                if (varName.isEmpty()) {
                    Toast.makeText(MatrixActivity.this, "变量名称不可为空", Toast.LENGTH_SHORT).show();
                } else if (MatrixVariable.VARIABLES.containsKey(varName) ||
                        MatrixFunction.FUNCTIONS.contains(varName)) {
                    Toast.makeText(MatrixActivity.this, "名称和已有变量名或函数名冲突",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mVariableBar.addVarWithBtn(varName, null);
                    if (chkAssignNow.isChecked()) {
                        int row = Integer.parseInt(edtRow.getText().toString());
                        int column = Integer.parseInt(edtColumn.getText().toString());
                        mWorkWindow.startMatrixInput(varName, row, column,
                                edtDefaultString.getText().toString());
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    //弹出对话框，选择更多功能
    public void dialogFindMore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更多功能").setCancelable(true);
        String[] items = new String[]{"矩阵合并", "求伴随", "代数余子式", "子矩阵", "字体调节", "帮助"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialogCombination();
                        break;
                    case 1:
                        dialogAdjoint();
                        break;
                    case 2:
                        dialogCofactor();
                        break;
                    case 3:
                        dialogSubMatrix();
                        break;
                    case 4:
                        dialogAdjustTextSize();
                        break;
                    case 5:
                        startActivity(new Intent(MatrixActivity.this, MatrixFAQActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    //弹出对话框，执行矩阵合并有关操作
    public void dialogCombination() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root =
                getLayoutInflater().inflate(R.layout.dialog_combination, mWorkWindow, false);
        builder.setCancelable(false).setView(root);
        final Spinner spnSource1 = (Spinner) root.findViewById(R.id.spnSource1);
        final Spinner spnSource2 = (Spinner) root.findViewById(R.id.spnSource2);
        final Spinner spnTarget = (Spinner) root.findViewById(R.id.spnTarget);
        final RadioButton rdbHorizontal = (RadioButton) root.findViewById(R.id.rdbHorizontal);
        final Button btnCancel = (Button) root.findViewById(R.id.btnCancel);
        final Button btnOK = (Button) root.findViewById(R.id.btnOK);
        final Dialog dialog = builder.show();

        final ArrayList<String> list = new ArrayList<>();
        list.addAll(MatrixVariable.VARIABLES.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, list);
        spnSource1.setAdapter(adapter);
        spnSource2.setAdapter(adapter);
        spnTarget.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int iSource1 = spnSource1.getSelectedItemPosition();
                int iSource2 = spnSource2.getSelectedItemPosition();
                int iTarget = spnTarget.getSelectedItemPosition();
                if (iSource1 >= 0 && iSource2 >= 0 && iTarget >= 0) {
                    MatrixOrNumber a = MatrixVariable.VARIABLES.get(list.get(iSource1));
                    MatrixOrNumber b = MatrixVariable.VARIABLES.get(list.get(iSource2));
                    if (a == null || b == null) {
                        Toast.makeText(MatrixActivity.this, "未初始化的矩阵不可作为合并来源",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            MatrixVariable.VARIABLES.put(list.get(iTarget),
                                    MatrixOrNumber.combine(a, b, rdbHorizontal.isChecked()));
                            mWorkWindow.startShowMatrix(list.get(iTarget), true);
                            dialog.dismiss();
                        } catch (CalcException e) {
                            Toast.makeText(MatrixActivity.this, e.getDetail(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
    }

    //弹出对话框，执行求伴随合并有关操作
    public void dialogAdjoint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root =
                getLayoutInflater().inflate(R.layout.dialog_adjoint, mWorkWindow, false);
        builder.setCancelable(false).setView(root);
        final Spinner spnSource = (Spinner) root.findViewById(R.id.spnSource);
        final Spinner spnTarget = (Spinner) root.findViewById(R.id.spnTarget);
        final Button btnCancel = (Button) root.findViewById(R.id.btnCancel);
        final Button btnOK = (Button) root.findViewById(R.id.btnOK);
        final Dialog dialog = builder.show();

        final ArrayList<String> list = new ArrayList<>();
        list.addAll(MatrixVariable.VARIABLES.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, list);
        spnSource.setAdapter(adapter);
        spnTarget.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int iSource = spnSource.getSelectedItemPosition();
                int iTarget = spnTarget.getSelectedItemPosition();
                if (iSource >= 0 && iTarget >= 0) {
                    MatrixOrNumber mSource = MatrixVariable.VARIABLES.get(list.get(iSource));
                    if (mSource == null) {
                        Toast.makeText(MatrixActivity.this, "未初始化的矩阵不可求伴随",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            MatrixVariable.VARIABLES.put(list.get(iTarget), mSource.adjoint());
                            mWorkWindow.startShowMatrix(list.get(iTarget), true);
                            dialog.dismiss();
                        } catch (CalcException e) {
                            Toast.makeText(MatrixActivity.this, e.getDetail(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
    }

    //弹出对话框，执行求代数余子式有关操作
    public void dialogCofactor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root = getLayoutInflater()
                .inflate(R.layout.dialog_cofactor, mWorkWindow, false);
        builder.setCancelable(false);
        builder.setView(root);
        final Spinner spnVar = ((Spinner) root.findViewById(R.id.spnVar));
        final NumberPicker pickerRow = (NumberPicker) root.findViewById(R.id.pickerRow);
        final NumberPicker pickerColumn = (NumberPicker) root.findViewById(R.id.pickerColumn);
        final TextView tvResult = (TextView) root.findViewById(R.id.tvResult);
        final Button btnCancel = (Button) root.findViewById(R.id.btnCancel);
        final Button btnOK = (Button) root.findViewById(R.id.btnOK);
        final Dialog dialog = builder.show();
        final String varName[] = new String[]{null};
        pickerRow.setMinValue(1);
        pickerColumn.setMinValue(1);
        pickerRow.setMaxValue(1);
        pickerColumn.setMaxValue(1);

        final LinkedList<String> varNames = new LinkedList<>();
        for (String str : MatrixVariable.VARIABLES.keySet()) {
            if (MatrixVariable.VARIABLES.get(str) != null) {
                varNames.add(str);
            }
        }
        spnVar.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, varNames));
        spnVar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                varName[0] = varNames.get(position);
                MatrixOrNumber selected = MatrixVariable.VARIABLES.get(varName[0]);
                pickerRow.setMaxValue(selected.row());
                pickerColumn.setMaxValue(selected.column());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (varName[0] != null) {
                    try {
                        tvResult.setText(MatrixVariable.VARIABLES.get(varName[0])
                                .cofactor(pickerRow.getValue() - 1,
                                        pickerColumn.getValue() - 1).element(0, 0).toString());
                    } catch (CalcException e) {
                        Toast.makeText(MatrixActivity.this, e.getDetail(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //弹出对话框，执行求子矩阵有关操作
    public void dialogSubMatrix() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root =
                getLayoutInflater().inflate(R.layout.dialog_submatrix, mWorkWindow, false);
        builder.setCancelable(false).setView(root);
        final Spinner spnSource = (Spinner) root.findViewById(R.id.spnSource);
        final Spinner spnTarget = (Spinner) root.findViewById(R.id.spnTarget);
        final LinearLayout barRowSelect = (LinearLayout) root.findViewById(R.id.barRowSelect);
        final LinearLayout barColumnSelect = (LinearLayout) root.findViewById(R.id.barColumnSelect);
        final Button btnCancel = (Button) root.findViewById(R.id.btnCancel);
        final Button btnInvSelect = (Button) root.findViewById(R.id.btnInvSelect);
        final Button btnOK = (Button) root.findViewById(R.id.btnOK);
        final Dialog dialog = builder.show();
        final CheckBox[][] rowBoxes = new CheckBox[1][];
        final CheckBox[][] columnBoxes = new CheckBox[1][];

        final ArrayList<String> list = new ArrayList<>();
        list.addAll(MatrixVariable.VARIABLES.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, list);
        spnSource.setAdapter(adapter);
        spnTarget.setAdapter(adapter);
        spnSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MatrixOrNumber selectedSource = MatrixVariable.VARIABLES.get(list.get(position));
                if (selectedSource != null) {
                    barRowSelect.removeAllViews();
                    barColumnSelect.removeAllViews();
                    int rowCount = selectedSource.row();
                    int columnCount = selectedSource.column();
                    rowBoxes[0] = new CheckBox[rowCount];
                    columnBoxes[0] = new CheckBox[columnCount];
                    for (int i = 0; i < rowCount; i++) {
                        CheckBox box = new CheckBox(MatrixActivity.this);
                        box.setText(String.valueOf(i + 1));
                        barRowSelect.addView(box);
                        rowBoxes[0][i] = box;
                    }
                    for (int i = 0; i < columnCount; i++) {
                        CheckBox box = new CheckBox(MatrixActivity.this);
                        box.setText(String.valueOf(i + 1));
                        barColumnSelect.addView(box);
                        columnBoxes[0][i] = box;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnInvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rowBoxes[0] != null && columnBoxes[0] != null) {
                    int row = rowBoxes[0].length;
                    int column = columnBoxes[0].length;
                    while (row-- > 0) {
                        rowBoxes[0][row].setChecked(!rowBoxes[0][row].isChecked());
                    }
                    while (column-- > 0) {
                        columnBoxes[0][column].setChecked(!columnBoxes[0][column].isChecked());
                    }
                }
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int iSource = spnSource.getSelectedItemPosition();
                int iTarget = spnTarget.getSelectedItemPosition();
                if (iSource >= 0 && iTarget >= 0) {
                    MatrixOrNumber mSource = MatrixVariable.VARIABLES.get(list.get(iSource));
                    if (mSource == null) {
                        Toast.makeText(MatrixActivity.this, "不可对未初始化的矩阵取子矩阵",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        int checkedRowCount = 0;
                        int checkedColumnCount = 0;
                        int[] allRows = new int[mSource.row()];
                        int[] allColumns = new int[mSource.column()];
                        for (int i = 0; i < allRows.length; i++) {
                            if (rowBoxes[0][i].isChecked()) {
                                allRows[checkedRowCount++] = i;
                            }
                        }
                        for (int i = 0; i < allColumns.length; i++) {
                            if (columnBoxes[0][i].isChecked()) {
                                allColumns[checkedColumnCount++] = i;
                            }
                        }
                        int[] checkedRows = new int[checkedRowCount];
                        int[] checkedColumns = new int[checkedColumnCount];
                        System.arraycopy(allRows, 0, checkedRows, 0, checkedRowCount);
                        System.arraycopy(allColumns, 0, checkedColumns, 0, checkedColumnCount);
                        try {
                            MatrixVariable.VARIABLES.put(list.get(iTarget),
                                    mSource.subMatrix(checkedRows, checkedColumns));
                            mWorkWindow.startShowMatrix(list.get(iTarget), true);
                            dialog.dismiss();
                        } catch (CalcException e) {
                            Toast.makeText(MatrixActivity.this, e.getDetail(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
    }

    //弹出对话框，调节字体
    public void dialogAdjustTextSize() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("字体调节").setMessage("已显示部分需重新进入界面才生效").setCancelable(true);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setProgress((mWorkWindow.getTextSize() - 10) * 5);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int textSize = seekBar.getProgress() / 5 + 10;
                mWorkWindow.setTextSize(textSize);
                mSharedPreferences.edit().putInt("matrix_textSize", textSize).apply();
            }
        });
        builder.setView(seekBar);
        builder.show();
    }

    //弹出对话框，执行更多清除选项的操作
    public void dialogClear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择执行的操作").setCancelable(true);
        String[] items = new String[]{"终止当前输入", "清屏", "清除变量", "全清"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mWorkWindow.startCommandLine();
                        break;
                    case 1:
                        mWorkWindow.clearWindow();
                        break;
                    case 2:
                        mVariableBar.clear();
                        break;
                    case 3:
                        mVariableBar.clear();
                        mWorkWindow.clearWindow();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }
}