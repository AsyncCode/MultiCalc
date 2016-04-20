package com.example.multicalc.matrix.math;

import android.util.Log;

import com.example.multicalc.basic_calc.math.CalcException;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 同样，也是一个对于Sign的Queue，这里是对于MatrixSign的MatrixSignQueue
 */
public class MatrixSignQueue {

    private String mTextInput = null;
    private int mParseIndex = 0;
    private LinkedList<MatrixSign> mMatrixSigns = new LinkedList<>();

    public static MatrixSignQueue parse(String input) throws CalcException {
        MatrixSignQueue queue = new MatrixSignQueue();
        if (input == null || input.length() == 0) {
            throw new CalcException("输入为空");
        }
        queue.mTextInput = input;
        while (queue.mParseIndex < input.length()) {
            if (!queue.parseOneSign()) {
                throw new CalcException("不能解析的部分", queue.mParseIndex, queue.mTextInput.length());
            }
        }
        if (queue.mMatrixSigns.size() == 1 && queue.mMatrixSigns.get(0) instanceof MatrixVariable) {
            queue.mMatrixSigns.add(0, new MatrixVariable(queue.mMatrixSigns.get(0).toString()));
            queue.add(1, new MatrixOperator("="));
        } else if (queue.size() < 2 || !(queue.get(0) instanceof MatrixVariable) ||
                !queue.get(1).toString().equals("=")) {
            queue.add(0, new MatrixVariable("ans"));
            queue.add(1, new MatrixOperator("="));
        }
        for (int i = 2; i < queue.size(); i++) {
            if (queue.get(i) instanceof MatrixVariable &&
                    !((MatrixVariable) queue.get(i)).initialized()) {
                throw new CalcException("未初始化的变量不能使用");
            }
            if (queue.get(i) instanceof MatrixOperator &&
                    (queue.get(i).toString().equals("="))) {
                throw new CalcException("不合法的赋值");
            }
        }
        return queue;
    }

    public boolean parseOneSign() {
        //跳过空白符号
        while (true) {
            char c = mTextInput.charAt(mParseIndex);
            if (c != ' ' && c != '\t' && c != '\n') {
                break;
            } else if (++mParseIndex >= mTextInput.length()) {
                return true;
            }
        }
        String residualPart = mTextInput.substring(mParseIndex);

        //尝试解析出一个函数
        for (String funName : MatrixFunction.FUNCTIONS) {
            if (residualPart.startsWith(funName)) {
                MatrixFunction funToAdd = new MatrixFunction(funName);
                mMatrixSigns.add(funToAdd);
                mParseIndex += funName.length();
                return true;
            }
        }

        //尝试解析出一个变量
        for (String varName : MatrixVariable.VARIABLES.keySet()) {
            if (residualPart.startsWith(varName)) {
                MatrixVariable varToAdd = new MatrixVariable(varName);
                mMatrixSigns.add(varToAdd);
                mParseIndex += varName.length();
                return true;
            }
        }

        //尝试解析出一个运算符
        for (String op : MatrixOperator.OPERATORS) {
            if (residualPart.startsWith(op)) {
                MatrixOperator opToAdd = new MatrixOperator(op);
                mMatrixSigns.add(opToAdd);
                mParseIndex += op.length();
                return true;
            }
        }

        //尝试解析出一个数字
        Matcher matcher = Pattern.compile("[0-9]+(\\.[0-9]+)?").matcher(residualPart);
        if (matcher.find() && matcher.start() == 0) {
            MatrixOrNumber numToAdd;
            try {
                numToAdd = new MatrixOrNumber(RationalNumber.parse(matcher.group()));
            } catch (CalcException e) {
                numToAdd = null;
                Log.d(getClass().getName(), Log.getStackTraceString(e));
            }
            mMatrixSigns.add(numToAdd);
            mParseIndex += matcher.end();
            return true;
        }
        return false;
    }

    public MatrixSign get(int index) {
        return mMatrixSigns.get(index);
    }

    public void add(MatrixSign sign) {
        mMatrixSigns.add(sign);
    }

    public void add(int location, MatrixSign sign) {
        mMatrixSigns.add(location, sign);
    }

    public int size() {
        return mMatrixSigns.size();
    }

    public void simplify(int start, int end, MatrixSign result) {
        while (end-- > start) {
            mMatrixSigns.remove(start);
        }
        mMatrixSigns.add(start, result);
    }

    public MatrixSignQueue subQueue(int start, int end) {
        MatrixSignQueue subQueue = new MatrixSignQueue();
        while (start < end) {
            subQueue.mMatrixSigns.add(this.mMatrixSigns.get(start++));
        }
        return subQueue;
    }

    public MatrixOrNumber queueValue() throws CalcException {
        if (size() == 0) {
            throw new CalcException("输入为空");
        }
        MatrixFunction.eliminateAllFunction(this);
        MatrixOperator.eliminateAllOperator(this);
        return (MatrixOrNumber) get(0);
    }
}
