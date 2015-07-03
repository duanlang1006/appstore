package com.applite.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.applite.bean.ScreenBean;

import java.util.List;

/**
 * Created by LSY on 15-7-1.
 */
public class ScreenAdapter extends BaseAdapter {

    private List<ScreenBean> mList;
    private Context mContext;

    public ScreenAdapter(Context mContext, List<ScreenBean> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
