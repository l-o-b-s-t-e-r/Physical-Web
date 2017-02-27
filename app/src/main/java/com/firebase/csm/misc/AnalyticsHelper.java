package com.firebase.csm.misc;

import android.os.Bundle;

import com.firebase.csm.App;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Lobster on 24.02.17.
 */

public class AnalyticsHelper {

    public static class Event {
        public static final String START_PLAY_AUDIO = "start_play_audio";
        public static final String FINISH_PLAY_AUDIO = "finish_play_audio";
        public static final String CLICK_NOTIFICATION = "click_notification";
    }

    public static void userStartPlayAudio(String artifactName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, artifactName);
        App.getAnalytics().logEvent(Event.START_PLAY_AUDIO, bundle);
    }

    public static void userFinishPlayAudio(String artifactName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, artifactName);
        App.getAnalytics().logEvent(Event.FINISH_PLAY_AUDIO, bundle);
    }

    public static void userClickNotification(String notificationName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, notificationName);
        App.getAnalytics().logEvent(Event.CLICK_NOTIFICATION, bundle);
    }

}
