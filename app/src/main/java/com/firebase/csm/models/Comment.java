package com.firebase.csm.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;
import com.firebase.csm.R;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.joda.time.DateTime;

/**
 * Created by Lobster on 06.02.17.
 */

@IgnoreExtraProperties
public class Comment extends BaseObservable implements Item {

    private Long articleId;

    private String comment;

    private Long time;

    @Override
    @Exclude
    public int getLayoutId() {
        return R.layout.comment;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    @Bindable
    public String getComment() {
        return comment;

    }

    public void setComment(String comment) {
        this.comment = comment;
        notifyPropertyChanged(BR.comment);
    }

    @Bindable
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
        notifyPropertyChanged(BR.time);
    }

    @Exclude
    public String getFormattedTime() {
        DateTime dateTime = new DateTime(time);
        return dateTime.dayOfMonth().getAsString() + " " + dateTime.monthOfYear().getAsShortText() + " " + dateTime.year().getAsText();
    }
}
