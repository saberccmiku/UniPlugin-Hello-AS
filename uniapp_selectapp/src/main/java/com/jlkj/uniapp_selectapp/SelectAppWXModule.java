package com.jlkj.uniapp_selectapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.dom.binding.JSONUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectAppWXModule extends WXSDKEngine.DestroyableModule {


    @JSMethod(uiThread = true)
    public void selectApp(JSCallback jsCallback) {

        if (mWXSDKInstance.getContext() instanceof Activity) {
            loadApplications(jsCallback);
        }
    }

    private void loadApplications(JSCallback jsCallback) {
        Activity activity = (Activity) mWXSDKInstance.getContext();
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(packageManager));
        for (ResolveInfo app : apps) {
            AppInfo appInfo = new AppInfo();
            appInfo.setName(app.loadLabel(packageManager));
            appInfo.setActivity(new ComponentName(app.activityInfo.applicationInfo.packageName, app.activityInfo.name),
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            appInfo.setIcon(app.activityInfo.loadIcon(packageManager));
            appInfo.setPackageName(app.activityInfo.applicationInfo.packageName);
            appInfo.setClassName(app.activityInfo.name);
            appInfoList.add(appInfo);
        }
        tracking(appInfoList,jsCallback);
    }

    private void tracking(List<AppInfo> appInfoList, final JSCallback jsCallback) {
        List<Map<String,String>> list = new ArrayList<>();
        for (int i = 0; i < appInfoList.size(); i++) {
            Map<String,String> map = new HashMap<>();
            map.put("name",appInfoList.get(i).getName().toString());
            list.add(map);
        }
        JSONObject result = new JSONObject();
        result.put("appInfoList",list);
        result.put("date",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" ).format(new Date()));
        result.put("total",appInfoList.size());
        jsCallback.invoke(result);
    }

    class AppText{
        private String name;

        public AppText(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @Override
    public void destroy() {

    }
}
