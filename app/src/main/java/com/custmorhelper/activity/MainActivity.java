package com.custmorhelper.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.custmorhelper.R;
import com.custmorhelper.bean.MessageBean;
import com.custmorhelper.fragment.ChangeKeywordDialog;
import com.custmorhelper.fragment.ChangeUserNameDialog;
import com.custmorhelper.manager.GlobleManager;
import com.custmorhelper.service.MainService;
import com.custmorhelper.service.SimulationService;
import com.custmorhelper.util.Constants;
import com.custmorhelper.util.MyLog;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends AppCompatActivity implements ChangeKeywordDialog.OnKeywordChangeListener, ChangeUserNameDialog.OnUserNameChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button startButton;
    private TextView isServiceRunTextView;
    private TextView messageTypeTextView;
    private TextView senderTextView;
    private TextView receiveMessageTextView;
    private TextView sendMessageTextView;

    private TextView monitorUserNameTextView;
    private Button monitorUserNameButton;
    private TextView monitorKeywordTextView;
    private Button monitorKeywordButton;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mToolBarImageView;
    private Toolbar mToolBar;

    private MainReceiver mainReceiver;

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

        startButton = (Button) findViewById(R.id.startButton);
        messageTypeTextView = (TextView) findViewById(R.id.messageTypeTextView);
        senderTextView = (TextView) findViewById(R.id.senderTextView);
        receiveMessageTextView = (TextView) findViewById(R.id.receiveMessageTextView);
        isServiceRunTextView = (TextView) findViewById(R.id.isServiceRunTextView);
        sendMessageTextView = (TextView) findViewById(R.id.sendMessageTextView);
        monitorUserNameTextView = (TextView) findViewById(R.id.monitorUserNameTextView);
        monitorKeywordTextView = (TextView) findViewById(R.id.monitorKeywordTextView);
        monitorUserNameButton = (Button) findViewById(R.id.monitorUserNameButton);
        monitorKeywordButton = (Button) findViewById(R.id.monitorKeywordButton);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.ctl_main);
        mToolBarImageView = (ImageView) findViewById(R.id.ctl_iv_top);
        mToolBar = (Toolbar) findViewById(R.id.toolbar_main);

        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.ic_back);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Glide.with(this).load(R.drawable.bg)
                .bitmapTransform(new BlurTransformation(this, 10))
                .into(mToolBarImageView);

        startButton.setOnClickListener(new View.OnClickListener() {
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

        monitorUserNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeUserNameDialog();
            }
        });

        monitorKeywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeKeywordDialog();
            }
        });


    }

    private void showChangeUserNameDialog() {
        ChangeUserNameDialog changeUserNameDialog = new ChangeUserNameDialog();
        changeUserNameDialog.show(getFragmentManager(), ChangeUserNameDialog.class.getSimpleName());
    }

    private void showChangeKeywordDialog() {
        ChangeKeywordDialog changeKeywordDialog = new ChangeKeywordDialog();
        changeKeywordDialog.show(getFragmentManager(), ChangeKeywordDialog.class.getSimpleName());
    }

    private void initDate() {

//        if (!isAccessibilitySettingsOn(this)) {
//            MyLog.e(TAG, "Accessibility settings is not open");
//        }

        String monitorUsername = GlobleManager.getSharePreferenceMonitor().getString(Constants.SP_MONITOR_USERNAME,
                getString(R.string.monitor_username_default));
        String monitorKeyword = GlobleManager.getSharePreferenceMonitor().getString(Constants.SP_MONITOR_KEYWORD,
                getString(R.string.monitor_keyword_default));
        monitorKeywordTextView.setText(monitorKeyword);
        monitorUserNameTextView.setText(monitorUsername);


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
        changeStatus();
    }

    private void changeStatus() {
        if (isAccessibilitySettingsOn(this)) {
            isServiceRunTextView.setText(getString(R.string.service_is_run));
        } else {
            isServiceRunTextView.setText(getString(R.string.service_not_run));
        }
    }

    @Override
    public void onKeywordChange(String keyword) {
        monitorKeywordTextView.setText(keyword);
    }

    @Override
    public void onUserNameChange(String username) {
        monitorUserNameTextView.setText(username);
    }


    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Bundle bundle = intent.getExtras();
            if (action.equals(Constants.ACTION_MSG)) {
                MessageBean messageBean = bundle.getParcelable(Constants.MSG);

                if (messageBean.getMessageType() == MessageBean.MSG_QQ) {
                    messageTypeTextView.setText(getString(R.string.message_type_qq));
                } else if (messageBean.getMessageType() == MessageBean.MSG_WECHAT) {
                    messageTypeTextView.setText(getString(R.string.message_type_wechat));
                }
                senderTextView.setText(messageBean.getFromName());
                receiveMessageTextView.setText(messageBean.getReceiveContent());
                sendMessageTextView.setText(messageBean.getSendContent());

            } else if (action.equals(Constants.ACTION_MSG_SERVICE)) {
                int serviceState = bundle.getInt(Constants.MSG_SERVICE);

                MyLog.e(TAG, "receive serviceState:" + serviceState);

                if (serviceState == Constants.MSG_SERVICE_ON) {
                    isServiceRunTextView.setText(getString(R.string.service_is_run));
                } else {
                    isServiceRunTextView.setText(getString(R.string.service_not_run));
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
        final String service = getPackageName() + "/" + SimulationService.class.getCanonicalName();
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