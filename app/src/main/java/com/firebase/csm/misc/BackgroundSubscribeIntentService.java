package com.firebase.csm.misc;

import android.app.IntentService;
import android.content.Intent;

import com.firebase.csm.App;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import timber.log.Timber;

/**
 * Created by Lobster on 13.02.17.
 */
public class BackgroundSubscribeIntentService extends IntentService {

    private NotificationHelper mNotificationHelper;

    public BackgroundSubscribeIntentService() {
        super("BackgroundSubscribeIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        mNotificationHelper = App.getInstance().appComponent().getNotificationHelper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationTest();
        if (intent != null) {
            Nearby.Messages.handleIntent(intent, new MessageListener() {
                @Override
                public void onFound(Message message) {
                    mNotificationHelper.buildNotification(message);
                }

                @Override
                public void onLost(Message message) {
                    mNotificationHelper.cancelNotification(message);
                }
            });
        }
    }

    private void notificationTest() {
        Timber.d("notificationTest");
        mNotificationHelper.buildNotification(null);
    }
}
