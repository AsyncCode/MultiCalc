package com.example.multicalc.matrix.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.example.multicalc.basic_calc.math.CalcException;
import com.example.multicalc.matrix.math.MatrixOrNumber;
import com.example.multicalc.matrix.math.RationalNumber;

/**
 * 顾名思义，MatrixView便是Matrix的View，这里的实际显示效果是把矩阵的所有元素显示字等行等列的一个GridLayout中，
 * 然后再对GridLayout画边界线，方便观察。
 */
public class MatrixView extends GridLayout {

    private Paint mPaint;
    private Context mContext;
    private int mRowCount;
    private int mColumnCount;
    private SmallEditText[][] mEditTexts;

    private MatrixView(Context context) {
        super(context);
        setPadding(20, 20, 20, 20);
        mContext = context;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    //传入行列数构建一个空白的MatrixView,editable参数用来标志使用者是否可以手动点击编辑
    public MatrixView(Context context, int row, int column, boolean editable) {
        this(context);
        setRowCount(mRowCount = row);
        setColumnCount(mColumnCount = column);
        mEditTexts = new SmallEditText[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                SmallEditText edt = new SmallEditText(mContext);
                edt.setTag(i * column + j);
                edt.setSingleLine(true);
                edt.setPadding(20, 5, 20, 5);
                edt.setSelectAllOnFocus(true);
                edt.setEditable(editable);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setGravity(Gravity.CENTER);
                edt.setLayoutParams(params);
                edt.setGravity(Gravity.CENTER);
                addView(edt);
                mEditTexts[i][j] = edt;
            }
        }
    }

    //结束一个矩阵的输入，因为MatrixView不仅有输出效果还有输入效果。
    //结束输入，尝试把每个单元格的输入转换为矩阵的元素。
    public MatrixOrNumber finishInput() {
        RationalNumber[][] numbers = new RationalNumber[mRowCount][mColumnCount];
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                try {
                    numbers[i][j] = RationalNumber.parse(mEditTexts[i][j].getText().toString());
                } catch (CalcException e) {
                    Toast.makeText(mContext, e.getDetail(), Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        }
        setEditable(false);
        return new MatrixOrNumber(numbers);
    }

    //使用时，需要改变当前输入的单元格，即改变当前获得焦点的EditText
    public void focusMove(int offset) {
        View oldFocus = findFocus();
        if (oldFocus != null) {
            offset += (Integer) oldFocus.getTag();
            offset = offset < 0 ? 0 : offset >= mRowCount * mColumnCount ?
                    mRowCount * mColumnCount - 1 : offset;
            mEditTexts[offset / mColumnCount][offset % mColumnCount].requestFocus();
        }
    }

    public void setTextSize(int size) {
        for (SmallEditText[] editTexts : mEditTexts) {
            for (SmallEditText editText : editTexts) {
                editText.setTextSize(size);
            }
        }
    }

    //设置MatrixView（实际为其中每个单元格的每个EditText）是否可以编辑
    public void setEditable(boolean editable) {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                mEditTexts[i][j].setEditable(editable);
            }
        }
    }

    //将所有文本输入转为二维字符串数组
    public String[][] getTexts() {
        String[][] texts = new String[mRowCount][mColumnCount];
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                texts[i][j] = mEditTexts[i][j].getText().toString();
            }
        }
        return texts;
    }

    //设置每个单元格文本，这在修改矩阵值，或者显示矩阵值得时候有用，先把矩阵值转为String[][]直接传入
    public void setTexts(String[][] texts) {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                mEditTexts[i][j].setText(texts[i][j]);
            }
        }
    }

    //画边界线和单元格的经纬线
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int left = getPaddingLeft();
        int right = getRight() - getLeft() - getPaddingRight();
        int top = getTop() + getPaddingTop();
        int bottom = getBottom() - getPaddingBottom();
        canvas.drawLine(left, top, right, top, mPaint);
        canvas.drawLine(left, bottom, right, bottom, mPaint);
        canvas.drawLine(left, top, left, bottom, mPaint);
        canvas.drawLine(right, top, right, bottom, mPaint);
        for (int j = 1; j < mColumnCount; j++) {
            int minX = getChildAt(j).getLeft();
            for (int i = 1; i < mRowCount; i++) {
                int x = getChildAt(i * mColumnCount + j).getLeft();
                minX = x < minX ? x : minX;
            }
            canvas.drawLine(minX, top, minX, bottom, mPaint);
        }
        for (int i = 1; i < mRowCount; i++) {
            int minY = getChildAt(i * mColumnCount).getTop();
            for (int j = 1; j < mColumnCount; j++) {
                int y = getChildAt(i * mColumnCount + j).getTop();
                minY = y < minY ? y : minY;
            }
            canvas.drawLine(left, minY, right, minY, mPaint);
        }
    }
}