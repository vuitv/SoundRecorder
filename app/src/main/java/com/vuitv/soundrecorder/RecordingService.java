package com.vuitv.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vuitv on 10/29/2018.
 */

public class RecordingService extends Service {
    private static final String LOG_TAG = "RecordingService";

    private String fileName = null;
    private String filePath = null;

    private MediaRecorder recorder = null;
    private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (recorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    public void startRecording() {
        setFileName();

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);

        if (MySharedPreference.getPrefHightQuality(this)) {
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(192000);
        }
        try {
            recorder.prepare();
            recorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopRecording() {
        recorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        recorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + filePath, Toast.LENGTH_LONG).show();

        //Loại bỏ thông báo
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
        recorder = null;
        try {
            mDatabase.addRecording(fileName, filePath, mElapsedMillis);

        } catch (Exception e) {
            Log.e(LOG_TAG, "addRecording exception: ", e);
        }
    }

    public void setFileName() {
        int count = 0;
        File file;
        do {
            count++;
            fileName = getString(R.string.default_file_name) + "_" + (mDatabase.getCount() + count) + ".mp4";
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            filePath += "/SoundRecorder/" + fileName;

            file = new File(filePath);
        } while (file.exists() && !file.isDirectory());
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_record_white)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }


}
