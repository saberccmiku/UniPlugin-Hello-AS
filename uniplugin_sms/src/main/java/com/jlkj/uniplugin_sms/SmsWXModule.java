package com.jlkj.uniplugin_sms;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsWXModule extends WXSDKEngine.DestroyableModule {

    public static final int REQ_CODE_CONTACT = 1;
    private JSCallback jsCallback;

    @JSMethod(uiThread = true)
    public void selectSms(JSCallback jsCallback) {

        if (mWXSDKInstance.getContext() instanceof Activity) {
            this.jsCallback = jsCallback;
            readSMS();
        }
    }

    /**
     * 检查申请短信权限
     */
    private void checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(mWXSDKInstance.getContext(), Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //未获取到读取短信权限

            //向系统申请权限
            ActivityCompat.requestPermissions((Activity) mWXSDKInstance.getContext(),
                    new String[]{Manifest.permission.READ_SMS}, REQ_CODE_CONTACT);
        } else {
            query();
        }
    }

    /**
     * 读取短信
     */
    private void readSMS() {
        checkSMSPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //判断用户是否，同意 获取短信授权
        if (requestCode == REQ_CODE_CONTACT && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //获取到读取短信权限
            query();
        } else {
            //Toast.makeText(this, "未获取到短信权限", Toast.LENGTH_SHORT).show();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 201);
            jsonObject.put("msg", "未获取到短信权限");
            jsCallback.invoke(jsonObject);
        }
    }

    private void query() {
        List<Map<String, Object>> maps = new ArrayList<>();
        //读取所有短信
        Uri uri = Uri.parse("content://sms/");
        ContentResolver resolver = mWXSDKInstance.getContext().getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id", "address", "body", "date", "type"}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            int _id;
            String address;
            String body;
            String date;
            int type;
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<String, Object>();
                _id = cursor.getInt(0);
                address = cursor.getString(1);
                body = cursor.getString(2);
                date = cursor.getString(3);
                type = cursor.getInt(4);
                map.put("_id", _id);
                map.put("address", address);
                map.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Double.valueOf(date)));
                map.put("type", type);
                map.put("body", body);
                Log.i("test", "_id=" + _id + " address=" + address + " body=" + body + " date=" + date + " type=" + type);
                maps.add(map);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 200);
        jsonObject.put("smsList", maps);
        jsCallback.invoke(jsonObject);
    }

    @Override
    public void destroy() {

    }
}
