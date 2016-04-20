package com.example.multicalc.other;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 调对话框实现检查更新，更新情况文件被配置在我github博客网站的一个文件中
 */
public class DialogCheckUpdate {
    private Activity mActivity;
    private Dialog mDialog;
    private Thread mCheckThread;
    private boolean mSuccessful;

    private int mLatestVersionCode;
    private int mLocalVersionCode;
    private String mLatestVersionName;
    private String mLocalVersionName;
    private String mLatestVersionDate;
    private String mDownloadLink;
    private String mDescription;

    public DialogCheckUpdate(final Activity activity) {
        mActivity = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle("正在检查更新");
        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setPadding(30, 50, 30, 50);
        builder.setView(progressBar);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCheckThread != null) {
                    mCheckThread.interrupt();
                }
            }
        });
        mDialog = builder.show();

        mCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    PackageInfo info = mActivity.getPackageManager()
                            .getPackageInfo(mActivity.getPackageName(), 0);
                    mLocalVersionCode = info.versionCode;
                    mLocalVersionName = info.versionName;
                    URL url = new URL("https://AsyncCode.github.io/MultiCalc/Download/latest.txt");
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setConnectTimeout(20000);
                    connection.setReadTimeout(20000);
                    reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"));
                    mLatestVersionCode = Integer.parseInt(reader.readLine());
                    mLatestVersionName = reader.readLine();
                    mLatestVersionDate = reader.readLine();
                    mDownloadLink = reader.readLine();
                    StringBuilder sb = new StringBuilder();
                    while ((mDescription = reader.readLine()) != null) {
                        sb.append(mDescription);
                    }
                    mDescription = sb.toString();
                    mSuccessful = true;
                } catch (Exception e) {
                    mSuccessful = false;
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            mSuccessful = false;
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (!Thread.currentThread().isInterrupted()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkDone();
                            }
                        });
                    }
                }
            }
        });
        mCheckThread.start();
    }

    public void checkDone() {
        mDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        if (mSuccessful) {
            builder.setTitle("检查更新成功");
            if (mLocalVersionCode == mLatestVersionCode) {
                builder.setMessage("已经是最新版本");
            } else {
                LinearLayout container = new LinearLayout(mActivity);
                container.setOrientation(LinearLayout.VERTICAL);
                container.setPadding(30, 30, 30, 30);

                TextView tv = new TextView(mActivity);
                tv.setText("最新版本：".concat(mLatestVersionName));
                tv.setTextColor(Color.BLACK);
                container.addView(tv);

                tv = new TextView(mActivity);
                tv.setText("\n已安装版本：".concat(mLocalVersionName));
                tv.setTextColor(Color.BLACK);
                container.addView(tv);

                tv = new TextView(mActivity);
                tv.setText("\n更新日期：".concat(mLatestVersionDate));
                tv.setTextColor(Color.BLACK);
                container.addView(tv);

                tv = new TextView(mActivity);
                tv.setText("\n新版说明：\n".concat(mDescription));
                tv.setTextColor(Color.BLACK);
                container.addView(tv);
                builder.setView(container);

                builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse(mDownloadLink));
                        mActivity.startActivity(intent);
                    }
                });
            }
        } else {
            builder.setTitle("检查更新失败");
        }
        builder.setNegativeButton("退出", null);
        builder.setCancelable(false).show();
    }
}
