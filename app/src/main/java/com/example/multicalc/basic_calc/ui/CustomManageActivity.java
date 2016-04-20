package com.example.multicalc.basic_calc.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.example.multicalc.R;

/**
 * 一个Activity控制着两个Fragment（FunctionManageFragment 和 ConstantManageFragment）的Activity
 * 可以进行Fragment的切换操作
 */
public class CustomManageActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_manage);
        Button btnFunction = (Button) findViewById(R.id.btnFunction);
        Button btnConstant = (Button) findViewById(R.id.btnConstant);
        btnFunction.setOnClickListener(this);
        btnConstant.setOnClickListener(this);
        switch (getIntent().getStringExtra("whichFragment")) {
            case "function":
                btnFunction.performClick();
                break;
            case "constant":
                btnConstant.performClick();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFunction:
                findViewById(R.id.btnFunction)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.ButtonSelected));
                findViewById(R.id.btnConstant)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.ButtonNormal));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentField, new FunctionManageFragment()).commit();
                break;
            case R.id.btnConstant:
                findViewById(R.id.btnFunction)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.ButtonNormal));
                findViewById(R.id.btnConstant)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.ButtonSelected));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentField, new ConstantManageFragment()).commit();
                break;
            default:
                break;
        }
    }
}
