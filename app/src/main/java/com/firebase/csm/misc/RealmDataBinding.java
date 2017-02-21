package com.firebase.csm.misc;

import io.realm.RealmChangeListener;

/**
 * Created by Lobster on 20.02.17.
 */

public interface RealmDataBinding {

    interface Factory {
        RealmChangeListener create();
    }

    RealmDataBinding.Factory FACTORY = () -> element -> {
        if(element instanceof RealmDataBinding) {
            ((RealmDataBinding)element).notifyChange();
        }
    };

    void notifyChange();
}
