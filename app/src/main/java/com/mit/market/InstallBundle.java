//package com.mit.market;
//
//import android.content.Context;
//import org.apkplug.Bundle.BundleControl;
//import org.apkplug.Bundle.OSGIServiceAgent;
//import org.apkplug.Bundle.installCallback;
//import org.osgi.framework.BundleContext;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
///**
// * 替代PropertyInstance.AutoStart()的功能
// * @author 梁前武
// */
//public class InstallBundle {
//	public boolean DEBUG=true;
//	OSGIServiceAgent<BundleControl> agent=null;
//	public InstallBundle(BundleContext mcontext){
//		agent=new OSGIServiceAgent<BundleControl>(mcontext,BundleControl.class);
//	}
//
//    /**
//     *
//     * @param context
//     * @param mcontext
//     * @param name        assets目录下的文件名  如 drag-sort-listview.apk
//     * @param callback        安装事件回掉接口
//     * @param startlevel      插件启动级别 小于2 插件会在框架启动时被自动启动
//     * @param isCheckVersion  是否对比当前安装的插件与已安装插件的版本号，如果为true时 新插件与已安装插件版本相同将不被更新。如果为false时将不检测版本直接覆盖已安装插件
//     * @throws Exception
//     */
//	public void install(Context context,BundleContext mcontext,String name,installCallback callback,int startlevel,boolean isCheckVersion) throws Exception{
//		// startlevel设置为2插件不会自启 isCheckVersion不检测插件版本覆盖更新
//		File f1=null;
//		try {
//				InputStream in=context.getAssets().setup_close(name);
//				f1=new File(context.getFilesDir(),name);
//				if(!DEBUG){
//					//不是调试模式
//					if(!f1.exists()){
//						copy(in, f1);
//						agent.getService().install(mcontext, "file:"+f1.getAbsolutePath(),callback, startlevel,isCheckVersion);
//					}
//				}else{
//					copy(in, f1);
//					agent.getService().install(mcontext, "file:"+f1.getAbsolutePath(),callback, startlevel,isCheckVersion);
//				}
//
//		} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//		} catch (Exception e) {
//				// TODO 自动生成的 catch 块
//				e.printStackTrace();
//		}
//	}
//
//    /**
//     * 安装本地插件服务调用
//     * @param path
//     * @param callback   安装插件的回掉函数
//     * @throws Exception
//     */
//    public void install(Context context,BundleContext mcontext,String path,installCallback callback) throws Exception{
//        try {
//            //插件启动级别为1(会自启) 并且不检查插件版本是否相同都安装
//            agent.getService().install(mcontext, "file:"+path, callback, 1, false);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
////	public void installBundle(Context context,BundleContext mBundleContext,installCallback callback) throws Exception{
////        //把BundleDemo1.apk从assets文件夹中移至应用安装目录中
////        this.install(context, mBundleContext,"BundleDemoJni.apk", callback,2,false);
////        this.install(context, mBundleContext,"BundleDemoStartActivity1.apk", callback,2,false);
////        this.install(context, mBundleContext,"BundleDemoShow.apk", callback,2,false);
////        this.install(context, mBundleContext,"ActivityForResultDemo.apk", callback,2,false);
////  }
//
//    public  static String stutasToStr(int stutas){
//        if(stutas==installCallback.stutas){
//            return "缺少SymbolicName";
//        }else if(stutas==installCallback.stutas1){
//            return "已是最新版本";
//        }else if(stutas==installCallback.stutas2){
//            return "版本号不正确";
//        }else if(stutas==installCallback.stutas3){
//            return " 版本相等";
//        }else if(stutas==installCallback.stutas4){
//            return "无法获取正确的证书";
//        }else if(stutas==installCallback.stutas5){
//            return "更新成功";
//        }else if(stutas==installCallback.stutas6){
//            return "证书不一致";
//        }else if(stutas==installCallback.stutas7){
//            return "安装成功";
//        }
//        return "状态信息不正确";
//    }
//
//	private void copy(InputStream is, File outputFile)
//	        throws IOException
//	    {
//	        OutputStream os = null;
//
//	        try
//	        {
//	            os = new BufferedOutputStream(
//	                new FileOutputStream(outputFile),4096);
//	            byte[] b = new byte[4096];
//	            int len = 0;
//	            while ((len = is.read(b)) != -1)
//	                os.write(b, 0, len);
//	        }
//	        finally
//	        {
//	            if (is != null) is.close();
//	            if (os != null) os.close();
//	        }
//	    }
//
//
//
//}
