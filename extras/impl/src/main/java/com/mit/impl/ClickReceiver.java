//package com.mit.impl;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.Toast;
//
//import com.osgi.extra.OSGIServiceHost;
//
//public class ClickReceiver extends BroadcastReceiver {
//    public ClickReceiver() {
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // TODO: This method is called when the BroadcastReceiver is receiving
//        try {
//            ((OSGIServiceHost) context).jumptoDownloadManager(true);
//        } catch (ClassCastException e) {
//            Toast.makeText(context, "然而并没有跳转", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//    }
//}
