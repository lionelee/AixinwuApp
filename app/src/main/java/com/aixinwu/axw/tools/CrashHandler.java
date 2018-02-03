package com.aixinwu.axw.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.aixinwu.axw.activity.WelcomeActivity;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lionel on 2018/1/31.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler crashHandler = new CrashHandler();

    private Context mContext;
    /** 错误日志文件 */
    private File logFile;

    private CrashHandler() {}

    public static CrashHandler getInstance() {
        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }
        return crashHandler;
    }

    public void init(Context context) {
        mContext = context;
        logFile = new File(mContext.getExternalCacheDir(),"crashLog.trace");
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置为线程默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 打印异常信息
        ex.printStackTrace();
        // 我们没有处理异常 并且默认异常处理不为空 则交给系统处理
        if (!handlelException(ex) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // 上传错误日志到服务器
                upLoadErrorFileToServer(logFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(mContext, WelcomeActivity.class);
            // 新开任务栈
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            // 杀死我们的进程
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Process.killProcess(Process.myPid());
                }
            }, 1000);

        }
    }

    private boolean handlelException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序发生异常，即将重启", Toast.LENGTH_LONG)
                        .show();
                Looper.loop();
            }
        }.start();
        PrintWriter pw = null;
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            pw = new PrintWriter(logFile);
            // 收集手机及错误信息
            logFile = collectInfoToSDCard(pw, ex);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 上传错误日志到服务器
     *
     * @param errorFile
     */
    private void upLoadErrorFileToServer(File errorFile) {

    }

    /**
     * 收集手机信息
     *
     * @throws NameNotFoundException
     */
    private File collectInfoToSDCard(PrintWriter pw, Throwable ex)
            throws NameNotFoundException {

        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        // 错误发生时间
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        pw.print("time : ");
        pw.println(time);
        // 版本信息
        pw.print("versionCode : ");
        pw.println(pi.versionCode);
        // 应用版本号
        pw.print("versionName : ");
        pw.println(pi.versionName);
        try {
            /** 暴力反射获取数据 */
            Field[] Fields = Build.class.getDeclaredFields();
            for (Field field : Fields) {
                field.setAccessible(true);
                pw.print(field.getName() + " : ");
                pw.println(field.get(null).toString());
            }
        } catch (Exception e) {
            Log.i(TAG, "an error occured when collect crash info" + e);
        }

        // 打印堆栈信息
        ex.printStackTrace(pw);
        return logFile;
    }
}
