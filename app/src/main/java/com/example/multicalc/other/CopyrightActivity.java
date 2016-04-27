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

        TextView content = new TextView(this);
        content.setTextColor(0xff111111);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        String str = "        本应用软件为开源软件，任何个人或组织皆可以自由地复制、分发软件的源代码及" +
                "安装包。且对安装文件的使用不做限制，但对于软件的源码的使用，希望大家从尊重作者劳动的角" +
                "度能做到以下几点：\n\n" +
                "        非商业使用的前提下：如果是部分引用源码，请在方便的请款下注明来源（注明本页底部" +
                "的邮箱和网址即可）；如果是发布软件的修改、衍生版本，请说明为修改、衍生版本，并保留本" +
                "页内容作为原版本说明。\n" +
                "        商业层面的使用的需向作者协商取得授权。\n";
        content.setText(str);
        container.addView(content);

        TextView email = new TextView(this);
        email.setText("作者邮箱：1943825697@qq.com");
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
