package com.firebase.csm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

/**
 * Created by Lobster on 13.02.17.
 */
public class BackgroundSubscribeIntentService extends IntentService {

    public BackgroundSubscribeIntentService() {
        super("BackgroundSubscribeIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Nearby.Messages.handleIntent(intent, new MessageListener() {
                @Override
                public void onFound(Message message) {
                    Log.i("service", "found message = " + message.toString());
                }

                @Override
                public void onLost(Message message) {
                    Log.i("service", "lost message = " + message.toString());
                }
            });
        }
    }
}
