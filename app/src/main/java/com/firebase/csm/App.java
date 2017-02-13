package com.firebase.csm;

import android.app.Application;

import com.firebase.csm.di.components.AppComponent;
import com.firebase.csm.di.components.DaggerAppComponent;
import com.firebase.csm.di.modules.AppModule;

import net.danlew.android.joda.JodaTimeAndroid;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Lobster on 04.02.17.
 */

public class App extends Application {

    private static App instance;

    private AppComponent appComponent;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        instance = this;

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public AppComponent appComponent() {
        return appComponent;
    }

}
