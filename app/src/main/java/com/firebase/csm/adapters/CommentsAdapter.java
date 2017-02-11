package com.firebase.csm.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.csm.models.Comment;
import com.firebase.csm.models.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Lobster on 06.02.17.
 */

public class CommentsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<Item>  mItems = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int layoutId) {
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false));
    }

    public void add(Item item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void addComments(List<Comment> comments) {
        mItems.addAll(comments);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getLayoutId();
    }

    public void clear() {
        mItems.clear();
    }
}
