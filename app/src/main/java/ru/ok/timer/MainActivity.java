package ru.ok.timer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.app.NotificationCompat;
import java.util.Locale;
import java.lang.String;


public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "MainActivity";
    private EditText tabloTextView;
    private Button buttonStartStop;
    private Button buttonReset;
    public static final long DEF_TIMER = 60000;
    public long timerCurrenMilliSec;
    private long timeCurren;
    private int mode;

    final static String BROADCAST_TIME = "MyTimeBroadcast";
    final static String BROADCAST_MODE = "MyModeBroadcast";

    BroadcastReceiver br;
    BroadcastReceiver brStatus;
    Intent timerServer;

    private PendingIntent resultPendingIntent;
    private PendingIntent resultStopIntent;
    private PendingIntent resultStartIntent;
    private PendingIntent resultResetIntent;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabloTextView = findViewById(R.id.timer);
        buttonStartStop = findViewById(R.id.btn_start_stop);
        buttonReset = findViewById(R.id.btn_reset);

        timerCurrenMilliSec = TimerPreferences.getTimerCurrenMilliSec(this);
        tabloTextView.setText(timerFormatWithMilliSec(timerCurrenMilliSec));
        timeCurren=timerCurrenMilliSec;

        mode = TimerPreferences.getTimerMode(this);

        timerServer = new Intent(MainActivity.this, ServicesCountDown.class);
        stopService(timerServer);
        if (mode == 1) {
            startService(timerServer);
            initButton(1);
        }

        notificationManager = createNotificationChannel("Timer_Id","Timer","Timer");

        Log.d(LOG_TAG, "Init !!! MyTimer: " + timerCurrenMilliSec +" Mode: "+ mode);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                timeCurren = intent.getLongExtra("milliSec",0L);
                if (timeCurren < 0){ timeCurren=0; }
                tabloTextView.setText(timerFormatWithMilliSec(timeCurren));
                createTimerNotification(notificationManager,"Timer_Id",true,timerFormatWithMilliSec(timeCurren));
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_TIME);
        registerReceiver(br, intFilt);

        brStatus = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean mode = intent.getBooleanExtra("TimerMode",true);
                if (mode){
                    Log.d(LOG_TAG, " timer.Start" + timeCurren +" Mode: "+ mode);
                    createTimerNotification(notificationManager,"Timer_Id",mode,timerFormatWithMilliSec(timeCurren));
                    initButton(1);
                } else {
                    Log.d(LOG_TAG, " timer.Stop" + timeCurren +" Mode: "+ mode);
                    createTimerNotification(notificationManager,"Timer_Id",mode,timerFormatWithMilliSec(timeCurren));
                    initButton(0);
                }
            }
        };
        IntentFilter intFiltStatus = new IntentFilter(BROADCAST_MODE);
        registerReceiver(brStatus, intFiltStatus);

        Intent resultIntent = new Intent(this, MainActivity.class);
        Intent intent = getIntent();
        String action = intent.getAction();

        if (action.equals("timer.Stop")) {
            Log.d(LOG_TAG, "timer.Stop: " + timeCurren +" Mode: "+ mode);
            timerStopStart();
        }
        else if (action.equals("timer.Start")) {
            Log.d(LOG_TAG, " timer.Start" + timeCurren +" Mode: "+ mode);
            timerStopStart();
        }else {;}

        resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        resultStopIntent = PendingIntent.getActivity(this, 0, intent.setAction("timer.Stop"), PendingIntent.FLAG_UPDATE_CURRENT);
        resultStartIntent = PendingIntent.getActivity(this, 0, intent.setAction("timer.Start"), PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart " + timeCurren);
        initButton(TimerPreferences.getTimerMode(this));
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume ");
        super.onResume();
    }
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause ");
        TimerPreferences.setTimerCurrenMilliSec(timeCurren,this);
        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop ");
        TimerPreferences.setTimerCurrenMilliSec(timeCurren,this);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy ");
        TimerPreferences.setTimerCurrenMilliSec(timeCurren,this);
        notificationManager.cancelAll();
        unregisterReceiver(br);
        unregisterReceiver(brStatus);
        super.onDestroy();
    }

    public void buttonStartStop(View view) {
        Log.d(LOG_TAG, "Click buttonStartStop");
        timerStopStart();
    }

    private void timerStopStart(){

        mode = TimerPreferences.getTimerMode(this);
        if (mode == 0) {
            timerCurrenMilliSec = timerFormatWithString(tabloTextView.getText().toString());
            TimerPreferences.setTimerCurrenMilliSec(timerCurrenMilliSec,this);
            startService(timerServer);
            initButton(1);
        } else if (mode == 1) {
            stopService(timerServer);
            initButton(0);
        }
    }

    public void buttonReset(View view) {
        Log.d(LOG_TAG, "Click buttonReset");
        timerReset();
    }

    private void timerReset(){
        mode = 0;
        timerCurrenMilliSec = DEF_TIMER;
        TimerPreferences.setTimerCurrenMilliSec(DEF_TIMER,this);
        buttonReset.setEnabled(true);
        buttonStartStop.setText("start");
        tabloTextView.setText(timerFormatWithMilliSec(TimerPreferences.getTimerCurrenMilliSec(this)));
    }

    private void initButton(int status){
        if (status == 1){
            buttonReset.setEnabled(false);
            buttonStartStop.setText("stop");
           // createTimerNotification(notificationManager,"Timer_Id",true,timerFormatWithMilliSec(timeCurren));
        }else {
            buttonReset.setEnabled(true);
            buttonStartStop.setText("start");
           // createTimerNotification(notificationManager,"Timer_Id",false,timerFormatWithMilliSec(timeCurren));
        }
    }

    private void createTimerNotification(NotificationManager notifManager,String id, boolean mode,String time) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(mode ? "Timer is't Run" : "Timer is Run")
                        .setContentText(time.substring(0, time.length() - 4))
                        .addAction(R.mipmap.ic_launcher, mode ? "Stop" : "Start", mode ? resultStartIntent : resultStopIntent)
                        .addAction(R.mipmap.ic_launcher, mode ? "Reset" : "Reset",mode ? null: null)
                        .setContentIntent(resultPendingIntent)
                ;
        notifManager.notify(1, builder.build());
    }

    private NotificationManager createNotificationChannel(String id,CharSequence name,String description) {
        NotificationManager  notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainActivity", "createNotificationChannel: "+id);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
            notificationChannel.setDescription(description);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return notificationManager;
    }

    public static String timerFormatWithMilliSec(long timeCurren) {
        String string= String.format(Locale.getDefault(), "%02d:%02d:%03d",
                (int) (timeCurren / 1000 / 60),
                (int) ((timeCurren / 1000) % 60),
                (int) (timeCurren % 1000));
        // Log.d("MainActivity", "timerFormatWithMilliSec: "+string+" "+timeCurren);
        return string;
    }

    public static long timerFormatWithString(String string){
        String[] buff = string.split(":");
        long timer = Long.parseLong(buff[0].toString(),10) * 60000L +
                Long.parseLong(buff[1].toString(),10) * 1000L +
                Long.parseLong(buff[2].toString(),10);
        //Log.d("MainActivity", "timerFormatWithString: "+string+" "+timer+" "+buff[0]+" "+buff[1]+" "+buff[2]);
        return timer;
    }

}
