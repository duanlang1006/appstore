package com.kapps.market.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.kapps.market.R;
import com.kapps.market.log.LogUtil;
import com.kapps.market.util.Constants;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


/**
 * 2010-9-21
 * 
 * @author admin
 * 
 */
public class DirsChooser extends CommonView implements OnItemClickListener, OnClickListener {

	public static final String TAG = "DirsChooser";

	// dirlabel
	private TextView dirLabel;
	// path
	private ArrayList<DirsWrap> dirsWrapList = new ArrayList<DirsWrap>();
	// �Ƿ�����ˡ�
	private boolean isError;

	/**
	 * @param context
	 */
	public DirsChooser(Context context) {
		super(context);
		addView(R.layout.dirs_chooser);

		dirLabel = (TextView) findViewById(R.id.dirLabel);
		findViewById(R.id.tryButton).setOnClickListener(this);
		ListView dirsList = (ListView) findViewById(R.id.dirsList);
		dirsList.setOnItemClickListener(this);
		dirsList.setAdapter(new FilePathAdapter());

		initChooser();
	}

	private void initChooser() {
		boolean sdCardOk = marketContext.getMarketManager().checkSDCardStateAndNote();
		if (sdCardOk) {
			DirsWrap dirsWrap = new DirsWrap(Environment.getExternalStorageDirectory());
			dirsWrapList.add(dirsWrap);
			showCurrentPath(dirsWrap);

		} else {
			showError();
		}
	}

	/**
	 * ��ʾǰһ��·����Ŀ¼
	 * 
	 * @return �Ƿ񻹿�����ʾ
	 */
	public boolean showPrefixPath() {
		if (dirsWrapList.size() > 1 && !isError) {
			// �Ƴ����һ��
			int size = dirsWrapList.size();
			dirsWrapList.remove(size - 1);
			DirsWrap dirsWrap = dirsWrapList.get(size - 2);
			showCurrentPath(dirsWrap);
			return true;

		} else {
			return false;
		}
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getCurrentDir() {
		if (dirsWrapList.size() > 0) {
			File file = dirsWrapList.get(dirsWrapList.size() - 1).getPath();
			return file.getAbsolutePath();
		} else {
			return null;
		}
	}

	/**
	 * ��õ�ǰ��Ŀ¼�����sdcard��rootĿ¼��ôֱ�ӷ���" "<br>
	 * ��apk�����л���ӵ�ǰ��sdcard��·����<br>
	 * ���ص�Ŀ¼��������ʼ/<br>
	 * see: LocalMarketService.initApkSummaryInfoList()
	 * 
	 * @return
	 */
	public String getCurrentDirNoneRoot() {
		if (dirsWrapList.size() > 0) {
			if (dirsWrapList.size() == 1) {
				return "";

			} else {
				String root = dirsWrapList.get(0).getPathStr();
				String file = dirsWrapList.get(dirsWrapList.size() - 1).getPathStr();
				return file.substring(root.length() + 1);
			}

		} else {
			return null;
		}
	}

	// ��ʾ����
	private void showError() {
		isError = true;
		findViewById(R.id.okButton).setEnabled(false);
		findViewById(R.id.tryButton).setVisibility(VISIBLE);
	}

	// ��ʾ��ǰ��·��
	private void showCurrentPath(DirsWrap dirsWrap) {
		LogUtil.d(TAG, "showCurrentPath ******* dirsWrap: " + dirsWrap);
		try {
			dirLabel.setText(dirsWrap.getPathStr());
			File path = dirsWrap.getPath();
			File[] files = path.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					boolean accept = pathname.isDirectory();
					String name = pathname.getName();
					accept &= (name.indexOf("") != 0)
							& (!name.equals(Constants.DOWNLOAD_DIR) & (!name.equals(Constants.DEFAULT_BASE_Dir)));
					return accept;
				}
			});
			LogUtil.d(TAG, "******* files: " + files);
			if (files == null) {
				showError();

			} else {
				Arrays.sort(files, new FileNameCompare());
				dirsWrap.setSubDirs(files);
				ListView dirsList = (ListView) findViewById(R.id.dirsList);
				BaseAdapter adapter = (BaseAdapter) dirsList.getAdapter();
				adapter.notifyDataSetChanged();
			}

		} catch (Exception e) {
			e.printStackTrace();
			showError();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = (File) parent.getItemAtPosition(position);
		DirsWrap dirsWrap = new DirsWrap(file);
		dirsWrapList.add(dirsWrap);
		showCurrentPath(dirsWrap);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tryButton) {
			isError = false;
			findViewById(R.id.okButton).setEnabled(true);
			v.setVisibility(GONE);
			if (dirsWrapList.size() == 0) {
				initChooser();
			} else {
				DirsWrap dirsWrap = dirsWrapList.get(dirsWrapList.size() - 1);
				showCurrentPath(dirsWrap);
			}
		}
	}

	// �ļ����������
	private class FilePathAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return dirsWrapList.size() > 0 ? dirsWrapList.get(dirsWrapList.size() - 1).getSubDirs().length : 0;
		}

		@Override
		public File getItem(int position) {
			return dirsWrapList.size() > 0 ? dirsWrapList.get(dirsWrapList.size() - 1).getSubDirs()[position] : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.dir_item, parent, false);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.dirNameLabel);
			textView.setText(getItem(position).getName());
			return convertView;
		}

	}

	// Ŀ¼��װ
	private class DirsWrap {
		private File path;
		private File[] subDirs = new File[0];

		public DirsWrap(File path) {
			this.path = path;
		}

		public String getPathStr() {
			return path.getAbsolutePath();
		}

		/**
		 * @return the path
		 */
		public File getPath() {
			return path;
		}

		/**
		 * @param path
		 *            the path to set
		 */
		public void setPath(File path) {
			this.path = path;
		}

		/**
		 * @return the subDirs
		 */
		public File[] getSubDirs() {
			return subDirs;
		}

		/**
		 * @param subDirs
		 *            the subDirs to set
		 */
		public void setSubDirs(File[] subDirs) {
			this.subDirs = subDirs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "DirsWrap [path=" + path + ", subDirs=" + Arrays.toString(subDirs) + "]";
		}

	}

	// ��Ŀ¼��������
	private class FileNameCompare implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			String name1 = o1.getName().toLowerCase();
			String name2 = o2.getName().toLowerCase();
			return name1.compareTo(name2);
		}
	}

}
