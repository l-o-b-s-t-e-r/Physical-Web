package com.firebase.csm.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

/**
 * Created by Lobster on 06.02.17.
 */

public class ArticleService implements IArticleService {

    private final String ID_KEY = "id";

    private DatabaseReference mArticleReference;

    public ArticleService(String key) {
        mArticleReference = FirebaseDatabase.getInstance().getReference(key);
    }

    @Override
    public void loadArticleById(Long id, ValueEventListener listener) {
        mArticleReference.orderByChild(ID_KEY)
                .equalTo(id)
                .addListenerForSingleValueEvent(listener);
    }

}
