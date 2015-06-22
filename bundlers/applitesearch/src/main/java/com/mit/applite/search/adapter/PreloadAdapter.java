package com.mit.applite.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mit.applite.search.R;
import com.mit.applite.search.main.BundleContextFactory;

import java.util.List;

/**
 * Created by LSY on 15-6-11.
 */
public class PreloadAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<String> mPreloadData;

    public PreloadAdapter(Context context, List<String> data) {
        mPreloadData = data;
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            this.context = mContext;
            mInflater = LayoutInflater.from(mContext);
            mInflater = mInflater.cloneInContext(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            mInflater = LayoutInflater.from(context);
            this.context = context;
        }
    }

    @Override
    public int getCount() {
        return mPreloadData.size();
    }

    @Override
    public Object getItem(int position) {
        return mPreloadData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        /* 将convertView封装在ViewHodler中，减少系统内存占用 */
        if (convertView == null) {
            /* convertView为空则初始化 */
            convertView = mInflater.inflate(R.layout.item_preload_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            // 不为空则直接使用已有的封装类
            viewholder = (ViewHolder) convertView.getTag();
        }
        viewholder.mName.setText(mPreloadData.get(position));
        return convertView;
    }

    class ViewHolder {
        TextView mName;

        public ViewHolder(View view) {
            this.mName = (TextView) view.findViewById(R.id.item_preload_listview_name);
        }
    }
}
