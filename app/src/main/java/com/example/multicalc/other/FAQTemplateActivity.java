package com.example.multicalc.other;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.multicalc.R;

/**
 * 帮助界面的一个父类（就像一个模板），完成了界面的基本绘制（虽然很丑）
 * 但向外提供addItem()方法，使得子类可以方便添加问答条目
 */
public class FAQTemplateActivity extends AppCompatActivity {

    private LinearLayout mContainer;
    private LinearLayout.LayoutParams paramsQuestion;
    private LinearLayout.LayoutParams paramsAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(ContextCompat.getColor(this, R.color.PageBackground));
        mContainer = new LinearLayout(this);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(mContainer);
        setContentView(scroll);

        paramsQuestion = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsQuestion.setMargins(20, 20, 20, 20);
        paramsAnswer = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsAnswer.setMargins(20, 0, 20, 40);


    }

    protected void addItem(String question, String answer) {
        TextView tvQuestion = new TextView(this);
        tvQuestion.setLayoutParams(paramsQuestion);
        tvQuestion.setTextColor(0xffcc3333);
        tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        tvQuestion.setText(question);
        mContainer.addView(tvQuestion);

        TextView tvAnswer = new TextView(this);
        tvAnswer.setLayoutParams(paramsAnswer);
        tvAnswer.setTextColor(0xff111111);
        tvAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvAnswer.setText(answer);
        mContainer.addView(tvAnswer);
    }
}
