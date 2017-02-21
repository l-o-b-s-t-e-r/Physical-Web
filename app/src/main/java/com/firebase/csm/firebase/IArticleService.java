package com.firebase.csm.firebase;

import com.google.firebase.database.ValueEventListener;

/**
 * Created by Lobster on 11.02.17.
 */

public interface IArticleService {

    void loadArticle(String title, ValueEventListener listener);

}
