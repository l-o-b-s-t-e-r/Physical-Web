package com.firebase.csm.di.modules;

import com.firebase.csm.App;
import com.firebase.csm.custom.AnimationHelper;
import com.firebase.csm.firebase.ArticleService;
import com.firebase.csm.firebase.CommentService;
import com.firebase.csm.firebase.IArticleService;
import com.firebase.csm.firebase.ICommentService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Lobster on 04.02.17.
 */

@Module
public class AppModule {

    private final String ARTICLES_KEY = "articles";
    private final String COMMENTS_KEY = "comments";

    private final App mApplication;

    public AppModule(App application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    App provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    AnimationHelper provideAnimationHelper() {
        return new AnimationHelper(mApplication);
    }

    @Provides
    @Singleton
    IArticleService provideArticleReference() {
        return new ArticleService(ARTICLES_KEY);
    }

    @Provides
    @Singleton
    ICommentService provideCommentReference() {
        return new CommentService(COMMENTS_KEY);
    }
}

