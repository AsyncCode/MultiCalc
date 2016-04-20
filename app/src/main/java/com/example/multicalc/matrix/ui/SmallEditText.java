package com.example.multicalc.matrix.ui;

import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * 小文本框，拥有禁止软键盘弹出，禁止ActionMode.Callback回调等效果，如此便只能接受键盘的输入。
 * 不能直接使用TextView代替主要是考虑到光标的存在与使用。
 */
public class SmallEditText extends EditText {
    private static ActionMode.Callback sNoCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    public SmallEditText(Context context) {
        super(context);
        setBackground(null);
        setCustomSelectionActionModeCallback(sNoCallBack);
        try {
            Method method = EditText.class.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(this, false);
        } catch (Exception e) {
            Log.d(getClass().getName(), Log.getStackTraceString(e));
        }
    }

    //设置文本框能否编辑
    public void setEditable(boolean editable) {
        if (editable) {
            setEnabled(true);
            setMinimumWidth(getPaddingStart() + getPaddingEnd() + 30);
        } else {
            setMinimumWidth(0);
            setEnabled(false);
            setTextColor(0xff000000);
        }
    }

    //在光标处插入字符串
    public void insert(String str) {
        getEditableText().replace(getSelectionStart(), getSelectionEnd(), str);
        if (getSelectionEnd() != getSelectionStart()) {
            setSelection(getSelectionEnd(), getSelectionEnd());
        }
    }

    //光标移动
    public void cursorMove(int offset) {
        offset += getSelectionStart();
        offset = offset < 0 ? 0 : offset > length() ? length() : offset;
        setSelection(offset);
    }

    //删除光标前的字符
    public void delete() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start != end) {
            getEditableText().delete(start, end);
        } else if (start > 0) {
            getEditableText().delete(start - 1, start);
        }
    }
}