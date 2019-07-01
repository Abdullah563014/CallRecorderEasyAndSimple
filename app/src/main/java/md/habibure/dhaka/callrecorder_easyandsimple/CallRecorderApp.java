package md.habibure.dhaka.callrecorder_easyandsimple;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class CallRecorderApp extends Application {
    public static final String MY_NOTIFICATION_CHANNEL_ID="callRecorderEasyAndSimple";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT>=26){
            NotificationChannel notificationChannel=new NotificationChannel(
                    MY_NOTIFICATION_CHANNEL_ID
                    ,"Call Recorder Easy And Simple Channel"
                    , NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }
}
