package com.example.multicalc.matrix.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.matrix.math.MatrixOrNumber;
import com.example.multicalc.matrix.math.MatrixSignQueue;
import com.example.multicalc.matrix.math.MatrixVariable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * WorkWindow，即工作窗口，是矩阵运算模块的主要交互界面，在此，可以浏览历史输入输出，并实现当前的输入或输出。
 * 其主要的显示内容分为三种：
 * 一种是“命令行”，主要由一个SmallEditText构成，接受用户输入的算式
 * 二者是“矩阵输入输出区域”（MatrixIOField）,当在为矩阵赋值或者显示一个矩阵运算结果时有用
 * 三者是“错误显示文本框”，一个简单的文本区域而已
 *
 * WorkWindow内部把MatrixView以及SmallEditText的方法再一次封装起来，例如insert,外界只需调用WorkWindow的
 * insert，而WorkWindow会根据当前所处状态，自行调用对于子控件的插入方法
 */
public class WorkWindow extends ScrollView {

    private final static int STATUS_NOTHING = 0;
    private final static int STATUS_COMMAND_LINE = 1;
    private final static int STATUS_MATRIX_INPUT = 2;

    private Activity mActivity;
    private LinearLayout mContainer;
    private int mTextSize;
    private OnTouchListener mOnTouchListener;

    private CommandLine mLine;
    private MatrixIOField mField;
    private int mInputStatus;

