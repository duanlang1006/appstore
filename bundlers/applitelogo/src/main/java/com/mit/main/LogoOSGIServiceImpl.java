package com.mit.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applite.common.Constant;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.Locale;


public class LogoOSGIServiceImpl implements ApkplugOSGIService {
	@Override
	public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
        Log.d(SimpleBundle.TAG,"ApkplugOSGIService,recv service:"+servicename+",node="+node);
        if (SimpleBundle.OSGI_SERVICE_LOGO_FRAGMENT.equals(servicename)){
            Fragment fg = new GuideFragment();
            FragmentManager fgm = (FragmentManager)objs[0];
            fgm.beginTransaction().add(node, fg, Constant.OSGI_SERVICE_LOGO_FRAGMENT).commit();
        }
		return null;
	}


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LogoTestFragment extends android.support.v4.app.Fragment {
        SectionsPagerAdapter mSectionsPagerAdapter;
        ViewPager mViewPager;
        private final ActionBar.TabListener mBarTabListener = new ActionBar.TabListener(){
            @Override
            public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            }

            @Override
            public void onTabSelected(ActionBar.Tab arg0, FragmentTransaction arg1) {
                mViewPager.setCurrentItem((int) arg0.getTag());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            }
        };

        private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                Log.d(SimpleBundle.TAG,"onPageScrolled("+i+","+v+","+i2+")");
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(SimpleBundle.TAG,"onPageSelected("+i+")");
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d(SimpleBundle.TAG, "onPageScrollStateChanged(" + i + ")");
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            LayoutInflater mInflater = LayoutInflater.from(context);
            mInflater = mInflater.cloneInContext(context);

            View rootView = mInflater.inflate(R.layout.activity_test, container, false);

            mSectionsPagerAdapter = new SectionsPagerAdapter(this.getFragmentManager());
            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setOnPageChangeListener(mPageChangeListener);
            rootView.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                        OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                                bundleContext, ApkplugOSGIService.class,
                                "(serviceName=osgi.service.host.opt)", //服务查询条件
                                OSGIServiceAgent.real_time);   //每次都重新查询
                        agent.getService().ApkplugOSGIService(bundleContext, SimpleBundle.OSGI_SERVICE_LOGO_FRAGMENT, 0, "");
                    } catch (Exception e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                }
            });
            initActionBar();

            return rootView;
        }

        private void initActionBar(){
            ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
//            actionBar.setTitle("ActionBarTest");
//            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//            actionBar.addTab(actionBar.newTab()
//                    .setText("home")
//                    .setTag(0)
//                    .setTabListener(mBarTabListener));
//            actionBar.addTab(actionBar.newTab()
//                    .setText("game")
//                    .setTag(1)
//                    .setTabListener(mBarTabListener));
            actionBar.hide();
        }

        public class SectionsPagerAdapter extends FragmentPagerAdapter {

            public SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
            }

            @Override
            public int getCount() {
                // Show 3 total pages.
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                Locale l = Locale.getDefault();
                switch (position) {
                    case 0:
                        return getString(R.string.title_section1).toUpperCase(l);
                    case 1:
                        return getString(R.string.title_section2).toUpperCase(l);
                    case 2:
                        return getString(R.string.title_section3).toUpperCase(l);
                }
                return null;
            }
        }

        /**
         * A placeholder fragment containing a simple view.
         */
        public static class PlaceholderFragment extends Fragment {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private static final String ARG_SECTION_NUMBER = "section_number";

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            public static PlaceholderFragment newInstance(int sectionNumber) {
                PlaceholderFragment fragment = new PlaceholderFragment();
                Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                fragment.setArguments(args);
                return fragment;
            }

            public PlaceholderFragment() {
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
                LayoutInflater mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);

                View rootView = mInflater.inflate(R.layout.fragment_test, container, false);
                TextView tv = (TextView)rootView.findViewById(R.id.section_label);
                String text = "" + tv.getText()+getArguments().getInt(ARG_SECTION_NUMBER);
                tv.setText(text);
                return rootView;
            }
        }
    }
}
