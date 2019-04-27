package ru.ok.timer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.os.CountDownTimer;
import android.os.Looper;

public class ServicesCountDown extends IntentService{
    final String LOG_TAG = "ServicesCountDown";
    private long timerCurrenMilliSec;
    private CountDownTimer countDownTimer;

    Intent tickIntent = new Intent(MainActivity.BROADCAST_TIME);
    Intent modeIntent = new Intent(MainActivity.BROADCAST_MODE);

    public ServicesCountDown() {
        super("ServicesCountDown");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG,"onCreate()");
        super.onCreate();
        Intent tickIntent = new Intent(MainActivity.BROADCAST_MODE);
        tickIntent.putExtra("TimerMode", true);
        sendBroadcast(modeIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setMode(1);
        timerCurrenMilliSec = TimerPreferences.getTimerCurrenMilliSec(this);
        countDownTimer = new CountDownTimer(timerCurrenMilliSec, 10) {
            //Intent tickIntent = new Intent(MainActivity.BROADCAST_TIME);
            @Override
            public void onTick(long millisUntilFinished) {
                tickIntent.putExtra("milliSec", millisUntilFinished);
                sendBroadcast(tickIntent);
                timerCurrenMilliSec = millisUntilFinished;
            }
            @Override
            public void onFinish() {
                Log.d("TIMER", "onFinish()");
                tickIntent.putExtra("milliSec", -1L);
                modeIntent.putExtra("TimerMode", false);
                sendBroadcast(tickIntent);
                sendBroadcast(modeIntent);
                setDefaultTime();
                setMode(0);
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        TimerPreferences.setTimerMode(0, this);
        TimerPreferences.setTimerCurrenMilliSec(timerCurrenMilliSec, this);
        tickIntent.putExtra("milliSec", timerCurrenMilliSec);
        modeIntent.putExtra("TimerMode", false);
        sendBroadcast(tickIntent);
        sendBroadcast(modeIntent);
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    public void setDefaultTime() {
        TimerPreferences.setTimerCurrenMilliSec(MainActivity.DEF_TIMER, this);
    }

    public void setMode(int mode) {
        TimerPreferences.setTimerMode(mode, this);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Looper.loop();
    }
}