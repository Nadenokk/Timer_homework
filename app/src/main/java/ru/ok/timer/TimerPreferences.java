package ru.ok.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;

public final class TimerPreferences {
    private static long  DEF_TIMER = MainActivity.DEF_TIMER;

    private TimerPreferences() {
        throw new AssertionError();
    }
    public static long getTimerCurrenMilliSec(@NonNull final Context context){
        long time = PreferenceManager.getDefaultSharedPreferences(context).getLong("TimerMilliSec",DEF_TIMER);
        if (time< 1000) time=DEF_TIMER;
        Log.d("TimerPreferences", "getTimerCurrenMilliSec: "+time);
        return  time;
    }
    public static void setTimerCurrenMilliSec(@NonNull final long time,@NonNull final Context context){
        Log.d("TimerPreferences", "setTimerCurrenMilliSec: "+time);
        final  Editor sPrefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sPrefEditor.putLong("TimerMilliSec",time);
        sPrefEditor.commit();
    }
    public static int getTimerMode(@NonNull final Context context){
        int mode = PreferenceManager.getDefaultSharedPreferences(context).getInt("TimerMode",0);
        Log.d("TimerPreferences", "getTimerMode: " + mode);
        return  mode;
    }
    public static void setTimerMode(@NonNull final int mode,@NonNull final Context context){
        Log.d("TimerPreferences", "setTimerMode "+mode);
        final  Editor sPrefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sPrefEditor.putInt("TimerMode",mode);
        sPrefEditor.commit();
    }
    public static int getTimerStatus(@NonNull final Context context){
        int mode = PreferenceManager.getDefaultSharedPreferences(context).getInt("TimerStatus",0);
        Log.d("TimerPreferences", "getTimerStatus: " + mode);
        return  mode;
    }
    public static void setTimerStatus(@NonNull final int mode,@NonNull final Context context) {
        Log.d("TimerPreferences", "setTimerStatus " + mode);
        final Editor sPrefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sPrefEditor.putInt("TimerStatus", mode);
        sPrefEditor.commit();
    }
}