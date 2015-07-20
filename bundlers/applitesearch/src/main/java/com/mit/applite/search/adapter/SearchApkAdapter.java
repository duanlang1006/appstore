package com.mit.applite.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.applite.search.R;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.main.BundleContextFactory;
import com.mit.applite.search.utils.SearchUtils;
import com.mit.applite.search.view.ProgressButton;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;

import net.tsz.afinal.FinalBitmap;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * Created by LSY on 15-5-27.
 */
public class SearchApkAdapter extends BaseAdapter {

    private final FinalBitmap mFinalBitmap;
    private final UpdateInatsllButtonText mListener;
    private LayoutInflater mInflater;
    private Context context;
    public List<SearchBean> mSearchBeans;
    private Context mActivity;
    public interface UpdateInatsllButtonText{
        void updateText();
    }

    public SearchApkAdapter(Context context, List<SearchBean> mSearchBeans ,UpdateInatsllButtonText listener) {
        mListener = listener;
        this.mSearchBeans = mSearchBeans;
        mFinalBitmap = FinalBitmap.create(context);
        mActivity = context;
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
        return mSearchBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchBeans.get(position);
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
            convertView = mInflater.inflate(R.layout.item_search_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            // 不为空则直接使用已有的封装类
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SearchBean data = mSearchBeans.get(position);
        mFinalBitmap.display(viewholder.mImg, data.getmImgUrl());
        viewholder.mName.setText(data.getmName());
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(Long.parseLong(data.getmApkSize())));
        viewholder.mDownloadNumber.setText(
                SearchUtils.getDownloadNumber(context, Integer.parseInt(data.getmDownloadNumber())) +
                        context.getResources().getString(R.string.download_number));
        viewholder.mVersionName.setText(context.getResources().getString(R.string.version) +
                data.getmVersionName());
        viewholder.mToDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchUtils.toDetailFragment(data.getmPackageName(), data.getmName(), data.getmImgUrl());
            }
        });

        ImplInfo info = data.getImplInfo();
        if (null == info){
            ImplAgent.queryDownload(mActivity,data.getmPackageName());
            info = ImplInfo.create(mActivity,data.getmPackageName(),data.getmDownloadUrl(),data.getmPackageName());
        }
        viewholder.mProgressButton.setText(info.getActionText(mActivity));
        viewholder.mProgressButton.setTag(info);
        viewholder.mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener(View view) {
                ImplInfo info = (ImplInfo)view.getTag();
                switch (info.getAction(mActivity)){
                    case ImplInfo.ACTION_DOWNLOAD:
                        ImplAgent.downloadPackage(context,
                                data.getmPackageName(),
                                data.getmDownloadUrl(),
                                Constant.extenStorageDirPath,
                                data.getmName() + ".apk",
                                3,
                                false,
                                data.getmName(),
                                "",
                                true,
                                data.getmImgUrl(),
                                "",
                                data.getmPackageName());
                        break;
                    default:
                        try{
                            mActivity.startActivity(info.getActionIntent(mActivity));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        viewholder.mXing.setRating(Float.parseFloat(data.getmXing()) / 2.0f);
        return convertView;
    }

    public class ViewHolder {
        public LinearLayout mToDetail;
        public ImageView mImg;
        public RatingBar mXing;
        public TextView mName;
        public TextView mDownloadNumber;
        public TextView mApkSize;
        public TextView mVersionName;
        public Button mBt;
        public ProgressButton mProgressButton;

        public ViewHolder(View v) {
            this.mToDetail = (LinearLayout) v.findViewById(R.id.list_item_to_detail);
            this.mImg = (ImageView) v.findViewById(R.id.list_item_img);
            this.mName = (TextView) v.findViewById(R.id.list_item_name);
            this.mXing = (RatingBar) v.findViewById(R.id.list_item_xing);
            this.mDownloadNumber = (TextView) v.findViewById(R.id.list_item_number);
            this.mApkSize = (TextView) v.findViewById(R.id.list_item_size);
            this.mVersionName = (TextView) v.findViewById(R.id.list_item_versionname);
            this.mBt = (Button) v.findViewById(R.id.list_item_bt);
            this.mProgressButton = (ProgressButton) v.findViewById(R.id.list_item_progress_button);
        }
    }
}
