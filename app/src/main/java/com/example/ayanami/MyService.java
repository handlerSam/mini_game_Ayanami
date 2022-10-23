package com.example.ayanami;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    //public static SoundPool soundPool;
    public MediaPlayer mediaPlayer;
    public int tempSoundId = -1;
    public int tempRnd = -1;
    public static final int NOTICE_ID = 100;

    public Runnable songEnd = new Runnable() {
        @Override
        public void run() {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.singButton,"歌う");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyService.this);
            ComponentName watchWidget = new ComponentName(MyService.this, WidgetProvider.class);
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Sam","ServiceCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(NOTICE_ID,getNotification());
            //Intent intent = new Intent(this, CancelNoticeService.class);
            //startService(intent);
        } else {
            startForeground(NOTICE_ID, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            final int number = intent.getIntExtra("voice",-1);
            tempRnd = intent.getIntExtra("random",-1);
            int soundId = -1;
            if(number == 4){
                soundId = getResource("song");
            }else{
                soundId = getResource("v"+(tempRnd + 1));
            }
            if((mediaPlayer != null && mediaPlayer.isPlaying())) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if(number != 5){
                mediaPlayer = MediaPlayer.create(getApplicationContext(),soundId);
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        if(number == 4){
                            SharedPreferences.Editor editor = getSharedPreferences("info",MODE_PRIVATE).edit();
                            editor.putInt("stage",9);
                            editor.apply();
                        }
                        Log.d("Sam","mediaPlay");
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mediaPlayer = null;
                        if(number == 4){
                            SharedPreferences.Editor editor = getSharedPreferences("info",MODE_PRIVATE).edit();
                            editor.putInt("song",-1);
                            editor.putInt("stage",-1);
                            WidgetProvider.mHandler.post(songEnd);
                            editor.apply();
                        }
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Sam","ServiceDestroy");
        /*if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }*/
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getResource(String idName){
        int resId = getResources().getIdentifier(idName, "raw", getPackageName());
        return resId;
    }

    private Notification getNotification(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String id = "Sam";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id,"my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(mChannel);//在NotificationManager中注册渠道通知对象
        }
        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setContentTitle("Ayanami")
                .setContentText("Ayanami is running.")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setChannelId(id);
        return notificationBuilder.build();
    }
}