    public WorkWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
        mActivity = (Activity) context;
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(10, 10, 10, 50);
        addView(mContainer);
    }

    //结束当前的输入，转而开始输入一个新的命令行
    public void startCommandLine() {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mContainer.removeView(mLine);
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            mContainer.removeView(mField);
        }
        mInputStatus = STATUS_COMMAND_LINE;
        mLine = new CommandLine();
        mLine.editText.requestFocus();
        mContainer.addView(mLine);
        scrollToBottom();
    }

    //结束当前输入，开始为一个矩阵值赋值，参数defaultString用来设置每个单元格的缺省字符串
    //相信大家在实际线代课程学习中碰到过某系矩阵拥有一堆0或类似情况
    public void startMatrixInput(String varName, int row, int column, String defaultString) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mContainer.removeView(mLine);
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            mContainer.removeView(mField);
        }
        mInputStatus = STATUS_MATRIX_INPUT;
        mField = new MatrixIOField(varName, row, column, true);
        if (defaultString != null && !defaultString.isEmpty()) {
            String[][] texts = new String[row][column];
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    texts[i][j] = defaultString;
                }
            }
            mField.matrixView.setTexts(texts);
        }
        mField.matrixView.getChildAt(0).requestFocus();
        mContainer.addView(mField);
        scrollToBottom();
    }

    //结束当前输入并开始修改矩阵的已有值
    public void startMatrixModify(String varName, MatrixOrNumber data) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mContainer.removeView(mLine);
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            mContainer.removeView(mField);
        }
        mInputStatus = STATUS_MATRIX_INPUT;
        mField = new MatrixIOField(varName, data, true);
        mField.matrixView.getChildAt(0).requestFocus();
        mContainer.addView(mField);
        scrollToBottom();
    }

    //输出，或者是展示，作用，展示一个矩阵的值，其中参数withPrompt决定展示时是否带有命令提示符
    public void startShowMatrix(String varName, boolean withPrompt) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mContainer.removeView(mLine);
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            mContainer.removeView(mField);
        }
        mContainer.addView(
                new MatrixIOField(varName, MatrixVariable.VARIABLES.get(varName), withPrompt));
        mInputStatus = STATUS_NOTHING;
        startCommandLine();
    }

    //控件生成时，自动从文件中读取上一次的显示状态
    //不过实践发现这一步在历书输入过多的情况非常耗时，这时会导致黑屏，界面卡死，
    //解决方法为：把控件的构建，绘制操作放在一个新线程中进行，然后最后主线程一次性addView()
    //同时，主线程中生成一个对话框表明当前状态，并提供中止按钮，中止读取
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final boolean[] continueToRead = new boolean[]{true};
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("请稍候");
        builder.setMessage("正在读取历史输入输入记录，如果选择中止，则只读取最近一部分记录。");
        builder.setCancelable(false);
        builder.setNegativeButton("中止", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                continueToRead[0] = false;
            }
        });
        final Dialog dialog = builder.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final LinkedList<View> views = new LinkedList<>();
                final boolean success[] = new boolean[]{true};
                Looper.prepare();
                File file = new File(mActivity.getFilesDir().getPath(), "matrixPreviousInput");
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    int count = dis.readInt();
                    for (int index = count; continueToRead[0] && index > 0; index--) {
                        byte viewType = dis.readByte();
                        if (viewType == 0) {
                            CommandLine line = new CommandLine();
                            line.editText.setText(dis.readUTF());
                            line.editText.setEditable(index == count);
                            views.add(0, line);
                        } else if (viewType == 1) {
                            String varName = dis.readUTF();
                            int row = dis.readInt();
                            int column = dis.readInt();
                            boolean withPrompt = dis.readBoolean();
                            MatrixIOField filed = new MatrixIOField(varName, row, column, withPrompt);
                            String[][] texts = new String[row][column];
                            for (int i = 0; i < row; i++) {
                                for (int j = 0; j < column; j++) {
                                    texts[i][j] = dis.readUTF();
                                }
                            }
                            filed.matrixView.setTexts(texts);
                            filed.matrixView.setEditable(index == count);
                            views.add(0, filed);
                        } else if (viewType == 2) {
                            views.add(0, new ErrorPrinter(dis.readUTF()));
                        }
                    }
                } catch (IOException e) {
                    success[0] = false;
                } finally {
                    if (dis != null) {
                        try {
                            dis.close();
                            if (!success[0] && file.exists() && !file.delete()) {
                                Log.d(getClass().getName(), "读取失败且删除失败");
                            }
                        } catch (IOException e) {
                            Log.d(getClass().getName(), Log.getStackTraceString(e));
                        }
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!success[0] || views.isEmpty()) {
                                clearWindow();
                            } else {
                                for (View view : views) {
                                    mContainer.addView(view);
                                }
                                View theLastOne = views.getLast();
                                if (theLastOne instanceof CommandLine) {
                                    mInputStatus = STATUS_COMMAND_LINE;
                                    mLine = (CommandLine) theLastOne;
                                    mLine.requestFocus();
                                } else {
                                    mInputStatus = STATUS_MATRIX_INPUT;
                                    mField = (MatrixIOField) theLastOne;
                                    mField.getChildAt(0).requestFocus();
                                }
                            }
                            dialog.dismiss();
                            scrollToBottom();
                        }
                    });
                }
            }
        }).start();
    }

    //工作窗口销毁前，把以显示的部分存入文件中，使得可以下次读取
    //值得一提，窗口中后显示的部分先存入，下次读取时，即使被使用者中止，也会显示出已读取得最近历史记录
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        File file = new File(mActivity.getFilesDir().getPath(), "matrixPreviousInput");
        boolean success = true;
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            int count = mContainer.getChildCount();
            dos.writeInt(count);
            while (count-- > 0) {
                View view = mContainer.getChildAt(count);
                if (view instanceof CommandLine) {
                    dos.writeByte(0);
                    dos.writeUTF(((CommandLine) view).editText.getText().toString());
                } else if (view instanceof MatrixIOField) {
                    dos.writeByte(1);
                    MatrixIOField field = (MatrixIOField) view;
                    String[][] texts = field.matrixView.getTexts();
                    dos.writeUTF(field.varName);
                    dos.writeInt(texts.length);
                    dos.writeInt(texts[0].length);
                    dos.writeBoolean(field.withPrompt);
                    for (String[] strings : texts) {
                        for (String string : strings) {
                            dos.writeUTF(string);
                        }
                    }
                } else if (view instanceof ErrorPrinter) {
                    dos.writeByte(2);
                    dos.writeUTF(((ErrorPrinter) view).getText().toString());
                }
            }
            dos.flush();
        } catch (IOException e) {
            success = false;
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                    if (!success && file.exists() && !file.delete()) {
                        Log.d(getClass().getName(), "写入失败且删除失败");
                    }
                } catch (IOException e) {
                    Log.d(getClass().getName(), Log.getStackTraceString(e));
                }
            }
        }
    }

    //插入函数，根据窗口当前状态，自动决定向“命令行”还是向“矩阵输入区域”中插入
    public void insert(String s) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mLine.editText.insert(s);
        } else if (mInputStatus == STATUS_MATRIX_INPUT && s.matches("([\\.0-9/-])+")) {
            SmallEditText editText = (SmallEditText) mField.matrixView.findFocus();
            if (editText != null) {
                editText.insert(s);
            }
        }
    }

    //插入并且光标位移，仅对命令行的输入有效
    public void insert(String s, int offset) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mLine.editText.insert(s);
            mLine.editText.cursorMove(offset);
        }
    }

    //对命令行，是光标位移，对矩阵输入区域，是焦点单元格的移动
    public void move(int offset) {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mLine.editText.cursorMove(offset);
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            mField.matrixView.focusMove(offset);
        }
    }

    //结束输入，如果是命令行的结束输入，则执行计算，如果是矩阵输入区域的结束，则为对应矩阵赋值
    public void finishInput() {
        VariableBar varBar = ((VariableBar) mActivity.findViewById(R.id.variableBar));
        if (mInputStatus == STATUS_COMMAND_LINE) {
            String input = mLine.editText.getText().toString();
            mLine.editText.setEditable(false);
            mInputStatus = STATUS_NOTHING;
            try {
                MatrixSignQueue queue = MatrixSignQueue.parse(input);
                String varName = queue.get(0).toString();
                MatrixOrNumber result = queue.subQueue(2, queue.size()).queueValue();
                varBar.addVarWithBtn(varName, result);
                startShowMatrix(varName, false);
            } catch (CalcException e) {
                mContainer.addView(new ErrorPrinter(e));
                startCommandLine();
                mLine.editText.setText(input);
                mLine.editText.cursorMove(input.length());
            }
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            MatrixOrNumber result = mField.matrixView.finishInput();
            if (result != null) {
                varBar.addVarWithBtn(mField.varName, result);
                mInputStatus = STATUS_NOTHING;
                startCommandLine();
            }
        }
    }

    //清除当前输入区域的文字
    public void clearInput() {
        if (mInputStatus == STATUS_COMMAND_LINE) {
            mLine.editText.setText("");
        } else if (mInputStatus == STATUS_MATRIX_INPUT) {
            SmallEditText editText = (SmallEditText) mField.matrixView.findFocus();
            if (editText != null) {
                editText.setText("");
            }
        }
    }

    //清屏
    public void clearWindow() {
        mContainer.removeAllViews();
        startCommandLine();
    }

    public void setTextSize(int size) {
        mTextSize = size;
    }

    public int getTextSize() {
        return mTextSize;
    }

    //把屏幕滑向最底部
    public void scrollToBottom() {
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, mContainer.getMeasuredHeight());
            }
        });
    }

    //为了实现双击全屏，需要为WorkWindow实现OnTouchListener，但是发现，这样会使得双击在子控件上双击无效
    //这里通过把OnTouchListener保存下来，在onInterceptTouchEvent中调用其中的方法，如此在子控件上双击也有效
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
        super.setOnTouchListener(null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(this, ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 命令行区域，主要部分为一个SmallEditText
     */
    private class CommandLine extends LinearLayout {
        SmallEditText editText;

        public CommandLine() {
            super(mActivity);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setOrientation(HORIZONTAL);
            setLayoutParams(params);
            TextView tv = new TextView(mActivity);
            tv.setText(">> ");
            tv.setTextColor(Color.BLUE);
            tv.setTextSize(mTextSize);
            addView(tv);
            editText = new SmallEditText(mActivity);
            editText.setTextSize(mTextSize);
            editText.setPadding(0, 0, 0, 0);
            addView(editText, params);
        }
    }

    /**
     * 矩阵输入输出区域，显示一个矩阵的结果或者允许一个矩阵的赋值输入
     */
    private class MatrixIOField extends HorizontalScrollView {
        MatrixView matrixView;
        String varName;
        boolean withPrompt;

        public MatrixIOField(String varName, int row, int column, boolean withPrompt) {
            super(mActivity);
            this.varName = varName;
            this.withPrompt = withPrompt;
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setLayoutParams(params);
            setHorizontalScrollBarEnabled(false);
            LinearLayout linearLayout = new LinearLayout(mActivity);
            addView(linearLayout);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(params);
            if (withPrompt) {
                TextView tv = new TextView(mActivity);
                tv.setText(">> ");
                tv.setTextSize(mTextSize);
                tv.setTextColor(Color.BLUE);
                linearLayout.addView(tv);
            }
            TextView tv = new TextView(mActivity);
            varName = varName + " =";
            tv.setText(varName);
            tv.setTextSize(mTextSize);
            tv.setTextColor(Color.BLACK);
            linearLayout.addView(tv);
            matrixView = new MatrixView(mActivity, row, column, withPrompt);
            matrixView.setTextSize(mTextSize);
            linearLayout.addView(matrixView);
        }

        public MatrixIOField(String varName, MatrixOrNumber data, boolean withPrompt) {
            this(varName, data.row(), data.column(), withPrompt);
            matrixView.setTexts(data.toStrings());
        }
    }

    /**
     * 错误显示区域，红色文本强调效果
     */
    private class ErrorPrinter extends TextView {
        public ErrorPrinter() {
            super(mActivity);
            setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            setTextColor(Color.RED);
            setTextSize(mTextSize);
        }

        public ErrorPrinter(CalcException e) {
            this();
            setText(e.getDetail());
        }

        public ErrorPrinter(String text) {
            this();
            setText(text);
        }
    }
}