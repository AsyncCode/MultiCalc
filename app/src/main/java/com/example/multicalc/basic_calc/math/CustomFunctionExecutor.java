package com.example.multicalc.basic_calc.math;

/**
 * 因为本身函数操作复杂，设计参数个数与参数在表达式中的各种问题，故设计这个类是作为对用户自定义函数的一个辅助类。
 * 这个类的对象可以看作是自定义函数的一个执行器（executor），把原来表示一个表达式的MathSignQueue做一些处理，
 * 使得参数所处位置被一些标识符来标识，待需执行，把对应位置的标识符替换为实参，即可像一般MathSignQueue一样求值
 */
public final class CustomFunctionExecutor {

    //同样是数学符号的一个队列,不过其中有特殊的MathSign即用来占位的MathSign:ParaPlaceHolder(见内部类)
    private MathSignQueue mQueueWithHolder = new MathSignQueue();

    //一个比喻，把这个类比做执行器（就像一个exe文件），这个返回类对象的静态方法就像一个编译器“编译”得出执行器
    public static CustomFunctionExecutor compile(String expression, String paraList)
            throws CalcException {
        expression = expression.replaceAll("\\s", "");
        paraList = paraList.replaceAll("\\s", "");
        CustomFunctionExecutor exe = new CustomFunctionExecutor();
        String[] paraNames = paraList.split(",");
        int paraCount = paraNames.length;
        for (int id = 0; id < paraCount; id++) {
            if (paraNames[id].length() == 0) {
                throw new CalcException("参数名不能为空");
            }
            for (int j = 0; j < id; j++) {
                if (paraNames[id].equals(paraNames[j])) {
                    throw new CalcException("不能有同名参数");
                }
            }
        }
        while (true) {
            int firstParaId = -1, firstParaIndex = expression.length();
            for (int id = 0; id < paraNames.length; id++) {
                int index = expression.indexOf(paraNames[id]);
                if (index >= 0 && index < firstParaIndex) {
                    firstParaIndex = index;
                    firstParaId = id;
                }
            }
            if (firstParaId != -1) {
                if (firstParaIndex != 0) {
                    exe.mQueueWithHolder.append(
                            MathSignQueue.parse(expression.substring(0, firstParaIndex)));
                }
                exe.mQueueWithHolder.add(new ParaPlaceHolder(firstParaId));
                expression = expression.substring(firstParaIndex + paraNames[firstParaId].length());
                if (expression.length() == 0) {
                    break;
                }
            } else {
                exe.mQueueWithHolder.append(MathSignQueue.parse(expression));
                break;
            }
        }
        return exe;
    }

    //执行函数，把占位符mQueueWithHolder中的参数占位符替换为实参，然后执行之求值
    public RealNumber execute(int angularUnit, RealNumber... paraValues) throws CalcException {
        mQueueWithHolder.setAngularUnit(angularUnit);
        for (int i = 0; i < mQueueWithHolder.size(); i++) {
            if (mQueueWithHolder.get(i) instanceof ParaPlaceHolder) {
                ParaPlaceHolder holder = (ParaPlaceHolder) mQueueWithHolder.get(i);
                mQueueWithHolder.remove(i);
                mQueueWithHolder.add(i, paraValues[holder.id]);
            }
        }
        return mQueueWithHolder.queueValue();
    }

    //应为这个执行过程是有损的，之后执行器不可继续带入实参执行的出结果
    //在数据库帮助类CustomDefinitionDbHelper中，从数据库中第一次读取表达式并编译函数的执行器，然后会保存一份
    //在帮助类的缓存（一个静态HashMap中），此后亦可以利用缓存中的拷贝继续拷贝的出编译器
    public CustomFunctionExecutor copy() {
        CustomFunctionExecutor backup = new CustomFunctionExecutor();
        backup.mQueueWithHolder = mQueueWithHolder.subQueue(0, mQueueWithHolder.size());
        return backup;
    }

    //参数占位符类，继承自MathSign,内部记录了参数在参数列表中对应哪个参数，将在执行器被对应实参替换掉
    private static class ParaPlaceHolder extends MathSign {
        public int id;

        public ParaPlaceHolder(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return null;
        }
    }
}