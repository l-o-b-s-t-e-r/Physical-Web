package com.firebase.csm.adapters;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Created by Lobster on 06.02.17.
 */

public class BindingAdapters {

    @BindingAdapter({"articleImage"})
    public static void loadArticleImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(view);
    }

}
