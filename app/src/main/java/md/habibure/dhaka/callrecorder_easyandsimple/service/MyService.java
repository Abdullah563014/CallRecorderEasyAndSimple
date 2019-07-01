package md.habibure.dhaka.callrecorder_easyandsimple.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import md.habibure.dhaka.callrecorder_easyandsimple.Constants;
import md.habibure.dhaka.callrecorder_easyandsimple.HomeActivity;
import md.habibure.dhaka.callrecorder_easyandsimple.R;
import md.habibure.dhaka.callrecorder_easyandsimple.database.Database;
import md.habibure.dhaka.callrecorder_easyandsimple.model.CallListModelClass;

import static md.habibure.dhaka.callrecorder_easyandsimple.CallRecorderApp.MY_NOTIFICATION_CHANNEL_ID;

public class MyService extends Service {

    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private boolean serviceStarted = false;
    private boolean recordStart;
    private boolean serviceStart;
    private boolean hasException = false;
    private File file;
    private Database database;
    private String filePath;
    private String callIndicator;
    private String phoneNumber;
    CharSequence userDate;
    CharSequence userMonth;
    CharSequence userYear;
    CharSequence userTime;
    CharSequence fileTime;
    private MediaMetadataRetriever mmr;
    private Uri uri;
    private String duration;
    private String contactName = null;
    private TelephonyManager manager;


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {
            recordStart = intent.getBooleanExtra("RecordStart", false);
            serviceStart = intent.getBooleanExtra("ServiceStart", false);
            if (serviceStart) {
                phoneNumber = intent.getStringExtra("PhoneNumber");
                callIndicator = intent.getStringExtra("CallIndicator");
            }
        }

        if (serviceStart && phoneNumber != null && !serviceStarted) {
            startService();
        } else if (!serviceStart && serviceStarted) {
            stopService();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    public void startService() {
        if (serviceStart && !serviceStarted && callIndicator != null && phoneNumber != null) {
            if (!Constants.onForground) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getBaseContext(), 0, intent, 0);
                Notification notification;
                if (Build.VERSION.SDK_INT >= 26) {
                    notification = new NotificationCompat.Builder(
                            getBaseContext(), MY_NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("Call Recorder Easy & Simple")
                            .setTicker("hi")
                            .setContentText("Click to see about new recorded file")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent).setOngoing(true)
                            .build();
                } else {
                    notification = new NotificationCompat.Builder(
                            getBaseContext())
                            .setContentTitle("Call Recorder Easy & Simple")
                            .setTicker("hi")
                            .setContentText("Click to see about new recorded file")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent).setOngoing(true)
                            .getNotification();
                }

                notification.flags = Notification.FLAG_NO_CLEAR;

                startForeground(1337, notification);
            }
            startRecording();
            Constants.onForground = true;
            serviceStarted = true;
            recordStarted = true;
            Constants.onForground = true;
        }
    }

    public void stopService() {
        if (!serviceStart && serviceStarted) {
            if (recordStarted) {
                stopRecording(phoneNumber);
                serviceStarted = false;
                recordStarted = false;
                phoneNumber = null;
                callIndicator = null;
            }
            showNotification();
            Constants.onForground = false;
            this.stopSelf();
        }
    }


    public Database getDatabaseInstance() {
        if (database == null) {
            database = new Database(getApplicationContext());
        }
        return database;
    }

    public MediaMetadataRetriever getMmrInstance() {
        if (mmr == null) {
            mmr = new MediaMetadataRetriever();
        }
        return mmr;
    }


    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        if (contactName == null) {
            contactName = phoneNumber;
        }
        return contactName;
    }


    public void startRecording() {
        if (recordStart && !recordStarted) {
            Toast.makeText(this, "Record Starting", Toast.LENGTH_SHORT).show();
            file = new File(Environment.getExternalStorageDirectory(), ".EasyAndSimple");
            final Date date = new Date();
            userDate = DateFormat.format("dd", date.getTime());
            userMonth = DateFormat.format("MM", date.getTime());
            userYear = DateFormat.format("yyyy", date.getTime());
            userTime = DateFormat.format("hh.mm", date.getTime());
            fileTime = DateFormat.format("hh.mm.ss", date.getTime());


            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                if (!file.exists()) {
                    file.mkdirs();
                }
                filePath = file.getAbsolutePath() + "/" + userDate + "-" + userMonth + "-" + userYear + "-" + fileTime + "rec.3gp";
                recorder.setOutputFile(filePath);
                recorder.prepare();
                Thread.sleep(2000);
                recorder.start();
            } catch (Exception e) {
                hasException = true;
            }
            recordStarted = true;
        }
    }


    public void stopRecording(String phoneNumber) {
        if (recorder != null && recordStarted) {
            Toast.makeText(this, "Record Stopping", Toast.LENGTH_SHORT).show();
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recordStarted = false;
                getDatabaseInstance();
                getMmrInstance();
                uri = Uri.parse(filePath);
                mmr.setDataSource(getApplicationContext(), uri);
                duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                contactName = getContactName(phoneNumber, getApplicationContext());
            } catch (Exception e) {
                hasException = true;
            }
            if (database != null && !hasException) {
                database.initializedDatabase();
                database.insertData(String.valueOf(userDate), String.valueOf(userMonth), String.valueOf(userYear), new CallListModelClass(callIndicator, duration, contactName, String.valueOf(userTime), filePath));
                database.closeDatabase();
            }
            database = null;
            mmr = null;
            recorder = null;
        }
    }


    public void showNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, MY_NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("New Recording")
                            .setContentText("A phone Call has been Recorded");
            Intent resultIntent = new Intent(this, HomeActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("New Recording")
                            .setContentText("A phone Call has been Recorded");
            Intent resultIntent = new Intent(this, HomeActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }

    }

}
