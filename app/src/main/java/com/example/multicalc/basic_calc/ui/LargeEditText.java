package com.example.multicalc.basic_calc.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 *主界面的大输入框，拥有不弹出键盘，带有下划线等性质
 * 并且有更多更符合实际需求的插入、删除、光标移动等函数
 */
public class LargeEditText extends EditText {

    private int mCurrentLineStartIndex;
    private Paint mPaint = new Paint();
    private Rect mRect = new Rect();
    private int mTotalLineCount = 0;
    public LargeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        setGravity(Gravity.TOP);
        setBackground(null);
        setSingleLine(false);
        //禁止弹出软键盘
        try {
            Method method = EditText.class.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(this, false);
        } catch (Exception e) {
            Log.d(getClass().getName(), Log.getStackTraceString(e));
        }
    }

    //按照行高和总高度计算行数，若为wrap_content，默认包含5行
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mTotalLineCount = 5;
        } else {
            int totalHeight = getMeasuredHeight();
            mTotalLineCount = totalHeight / getLineHeight() + 1;
        }
        setLines(mTotalLineCount);
    }

    //画文本下划线
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int textLineCount = getLineCount();
        int lineHeight = getLineHeight();
        int startX = getPaddingLeft();
        int endX = getRight() - getLeft() - getPaddingRight();
        for (int i = 0; i < textLineCount; i++) {
            getLineBounds(i, mRect);
            canvas.drawLine(startX, mRect.bottom, endX, mRect.bottom, mPaint);
        }
        if (mTotalLineCount > textLineCount) {
            int blankLineCount = mTotalLineCount - textLineCount;
            for (int i = 1; i <= blankLineCount; i++) {
                float currentHeight = mRect.bottom + i * lineHeight;
                canvas.drawLine(startX, currentHeight, endX, currentHeight, mPaint);
            }
        }
    }

    //在光标出插入
    public void insert(String str) {
        getEditableText().replace(getSelectionStart(), getSelectionEnd(), str);
        if (getSelectionEnd() != getSelectionStart()) {
            setSelection(getSelectionEnd(), getSelectionEnd());
        }
    }

    //插入后且光标移动
    public void insert(String str, int offset) {
        insert(str);
        cursorMove(offset);
    }

    //光标移动
    public void cursorMove(int offset) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start == end) {
            start += offset;
            start = start < 0 ? 0 : start > length() ? length() : start;
            setSelection(start);
        } else {
            if (offset < 0) {
                start += offset;
            } else {
                end += offset;
            }
            start = start < 0 ? 0 : start > length() ? length() : start;
            end = end < 0 ? 0 : end > length() ? length() : end;
            setSelection(start, end);
        }
    }

    //删除光标前一个字符或者选中区域
    public void delete() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start != end) {
            getEditableText().delete(start, end);
        } else if (start > 0) {
            getEditableText().delete(start - 1, start);
        }
    }

    //判断光标是否在行首，在连续计算（加减乘除前一个操作数的自动填充）时会用到
    public boolean isLineHead() {
        return getSelectionStart() == 0 || getText().charAt(getSelectionStart() - 1) == '\n';
    }

    //获取当前行的表达式
    public String handleCurrentLine() {
        String allText = getText().toString();
        int start = getSelectionStart();
        int end = start;
        while (start > 0 && allText.charAt(start - 1) != '\n') {
            start--;
        }
        while (end < allText.length() && allText.charAt(end) != '\n') {
            end++;
        }
        mCurrentLineStartIndex = start;
        int indexOfEqualSign = allText.indexOf('=', start);
        if (indexOfEqualSign >= start && indexOfEqualSign < end) {
            getEditableText().delete(indexOfEqualSign + 1, end);
            setSelection(indexOfEqualSign + 1);
            return allText.substring(start, indexOfEqualSign);
        } else {
            setSelection(end);
            insert("=");
            return allText.substring(start, end);
        }
    }

    public int lineStart() {
        return mCurrentLineStartIndex;
    }
}