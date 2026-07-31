package com.mysystem.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.widget.Toast;
import java.util.HashMap;

public class SmsReceiver extends BroadcastReceiver {

    private static final HashMap<String, Integer> smsTracker = new HashMap<>();
    private static long lastResetTime = System.currentTimeMillis();
    private static final int BOMB_THRESHOLD = 3; // Порог сообщений для определения бомбера

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = sms.getOriginatingAddress();

                        trackAndRespond(context, sender);
                    }
                }
            }
        }
    }

    private void trackAndRespond(Context context, String sender) {
        long currentTime = System.currentTimeMillis();
        // Сброс счетчика каждые 10 секунд
        if (currentTime - lastResetTime > 10000) {
            smsTracker.clear();
            lastResetTime = currentTime;
        }

        int count = smsTracker.getOrDefault(sender, 0) + 1;
        smsTracker.put(sender, count);

        if (count >= BOMB_THRESHOLD) {
            Toast.makeText(context, "Онаружена СМС-атака от: " + sender, Toast.LENGTH_LONG).show();
            sendAutoReply(sender, "Система защиты MySystemApp: атака заблокирована.");
        }
    }

    private void sendAutoReply(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
