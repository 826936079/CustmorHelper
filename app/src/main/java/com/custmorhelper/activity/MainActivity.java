package com.custmorhelper.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.custmorhelper.R;
import com.custmorhelper.bean.MessageBean;
import com.custmorhelper.bean.ResultBean;
import com.custmorhelper.service.MainService;
import com.custmorhelper.service.MonitorService;
import com.custmorhelper.util.Constants;
import com.custmorhelper.util.MyLog;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button startBtn;
    private TextView isServiceRun;
    private TextView messageType;
    private TextView sender;
    private TextView lastMessage;

    private MainReceiver mainReceiver;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            ResultBean resultBean = (ResultBean) msg.obj;
            MyLog.e(TAG, "handleMessage->msg:" + resultBean.getText());

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {
        initView();
        initDate();
    }

    private void initView() {

        startBtn = (Button) findViewById(R.id.start);
        messageType = (TextView) findViewById(R.id.messageType);
        sender = (TextView) findViewById(R.id.sender);
        lastMessage = (TextView) findViewById(R.id.lastMessage);
        isServiceRun = (TextView) findViewById(R.id.isServiceRun);


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //打开系统设置中辅助功能
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, R.string.find_envelope, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initDate() {

//        if (!isAccessibilitySettingsOn(this)) {
//            MyLog.e(TAG, "Accessibility settings is not open");
//        }

        Intent intent = new Intent(this, MainService.class);
        startService(intent);

        mainReceiver = new MainReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_MSG);
        intentFilter.addAction(Constants.ACTION_MSG_SERVICE);

        registerReceiver(mainReceiver, intentFilter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Bundle bundle = intent.getExtras();
            if (action.equals(Constants.ACTION_MSG)) {
                MessageBean messageBean = bundle.getParcelable(Constants.MSG);

                if (messageBean.getType() == Constants.MSG_QQ) {
                    messageType.setText(getString(R.string.message_type_qq));
                } else if (messageBean.getType() == Constants.MSG_WECHAT) {
                    messageType.setText(getString(R.string.message_type_wechat));
                }
                sender.setText(messageBean.getSender());
                lastMessage.setText(messageBean.getContent());

            } else if (action.equals(Constants.ACTION_MSG_SERVICE)) {
                int serviceState = bundle.getInt(Constants.MSG_SERVICE);

                MyLog.e(TAG, "receive serviceState:" + serviceState);

                if (serviceState == Constants.MSG_SERVICE_ON) {
                    isServiceRun.setText(getString(R.string.service_is_run));
                } else {
                    isServiceRun.setText(getString(R.string.service_not_run));
                }


            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mainReceiver);
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MonitorService.class.getCanonicalName();
        MyLog.e(TAG, "-------------- > service :: " + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            MyLog.e(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            MyLog.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            MyLog.e(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    MyLog.e(TAG, "-------------- > accessibilityService :: " + accessibilityService + "   " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        MyLog.e(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            MyLog.e(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }


}