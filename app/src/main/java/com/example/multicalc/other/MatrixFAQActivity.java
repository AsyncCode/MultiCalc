package com.example.multicalc.other;

import android.os.Bundle;

import com.example.multicalc.other.FAQTemplateActivity;

/**
 * 矩阵运算模块的帮助界面
 */
public class MatrixFAQActivity extends FAQTemplateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String question, answer;

        question = "基本使用概述";
        answer = "不同于常见的Android矩阵计算工具：一次性输入矩阵，然后选择要计算的结果。作者设计时" +
                "致力于软件的使用风格接近MATLAB，一者让本就熟悉MATLAB的使用者可以驾轻就熟，二者MATLAB" +
                "的输入输出风格确实更加适用于连续、大量的矩阵计算。";
        addItem(question, answer);

        question = "界面";
        answer = "矩阵运算界面主要由上半部分（姑且称之为【工作窗口】）和下半部分的键盘区域组成，工作" +
                "窗口通过双击可以进入/退出全屏状态。工作窗口用来显示使用者的输入和结果输出，其中带有" +
                "命令提示符【 >> 】的表示输入操作。至于键盘区域，特殊的是上部多出来一个滚动条（暂且" +
                "称之为【变量条】吧)。";
        addItem(question, answer);

        question = "一个简单的计算示范";
        answer = "数学界的Hello World，来一个1+1=2吧。1按下、+按下、1按下、等于按下、等于按下……" +
                "（╯‵□′）╯︵┴─┴搞毛线啊什么破软件啊没反应。╮(╯▽╰)╭这里的等于号其实已经是" +
                "赋值运算符去了，回车才是执行运算的按钮，1+1回车走一个。";
        addItem(question, answer);

        question = "变量的概念";
        answer = "就是类似MATLAB中的变量啊，一个变量保存着一个矩阵（当然一阶方阵是数，数也就可以视为" +
                "一阶方阵）。不过不同MATLAB的是，这里的变量需要先定义再使用，点击新建就可以建立变量哦，" +
                "建立变量时，可以先不赋值，不过这么变量就还不能出现在等号右端。同样类似MATLAB中的特殊" +
                "变量ans，这里也是吧ans作为缺省赋值符号左端时的赋值对象。";
        addItem(question, answer);

        question = "变量条";
        answer = "MATLAB是电脑软件当然可以通过键盘敲变量名的，出于手机端的特性加了这个变量条，在变量条" +
                "中，点击新建可以定义一个新的变量，点击已有的变量可以往工作窗口输入，长按已有变量可以" +
                "删除或者修改其值。";
        addItem(question, answer);

        question = "更多按键说明";
        answer = " 按屏幕顺序来，回车刚才已经说了；左右按键，在输入算式的时候是光标左右移动一个字符，" +
                "在输入矩阵值的时候则是左右移动一个单元格。AC按钮，短按是全清一个算式或者一个矩阵单元" +
                "格，长按有更多清理选项，于此不一一赘述；删除按钮作用是删除一个字符；至于其他按钮，" +
                "除【更多】按钮可以选择更多功能，都是输入对应文字的按钮。";
        addItem(question, answer);

        question = "函数和运算符";
        answer = "支持的函数有【det求行列式】【inv求逆】【trans求转置】【rank求秩】【rref求行最简型】，" +
                "这些函数的函数名也是与MATLAB一致，还有一个特殊的函数【E(n)】，n为正整数，用来产生n阶" +
                "单位阵。至于运算符，其他的无需多言，只是【/】，如果右端为数则，表示除号或分号（在输入" +
                "矩阵时，是允许分号的），如果右端为矩阵，等价于乘以矩阵的逆。";
        addItem(question, answer);

        question = "界面卡慢";
        answer = "一般使用不存在如此问题，只是当工作窗口中的输入输出记录太多了，或者显示了一个很大的" +
                "矩阵就会造成界面卡慢（我曾机智地输出了个100阶方阵把手机卡死(ง •̀_•́)ง），以作者线代" +
                "没挂科的丰富学习经验，100阶矩阵基本没人会这么做，如果使用时略感卡顿，基本是是历史输入" +
                "输出太多的缘故，可以试试清屏。、这个问题咎在自定义控件的绘制速度跟不上，诚请大家在" +
                "软件开源后提出改进方案。";
        addItem(question, answer);


    }
}