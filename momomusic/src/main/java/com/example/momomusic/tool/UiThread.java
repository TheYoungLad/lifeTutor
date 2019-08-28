package com.example.momomusic.tool;

import android.os.Handler;
import android.os.Looper;


/**
 * UI线程
 */
public class UiThread {


    /**
     * 使用handler回调到主线程中
     *
     * @return handler
     */
    public static Handler getUiThread() {
        Handler handler = new Handler(Looper.getMainLooper());
        return handler;
    }

}
