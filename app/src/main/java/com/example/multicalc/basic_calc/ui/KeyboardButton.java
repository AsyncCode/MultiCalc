package com.example.multicalc.basic_calc.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * 键盘按钮的Button，有按下效果
 */
public class KeyboardButton extends ImageView {

    public KeyboardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
        setScaleType(ScaleType.FIT_XY);
        //让对应Activity实现OnClickListener接口，所以直接把context强制转换设定即可，避免大量设定代码
        setOnClickListener((OnClickListener) context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getDrawable().setColorFilter(0xff2277ee, PorterDuff.Mode.MULTIPLY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getDrawable().clearColorFilter();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    //使长按代替连续点击生效
    public void enableLongClickForRepeat() {
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread() {
                        @Override
                        public void run() {
                            //前500ms如果松了手，判定为不是长按，取消执行
                            for (int i = 0; i < 10; i++) {
                                SystemClock.sleep(50);
                                if (!v.isPressed()) {
                                    return;
                                }
                            }
                            //500ms后，每90ms检查一次，如果还在按，执行一次点击动作
                            while (v.isPressed()) {
                                SystemClock.sleep(90);
                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        v.performClick();
                                    }
                                });
                            }
                        }
                    }.start();
                }
                return false;
            }
        });
    }
}
