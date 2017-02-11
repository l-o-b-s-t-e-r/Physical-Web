package com.firebase.csm.firebase;

import com.firebase.csm.models.Comment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Lobster on 11.02.17.
 */

public interface ICommentService {

    void loadComments(Long articleId, Integer alreadyLoadedCommentsNumber, ValueEventListener listener);

    void addComment(Comment comment, DatabaseReference.CompletionListener listener);

}
