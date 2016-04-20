package com.example.multicalc.other;

import android.os.Bundle;

/**
 * 复数运算模块的帮助界面
 */
public class ComplexFAQActivity extends FAQTemplateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String question, answer;

        question = "【弧度制】【角度制】和【弧度提出π】的区别";
        answer = "【弧度制】不需要解释；【弧度提出π】其实还是弧度制，只不过当以极坐标形式输出结果时，" +
                "会从结果的辐角中提出π；而当在【角度值】状态下时，使用∠以极坐标形式输入输出，辐角" +
                "使用角度值，并且arg函数也会返回角度结果。\n" +
                "但是不管如何选择，三角函数始终都是使用角度制！！！";
        addItem(question, answer);

        question = "支持的函数";
        answer = "除三角、对数等外，还有【求实部Re】【求虚部Im】【求模abs】【求辐角arg】【求共轭conj】。";
        addItem(question, answer);

        question = "结果多值的情况";
        answer = "如求对数，辐角等情况数学上会有多个结果，这里取主支值。";
        addItem(question, answer);

        question = "结果不精确的问题";
        answer = "软件存在形如【3∠(π)=-3+i(-3.673940397E-16)】情况，这是因为双精度浮点数的精度有限，" +
                "并且系统取圆周率、三角函数值等也不能完全精确。不过看到如此差别巨大的数量级，想必大家" +
                "应该能猜到结果虚部是0。";
        addItem(question, answer);

        question = "按键【e^(i)】的作用";
        answer = "完全是考虑到使用频繁而添加的一个快捷按键，和一个一个按没区别。";
        addItem(question, answer);
    }
}