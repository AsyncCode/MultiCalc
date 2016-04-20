package com.example.multicalc.basic_calc.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.multicalc.R;
import com.example.multicalc.basic_calc.math.CustomDefinitionDbHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * 用于管理自定义常数的Fragment,功能在于获得数据库中的已储存常数，将之填装在一个ListView中形成一个一览图
 * 可以进行概况一栏，点击进入编辑，长按可以删除
 */
public class ConstantManageFragment extends Fragment {

    private LinkedList<String> mNames = new LinkedList<>();
    private LinkedList<Double> mValues = new LinkedList<>();
    private LinkedList<String> mDetails = new LinkedList<>();
    private MyAdapter mAdapter;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mActivity.findViewById(R.id.btnNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, EditConstantActivity.class));
            }
        });

        ListView listView = new ListView(mActivity);
        mAdapter = new MyAdapter(mActivity, R.layout.item_edit_constant, mNames);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, EditConstantActivity.class);
                SQLiteDatabase db = new CustomDefinitionDbHelper(mActivity).getReadableDatabase();
                String sql = "SELECT name, value, detail FROM constant " +
                        "WHERE name = \"" + mNames.get(position) + "\"";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.moveToFirst()) {
                    intent.putExtra("name", cursor.getString(cursor.getColumnIndex("name")));
                    intent.putExtra("value", cursor.getDouble(cursor.getColumnIndex("value")));
                    intent.putExtra("detail", cursor.getString(cursor.getColumnIndex("detail")));
                }
                cursor.close();
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("确认删除？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CustomDefinitionDbHelper(mActivity).removeConstant(mNames.get(position));
                        refresh();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                return true;
            }
        });
        return listView;
    }

    private void refresh() {
        SQLiteDatabase db = new CustomDefinitionDbHelper(mActivity).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, value, detail FROM constant", null);
        mNames.clear();
        mValues.clear();
        mDetails.clear();
        while (cursor.moveToNext()) {
            mNames.add(cursor.getString(cursor.getColumnIndex("name")));
            mValues.add(cursor.getDouble(cursor.getColumnIndex("value")));
            mDetails.add(cursor.getString(cursor.getColumnIndex("detail")));
        }
        cursor.close();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_edit_constant, parent, false);
            } else {
                v = convertView;
            }
            String nameAndValue = mNames.get(position) + " = " + mValues.get(position);
            ((TextView) v.findViewById(R.id.tvNameAndValue)).setText(nameAndValue);
            if (!mDetails.get(position).isEmpty()) {
                TextView tvDetail = ((TextView) v.findViewById(R.id.tvDetail));
                tvDetail.setVisibility(View.VISIBLE);
                tvDetail.setText(mDetails.get(position));
            } else {
                v.findViewById(R.id.tvDetail).setVisibility(View.GONE);
            }
            return v;
        }
    }
}
