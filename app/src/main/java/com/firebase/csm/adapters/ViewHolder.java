package com.firebase.csm.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.databinding.tool.DataBinder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.databinding.library.baseAdapters.BR;

/**
 * Created by Lobster on 06.02.17.
 */

public class ViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding mViewDataBinding;

    public ViewHolder(ViewDataBinding viewDataBinding) {
        super(viewDataBinding.getRoot());
        mViewDataBinding = viewDataBinding;
    }

    public void bindTo(Object data) {
        mViewDataBinding.setVariable(BR.data, data);
    }

}
