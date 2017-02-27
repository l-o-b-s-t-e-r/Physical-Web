package com.firebase.csm.ui.main;

import com.firebase.csm.firebase.IArticleService;
import com.firebase.csm.firebase.ICommentService;
import com.firebase.csm.models.Article;
import com.firebase.csm.models.Comment;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Lobster on 04.02.17.
 */

public class MainPresenter implements MainPerformance.Actions {

    private MainPerformance.View mView;
    private Realm mRealm;
    private IArticleService mArticleService;
    private ICommentService mCommentService;

    public MainPresenter(MainPerformance.View view, IArticleService articleService, ICommentService commentService, Realm realm) {
        mView = view;
        mArticleService = articleService;
        mCommentService = commentService;
        mRealm = realm;
    }

    @Override
    public void loadComments(Long id, Integer alreadyLoadedCommentsNumber) {
        mCommentService.loadComments(id, alreadyLoadedCommentsNumber, loadCommentsByIdListener);
    }

    @Override
    public void addComment(Comment comment) {
        mCommentService.addComment(comment, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                mView.showNewComment(comment);
            }
        });
    }

    @Override
    public void loadArticle(String title) {
        mArticleService.loadArticle(title, loadArticleByIdListener);
    }

    private ValueEventListener loadArticleByIdListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot ds = Iterables.getFirst(dataSnapshot.getChildren(), null);
            if (ds != null) {
                Article article = ds.getValue(Article.class);
                mView.showArticle(article);
                mView.prepareAudio(article.getAudioUri());

                mCommentService.loadComments(article.getId(), 0, loadCommentsByIdListener);
                Article.saveOrUpdate(article, mRealm);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mView.showError("Request is cancelled");
        }
    };

    private ValueEventListener loadCommentsByIdListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Comment> comments = new ArrayList<>();

            for (DataSnapshot child: dataSnapshot.getChildren()) {
                comments.add(child.getValue(Comment.class));
            }

            mView.showComments(comments);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mView.showError("Request is cancelled");
        }
    };
}
