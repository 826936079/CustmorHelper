package com.custmorhelper.model;

import com.custmorhelper.bean.ResultBean;
import com.custmorhelper.manager.GlobleManager;
import com.custmorhelper.util.MyLog;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/16.
 */
public class RobotUtil {
    private static final String TAG = "RobotUtil";

    public static final String API_URL = "http://www.tuling123.com/openapi/api";
    public static final String KEY = "1e91d04c5b2d4dc2aa8b31eace653456";
    public static final String USER_ID = "0728";

    private static int serverLoadTimeout;
    private static String result;

    public interface RobotCallback {
        public void getRobotMsg(String result);
    }

    public static ResultBean getRobotResult(String msg) {
        ResultBean resultBean = new ResultBean();
        Gson gson = new Gson();

        String result = doGet(msg);
        resultBean = gson.fromJson(result, ResultBean.class);

        return resultBean;
    }


    public static String doGet(String msg) {


        final OkHttpClient okHttpClient = GlobleManager.getInstance(GlobleManager.getContext()).getOkHttpClient();

        final Request request = new Request.Builder()
                .url(getUrl(msg))
                .get()
                .build();

        serverLoadTimeout = 0;

        try {
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            InputStream is = response.body().byteStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len = -1;
            byte[] buffer = new byte[128];
            while ( (len = is.read(buffer) ) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            result = new String(baos.toByteArray());
            MyLog.e(TAG, "result:" + result);

            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }




    private static String getResult(final OkHttpClient okHttpClient, Request request) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLog.e(TAG, e.getMessage());
                e.printStackTrace();

                if (e.getCause() != null && e.getCause().equals(SocketTimeoutException.class)
                        && serverLoadTimeout < 5) {
                    serverLoadTimeout++;
                    okHttpClient.newCall(call.request()).enqueue(this);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                serverLoadTimeout = 0;

                InputStream is = response.body().byteStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int len = -1;
                byte[] buffer = new byte[128];
                while ( (len = is.read(buffer) ) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                result = new String(baos.toByteArray());
                MyLog.e(TAG, "result:" + result);

                try {
                    baos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return  result;
    }

    public static String getUrl(String msg) {
        String url = null;

        try {
            url = API_URL + "?key=" + KEY + "&info=" + URLEncoder.encode(msg, "UTF-8") + "&user=" + USER_ID;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return url;
    }


}
