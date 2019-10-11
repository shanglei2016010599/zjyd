package com.example.zjyd.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class LongRunningService extends Service {

    private static final String TAG = "LongRunningService";

    /* 判断是否是第一次启动服务的标志，默认为true */
    private boolean isFirst = true;
    /* 存储点值的数组 */
    private int[] s;

    private OnAddCalculateListener onAddCalculateListener;
    /* 返回的接口 */
    public interface OnAddCalculateListener{
        void onAddResultCallback(int[] s);
    }
    /* 注册方法 */
    public void registererOnAddCalculateListener(OnAddCalculateListener onAddCalculateListener){
        this.onAddCalculateListener = onAddCalculateListener;
    }
    /* 返注册方法 */
    public void unregisterOnAddCalculateListener(){
        this.onAddCalculateListener = null;
    }

    public LongRunningService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }
    /* 创建MyBinder类继承Binder，实现绑定服务 */
    public class MyBinder extends Binder {
        public LongRunningService getService(){
            return LongRunningService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* 如果是第一次启动服务，通过intent得到数组s，之后不需要 */
        if (isFirst){
            s = intent.getIntArrayExtra("Data");
            isFirst = false;
        }

        Log.d(TAG, "onStartCommand: " + Arrays.toString(s));

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行具体的逻辑操作
                updateData(s);
            }
        }).start();

        /* 设置定时 */
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int interval = 10 * 1000;   // 10秒的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + interval;
        Intent i = new Intent(this, LongRunningService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }

        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * 更新数据，回调接口，把值回调
     * @param s 存储点值的数组
     */
    public void updateData(int[] s){
        if (onAddCalculateListener != null){
            Log.d(TAG, "updateData: " + System.currentTimeMillis());
            /* 将数组第一位去除，后面所有数组前移 */
            if (s.length - 1 >= 0) System.arraycopy(s, 1, s, 0, s.length - 1);
            /* 增加新的数据 */
            Random random = new Random();
            s[s.length - 1] = random.nextInt(47);
            /* 回调接口，将新的数组返回给活动 */
            onAddCalculateListener.onAddResultCallback(s);
        }
    }
}
