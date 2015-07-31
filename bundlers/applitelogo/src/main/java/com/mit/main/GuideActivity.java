package com.mit.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIBaseFragment;
import java.lang.reflect.Constructor;

public class GuideActivity extends FragmentActivity {
    private static final String TAG = "GuideActivity";
    private FragmentManager fm;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        setDefaultFragment();
    }

    /**
     * 设置默认的Fragment
     */
    private void setDefaultFragment() {
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        Fragment fg = ApkPluginFragment.newInstance(Constant.OSGI_SERVICE_LOGO_FRAGMENT,GuideFragment.class.getName(),null);
        transaction.replace(R.id.guide_content, fg);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public static class ApkPluginFragment extends Fragment {
        private final String TAG = "apkplugin_Fragment";
        private Activity mActivity;
        private String mTargetService;
        private String mWhichFragment;
        private Bundle mParams;
        private OSGIBaseFragment mPluginFragment;

        public static Fragment newInstance(String tag, String which, Bundle params) {
            Fragment fg = new ApkPluginFragment();
            Bundle b = new Bundle();
            b.putString("tag", tag);
            b.putString("which", which);
            if (null != params) {
                b.putBundle("params", params);
            }
            fg.setArguments(b);
            return fg;
        }


        public ApkPluginFragment() {
            mTargetService = null;
            mWhichFragment = null;
            mParams = null;
            mPluginFragment = null;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);
            setHasOptionsMenu(true);

            if (null != mPluginFragment) {
                mPluginFragment.onCreate(savedInstanceState);
            } else {
                //wrong
            }
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            LogUtils.d(TAG, "onAttach ");
            mActivity = activity;
            Bundle arguments = getArguments();
            if (null != arguments) {
                mTargetService = arguments.getString("tag");
                mWhichFragment = arguments.getString("which");
                mParams = arguments.getBundle("params");
            }
            try {
                Class<?> cls = Class.forName(mWhichFragment);
                Constructor ct = cls.getConstructor(Fragment.class, String[].class);
                mPluginFragment = (OSGIBaseFragment) ct.newInstance(this, mParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != mPluginFragment) {
                mPluginFragment.onAttach(activity);
            } else {
                //wrong
            }
        }

        @Override
        public void onStart() {
            LogUtils.d(TAG, "onStart ");
            super.onStart();
            if (null != mPluginFragment) {
                mPluginFragment.onStart();
            } else {
                //wrong
            }
        }

        @Override
        public void onResume() {
            LogUtils.d(TAG, "onResume ");
            super.onResume();
            if (null != mPluginFragment) {
                mPluginFragment.onResume();
            } else {
                //wrong
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            LogUtils.d(TAG, "onSaveInstanceState ");
            super.onSaveInstanceState(outState);
            if (null != mPluginFragment) {
                mPluginFragment.onSaveInstanceState(outState);
            } else {
                //wrong
            }
        }

        @Override
        public void onPause() {
            LogUtils.d(TAG, "onPause ");
            super.onPause();
            if (null != mPluginFragment) {
                mPluginFragment.onPause();
            } else {
                //wrong
            }
        }

        @Override
        public void onStop() {
            LogUtils.d(TAG, "onStop ");
            super.onStop();
            if (null != mPluginFragment) {
                mPluginFragment.onStop();
            } else {
                //wrong
            }
        }

        @Override
        public void onDestroy() {
            LogUtils.d(TAG, "onDestroy ");
            super.onDestroy();
            if (null != mPluginFragment) {
                mPluginFragment.onDestroy();
            } else {
                //wrong
            }
        }

        @Override
        public void onDestroyView() {
            LogUtils.d(TAG, "onDestroyView ");
            super.onDestroyView();
            if (null != mPluginFragment) {
                mPluginFragment.onDestroyView();
            } else {
                //wrong
            }
        }

        @Override
        public void onDetach() {
            LogUtils.d(TAG, "onDetach ");
            super.onDetach();
            if (null != mPluginFragment) {
                mPluginFragment.onDetach();
            } else {
                //wrong
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            LogUtils.d(TAG, "onCreateView ");
            if (null != mPluginFragment) {
                return mPluginFragment.onCreateView(inflater, container, savedInstanceState);
            } else {
                //wrong
                return super.onCreateView(inflater, container, savedInstanceState);
            }
        }

        @Override
        public void onHiddenChanged(boolean hidden) {
            LogUtils.d(TAG, "onHiddenChanged: " + hidden);
            super.onHiddenChanged(hidden);
            if (null != mPluginFragment) {
                mPluginFragment.onHiddenChanged(hidden);
            } else {
                //wrong
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    getFragmentManager().popBackStack();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
