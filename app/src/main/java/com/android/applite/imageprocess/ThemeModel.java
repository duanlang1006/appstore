package com.android.applite.imageprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;

public class ThemeModel {
	public String packageName;

	public String name;

	public int version;

	public final HashMap<ComponentName, String> appIcons = new HashMap<ComponentName, String>();

	public final List<String> wallpapers = new ArrayList<String>();

	public final List<String> iconBackgrounds = new ArrayList<String>();

	public String iconFg;

	public String iconShade;

	public String delete;

	public DockBar dockbar = new DockBar();

	public Folder folder = new Folder();

	public Navigation navigation = new Navigation();

	public OpenFolder openFolder = new OpenFolder();

	// public Navigation nav = new Navigation();

	public IconMask iconMask = new IconMask();

	public Scence scence = new Scence();

	public final HashMap<String, String> wigetIcons = new HashMap<String, String>();

	public static class DockBar {

		public String background;

		public String app_handle;

		public String home_handle;

		public String ic_market;

		public String ic_search;
	}

	public static class Folder {

		public String background;

		public String shade_open;

		public String shade_close;
	}

	public static class Navigation {

		public String normal;

		public String selected;
	}

	public static class OpenFolder {

		public String folderBottomLeftDraw;

		public String folderBottomRightDraw;

		public String folderBottomBodyDraw;

		public String folderUpLeftDraw;

		public String folderUpRightDraw;

		public String folderUpBodyDraw;
	}

	public static class IconMask {
		public String mask;
		public int x,y,w,h;
		public int paddingLeft,paddingTop,paddingRight,paddingBottom;
		public float degX;
		public float degY;
	}

	public static class Scence {

		public String background;

		public String select_icon;

		public String normal_button;

		public String press_button;

		public String normal_indication;

		public String hilite_indication;
	}

}
