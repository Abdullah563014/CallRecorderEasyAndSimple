package md.habibure.dhaka.callrecorder_easyandsimple.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import md.habibure.dhaka.callrecorder_easyandsimple.Constants;
import md.habibure.dhaka.callrecorder_easyandsimple.service.MyService;

public class MyReceiver extends BroadcastReceiver {

    TelephonyManager telephonyManager;
    PhoneStateListener listener;
    private String phoneNumber;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
            telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    switch (state) {

                        case TelephonyManager.CALL_STATE_IDLE:
                            if (Constants.isRecordStarted) {
                                stopMyService(context);
                                resetData();
                            }
                            Constants.callIndicator = "outgoing";
                            break;

                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            if (!Constants.isRecordStarted && phoneNumber != null) {
                                startMyService(context);
                            }
                            break;


                        case TelephonyManager.CALL_STATE_RINGING:
                            Constants.callIndicator = "incoming";
                            break;
                    }
                }
            };
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void resetData() {
        Constants.isRecordStarted = false;
        Constants.callIndicator = "outgoing";
        phoneNumber=null;
    }

    private void startMyService(Context context) {
        if (!Constants.isRecordStarted && Constants.callIndicator != null && phoneNumber != null) {
            Intent intent = new Intent(context, MyService.class);
            intent.putExtra("RecordStart", true);
            intent.putExtra("ServiceStart", true);
            intent.putExtra("PhoneNumber", phoneNumber);
            intent.putExtra("CallIndicator", Constants.callIndicator);
            ContextCompat.startForegroundService(context,intent);
            Constants.isRecordStarted = true;
        }
    }


    private void stopMyService(Context context) {
        if (Constants.isRecordStarted) {
            Intent intent = new Intent(context, MyService.class);
            intent.putExtra("ServiceStart", false);
            ContextCompat.startForegroundService(context,intent);
            Constants.isRecordStarted = false;
        }
    }
}
