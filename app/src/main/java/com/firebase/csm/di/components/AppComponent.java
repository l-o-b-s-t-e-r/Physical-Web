package com.firebase.csm.di.components;

import com.firebase.csm.ui.MainActivity;
import com.firebase.csm.di.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Lobster on 04.02.17.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

}
