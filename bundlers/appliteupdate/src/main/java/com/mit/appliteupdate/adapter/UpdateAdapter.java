package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.mit.appliteupdate.main.BundleContextFactory;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateAdapter extends BaseAdapter {

    private Context mActivity;
    private FinalBitmap mFinalBitmap;
    private Context mContext;
    private List<DataBean> mDatas;
    private LayoutInflater mInflater;

    public UpdateAdapter(Context context, List<DataBean> mDatas) {
        this.mDatas = mDatas;
        mFinalBitmap = FinalBitmap.create(context);
        mActivity = context;
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            this.mContext = mContext;
            mInflater = LayoutInflater.from(mContext);
            mInflater = mInflater.cloneInContext(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            mInflater = LayoutInflater.from(context);
            this.mContext = context;
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
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
            convertView = mInflater.inflate(R.layout.item_update_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            // 不为空则直接使用已有的封装类
            viewholder = (ViewHolder) convertView.getTag();
        }
        final DataBean data = mDatas.get(position);
        viewholder.mName.setText(data.getmName());
        mFinalBitmap.display(viewholder.mImg, data.getmImgUrl());
        viewholder.mVersionName.setText("V "+data.getmVersionName());
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(data.getmSize()));

        ImplInfo implInfo = data.getImplInfo();
        if (null == implInfo){
            ImplAgent.queryDownload(mContext,data.getmPackageName());
            implInfo = ImplInfo.create(mContext,data.getmPackageName(),data.getmUrl(),
                    data.getmPackageName(),data.getmVersionCode());
        }
        viewholder.mBt.setText(implInfo.getActionText(mActivity));
        viewholder.mBt.setTag(implInfo);
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImplInfo info = (ImplInfo)v.getTag();
                switch(info.getAction(mContext)){
                    case ImplInfo.ACTION_DOWNLOAD:
                        ImplAgent.downloadPackage(mContext,
                                data.getmPackageName(),
                                data.getmUrl(),
                                Constant.extenStorageDirPath,
                                data.getmName() + ".apk",
                                3,
                                false,
                                data.getmName(),
                                "",
                                true,
                                data.getmImgUrl(),
                                "",
                                data.getmPackageName(),
                                data.getmVersionCode());
                        break;
                    default:
                        try{
                            mContext.startActivity(info.getActionIntent(mContext));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                }

            }
        });
        return convertView;
    }

    public class ViewHolder {
        public ImageView mImg;
        public TextView mName;
        public TextView mApkSize;
        public TextView mVersionName;
        public Button mBt;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);
        }
    }
}
