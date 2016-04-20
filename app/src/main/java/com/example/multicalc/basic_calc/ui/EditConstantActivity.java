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
import com.example.multicalc.basic_calc.math.Constant;
import com.example.multicalc.basic_calc.math.CustomDefinitionDbHelper;
import com.example.multicalc.basic_calc.math.Function;

/**
 *常数值的定义或者编辑（可视为输入框中填入了旧值得新建操作）操作的Activity,若名字和数值合法，存入数据库
 */
public class EditConstantActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEdtName;
    private EditText mEdtValue;
    private EditText mEdtDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_constant);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mEdtName = (EditText) findViewById(R.id.edtName);
        mEdtValue = (EditText) findViewById(R.id.edtValue);
        mEdtDetail = (EditText) findViewById(R.id.edtDetail);
        Intent intent = getIntent();
        if (intent.hasExtra("name")) {
            mEdtName.setText(intent.getStringExtra("name"));
            mEdtValue.setText(String.valueOf(intent.getDoubleExtra("value", 0)));
            mEdtDetail.setText(intent.getStringExtra("detail"));
        }
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOK).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("编辑未保存").setMessage("确认退出？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditConstantActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;

            case R.id.btnOK:
                String name = mEdtName.getText().toString();
                double value;
                try {
                    value = Double.parseDouble(mEdtValue.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(EditConstantActivity.this, "数值格式非法",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String detail = mEdtDetail.getText().toString();
                String oldName = getIntent().getStringExtra("name");
                if (name.isEmpty()) {
                    Toast.makeText(EditConstantActivity.this, "常数名不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (oldName == null || !oldName.equals(name)) {
                    if (Function.FUNCTIONS.containsKey(name) || Constant.CONSTANTS.containsKey(name)) {
                        Toast.makeText(EditConstantActivity.this, "已有函数/常数使用该名称",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                CustomDefinitionDbHelper helper = new CustomDefinitionDbHelper(EditConstantActivity.this);
                helper.removeConstant(oldName);
                helper.addConstant(name, value, detail);
                Toast.makeText(EditConstantActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                EditConstantActivity.this.finish();
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