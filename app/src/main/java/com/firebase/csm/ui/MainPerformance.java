package com.firebase.csm.ui;

import android.net.Uri;

import com.firebase.csm.models.Article;
import com.firebase.csm.models.Comment;

import java.util.List;

/**
 * Created by Lobster on 04.02.17.
 */

public interface MainPerformance {

    interface Actions {

        void loadArticle(String title);

        void loadComments(Long id, Integer alreadyLoadedCommentsNumber);

        void addComment(Comment comment);

    }

    interface View {

        void showArticle(Article article);

        void showComments(List<Comment> comments);

        void showNewComment(Comment comment);

        void prepareAudio(Uri audioUri);

        void showError(String errorMessage);
    }

}
