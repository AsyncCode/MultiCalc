package com.example.multicalc.basic_calc.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.basic_calc.math.Constant;
import com.example.multicalc.basic_calc.math.CustomDefinitionDbHelper;
import com.example.multicalc.basic_calc.math.CustomFunctionExecutor;
import com.example.multicalc.basic_calc.math.Function;

/**
 *函数的定义或者编辑操作的Activity（对照EditConstantActivity）,若结果名字，参数，表达式合法，存入数据库
 */
public class EditFunctionActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEdtName;
    private EditText mEdtParaList;
    private EditText mEdtExpression;
    private EditText mEdtDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_function);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mEdtName = (EditText) findViewById(R.id.edtName);
        mEdtParaList = (EditText) findViewById(R.id.edtParaList);
        mEdtExpression = (EditText) findViewById(R.id.edtExpression);
        mEdtDetail = (EditText) findViewById(R.id.edtDetail);
        Intent intent = getIntent();
        if (intent.hasExtra("name")) {
            mEdtName.setText(intent.getStringExtra("name"));
            mEdtParaList.setText(intent.getStringExtra("paraList"));
            mEdtExpression.setText(intent.getStringExtra("expression"));
            mEdtDetail.setText(intent.getStringExtra("detail"));
        }
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOK).setOnClickListener(this);
        findViewById(R.id.btnAddition).setOnClickListener(this);
        findViewById(R.id.btnSubtraction).setOnClickListener(this);
        findViewById(R.id.btnMultiplication).setOnClickListener(this);
        findViewById(R.id.btnDivision).setOnClickListener(this);
        findViewById(R.id.btnPow).setOnClickListener(this);
        findViewById(R.id.btnRoot).setOnClickListener(this);
        findViewById(R.id.btnBrackets).setOnClickListener(this);
        final View RowOperation = findViewById(R.id.rowOperation);
        RowOperation.setVisibility(View.GONE);
        mEdtExpression.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RowOperation.setVisibility(View.VISIBLE);
                } else {
                    RowOperation.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("编辑未保存").setMessage("编辑未保存，退出吗？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditFunctionActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;

            case R.id.btnOK:
                String name = mEdtName.getText().toString();
                String paraList = mEdtParaList.getText().toString();
                String expression = mEdtExpression.getText().toString();
                String detail = mEdtDetail.getText().toString();
                String oldName = getIntent().getStringExtra("name");
                if (name.isEmpty()) {
                    Toast.makeText(EditFunctionActivity.this, "函数名不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (oldName == null || !oldName.equals(name)) {
                    if (Function.FUNCTIONS.containsKey(name) || Constant.CONSTANTS.containsKey(name)) {
                        Toast.makeText(EditFunctionActivity.this, "已有函数/常数使用该名称",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                try {
                    CustomFunctionExecutor.compile(expression, paraList);
                } catch (CalcException e) {
                    Toast.makeText(EditFunctionActivity.this, e.getDetail(), Toast.LENGTH_SHORT).show();
                    return;
                }
                CustomDefinitionDbHelper helper
                        = new CustomDefinitionDbHelper(EditFunctionActivity.this);
                helper.removeFunction(oldName);
                helper.addFunction(name, paraList, expression, detail);
                Toast.makeText(EditFunctionActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                EditFunctionActivity.this.finish();
                break;

            case R.id.btnAddition:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "+");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnSubtraction:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "-");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnMultiplication:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "×");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnDivision:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "÷");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnPow:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "^");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnRoot:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "√");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart());
                break;

            case R.id.btnBrackets:
                mEdtExpression.getEditableText().insert(mEdtExpression.getSelectionStart(), "()");
                mEdtExpression.setSelection(mEdtExpression.getSelectionStart() - 1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.btnCancel).performClick();
    }
}
