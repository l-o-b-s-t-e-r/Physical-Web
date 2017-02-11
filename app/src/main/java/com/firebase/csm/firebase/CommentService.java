package com.firebase.csm.firebase;

import com.firebase.csm.models.Comment;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

/**
 * Created by Lobster on 07.02.17.
 */

public class CommentService implements ICommentService{

    public static final Integer OFFSET = 6;

    private final String ID_KEY = "articleId";

    private DatabaseReference mCommentReference;

    public CommentService(String key) {
        mCommentReference = FirebaseDatabase.getInstance().getReference(key);
    }

    @Override
    public void loadComments(Long articleId, Integer alreadyLoadedCommentsNumber, ValueEventListener listener) {
        mCommentReference.orderByChild(ID_KEY)
                .equalTo(articleId)
                .limitToLast(alreadyLoadedCommentsNumber + OFFSET)
                .addListenerForSingleValueEvent(listener);
    }

    @Override
    public void addComment(Comment comment, DatabaseReference.CompletionListener listener) {
        mCommentReference.push().setValue(comment, listener);
    }
}
