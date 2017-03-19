package com.custmorhelper.manager;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/3/16.
 */
public class GlobleManager {

    private static Context mContext;
    private static GlobleManager globleManager;
    private static OkHttpClient okHttpClient;
    private static Map<String, List<Cookie>> listMap;

    private GlobleManager () {
        listMap = new HashMap<String, List<Cookie>>();
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        listMap.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = listMap.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
    }

    public static GlobleManager getInstance(Context context) {
        if (globleManager == null) {
            mContext = context.getApplicationContext();
            globleManager = new GlobleManager();
        }
        return globleManager;
    }


    public static Context getContext() {
        return mContext;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
