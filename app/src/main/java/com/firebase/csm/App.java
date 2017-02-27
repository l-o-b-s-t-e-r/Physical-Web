package com.firebase.csm;

import android.support.multidex.MultiDexApplication;

import com.firebase.csm.di.components.AppComponent;
import com.firebase.csm.di.components.DaggerAppComponent;
import com.firebase.csm.di.modules.AppModule;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Lobster on 04.02.17.
 */

public class App extends MultiDexApplication {

    private static App instance;
    private static FirebaseAnalytics analytics;

    private AppComponent appComponent;

    public static App getInstance() {
        return instance;
    }

    public static FirebaseAnalytics getAnalytics() {
        return analytics;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build());

        Timber.plant(new Timber.DebugTree());

        JodaTimeAndroid.init(this);

        instance = this;

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build());

        analytics = FirebaseAnalytics.getInstance(this);
    }

    public AppComponent appComponent() {
        return appComponent;
    }

}
