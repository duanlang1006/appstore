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
import com.mit.applite.search.R;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.main.BundleContextFactory;
import com.mit.applite.search.utils.SearchUtils;
import com.mit.applite.search.view.ProgressButton;
import com.mit.impl.ImplAgent;

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
    private LayoutInflater mInflater;
    private Context context;
    public List<SearchBean> mSearchBeans;


    public SearchApkAdapter(Context context, List<SearchBean> mSearchBeans) {
        this.mSearchBeans = mSearchBeans;
        mFinalBitmap = FinalBitmap.create(context);
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

    public void setList(List list) {
        mSearchBeans = list;
        notifyDataSetChanged();
    }

    public List getList() {
        return mSearchBeans;
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
                toDetailFragment(data.getmName());
            }
        });

        final int mApkType = AppliteUtils.isAppInstalled(context, data.getmPackageName(), data.getmVersionCode());
//        if (mApkType == Utils.INSTALLED) {
//            viewholder.mBt.setText(context.getResources().getString(R.string.open));
//        } else if (mApkType == Utils.INSTALLED_UPDATE) {
//            viewholder.mBt.setText(context.getResources().getString(R.string.update));
//        } else if (mApkType == Utils.UNINSTALLED) {
//            viewholder.mBt.setText(context.getResources().getString(R.string.install));
//        }
//        final ViewHolder finalViewholder = viewholder;
//        viewholder.mBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mApkType == Utils.INSTALLED) {
//                    Utils.startApp(context, data.getmPackageName());
//                } else {
//                    Utils.setDownloadViewText(context, finalViewholder.mBt);
//                    ImplAgent.downloadPackage(context,
//                            data.getmPackageName(),
//                            data.getmDownloadUrl(),
//                            Utils.extenStorageDirPath,
//                            data.getmName() + ".apk",
//                            3,
//                            false,
//                            data.getmName(),
//                            "",
//                            true,
//                            data.getmImgUrl(),
//                            "",
//                            data.getmPackageName());
//                }
//            }
//        });
        if (mApkType == Constant.INSTALLED) {
            viewholder.mProgressButton.setText(context.getResources().getString(R.string.open));
        } else if (mApkType == Constant.INSTALLED_UPDATE) {
            viewholder.mProgressButton.setText(context.getResources().getString(R.string.update));
        } else if (mApkType == Constant.UNINSTALLED) {
            viewholder.mProgressButton.setText(context.getResources().getString(R.string.install));
        }
        viewholder.mProgressButton.setText(data.getmShowButtonText());
        viewholder.mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (mApkType == Constant.INSTALLED) {
                    AppliteUtils.startApp(context, data.getmPackageName());
                } else {
//                    Utils.setDownloadViewText(context, finalViewholder.mProgressButton);
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
                }
            }
        });
        viewholder.mXing.setRating(Float.parseFloat(data.getmXing()) / 2.0f);
        return convertView;
    }

    /**
     * 去详情页面
     */
    public void toDetailFragment(String name) {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=" + Constant.OSGI_SERVICE_HOST_OPT + ")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_SEARCH_FRAGMENT,
                    0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT, name);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
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
