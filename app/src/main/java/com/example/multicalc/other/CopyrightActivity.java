package com.example.multicalc.other;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.multicalc.R;

/**
 * 版权声明Activity
 */
public class CopyrightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(makeView());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private View makeView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(30, 30, 30, 150);

        TextView title = new TextView(this);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xffff0000);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        title.setText("版权声明\n");
        container.addView(title);

        TextView warning = new TextView(this);
        warning.setTextColor(0xffcc3333);
        warning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        warning.setText("        依照我国法律，计算机程序自作品完成之日起，作者自动享有其著作权。\n");
        container.addView(warning);

        TextView content = new TextView(this);
        content.setTextColor(0xff111111);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        String str = "        本应用软件为“2016年（第八届）上海市大学生计算机应用能力大赛”参赛作品，" +
                "在大赛结束前，谢绝向除大赛有关负责部门外的任何个人、组织提供源代码，并且请体验人员在" +
                "无必要情况下不要大范围传播安装包。\n\n" +
                "        大赛结束后，包括大赛负责部门在内的任何个人或组织皆可以在非商业用途前提下自由" +
                "复制、分发、修改和使用本应用的源代码及安装包，但必须保留本页全部内容。如果是部分引用，" +
                "标明作者联系邮箱、项目网址（见本页底部）以示出处即可。\n\n" +
                "        任何商业用途必须通过上示联系方式与负责人协商取得授权！将软件及其修改、衍生版" +
                "本投放至电子市场（无论是否营利）都需获得授权。\n\n" +
                "        虽然本应用并无多少过人之处，作者也能力平平，但希望大家尊重劳动、尊重知识产权" +
                "、尊重法律，谢谢！\n\n" +
                "        同时，开放源代码旨在方便大家学习交流，也有利创作者的能力提升，欢迎大家对软件" +
                "的缺陷提出反馈建议与改进方案，以期软件日臻完善，更多地投入实际使用。\n\n";
        content.setText(str);
        container.addView(content);

        TextView email = new TextView(this);
        email.setText("联系邮箱：1943825697@qq.com");
        email.setTextColor(0xff000000);
        email.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        email.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        email.setMovementMethod(LinkMovementMethod.getInstance());
        container.addView(email);

        TextView website = new TextView(this);
        website.setTextColor(0xff000000);
        website.setText("项目网址：https://AsyncCode.github.io/MultiCalc/");
        website.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        website.setAutoLinkMask(Linkify.WEB_URLS);
        website.setMovementMethod(LinkMovementMethod.getInstance());
        container.addView(website);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(ContextCompat.getColor(this, R.color.PageBackground));
        scroll.addView(container);
        return scroll;
    }
}
