package com.custmorhelper.service;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.custmorhelper.R;
import com.custmorhelper.activity.MainActivity;
import com.custmorhelper.bean.MessageBean;
import com.custmorhelper.manager.GlobleManager;
import com.custmorhelper.model.RobotUtil;
import com.custmorhelper.util.Constants;
import com.custmorhelper.util.MyLog;
import com.custmorhelper.util.ToastUtil;

import java.util.Calendar;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SimulationService extends AccessibilityService {
    private static final String TAG = SimulationService.class.getSimpleName();

    private boolean enableKeyguard = true;//默认有屏幕锁

    //窗口状态
    private static final int WINDOW_NONE = 0;
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_OTHER = -1;
    //当前窗口
    private int mCurrentWindow = WINDOW_NONE;

    //锁屏、解锁相关
    private KeyguardManager km;
    private KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;

    //收到的信息
    private String sendMessage = "";

    private Parcelable mNotifyData;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case Constants.HANDLE_GET_ROBOT_MSG:
                    try {
                        MessageBean messageBean = (MessageBean) msg.obj;
                        sendMessage = messageBean.getSendContent();

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMessage = getString(R.string.error_msg);
                    }

                    break;

                case Constants.HANDLE_AUTO_INPUT:

                    AccessibilityNodeInfo nodeInfoInput = (AccessibilityNodeInfo) msg.obj;

                    if (sendMessage == null || sendMessage.isEmpty()) {
                        break;
                    }
                    performInput(nodeInfoInput, sendMessage);

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }
                    simulationSendEventByTextName();
                    break;

                case Constants.HANDLE_AUTO_SEND:

                    AccessibilityNodeInfo nodeInfoSend = (AccessibilityNodeInfo) msg.obj;
                    MyLog.e(TAG, "receive button node : " + nodeInfoSend.toString());
                    performClick(nodeInfoSend);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }
                    //performHome(SimulationService.this);
                    goToMainActivity();
                    break;
                default:

                    break;

            }

            super.handleMessage(msg);
        }
    };

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    //播放提示声音
    private MediaPlayer player;

    public void playSound(Context context) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        //夜间不播放提示音
        if (hour > 7 && hour < 22) {
            player.start();
        }
    }

    //唤醒屏幕和解锁
    private void wakeAndUnlock(boolean unLock) {
        if (unLock) {
            //若为黑屏状态则唤醒屏幕
            if (!pm.isScreenOn()) {
                //获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
                wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
                //点亮屏幕
                wl.acquire();
                MyLog.e(TAG, "亮屏");
            }
            //若在锁屏界面则解锁直接跳过锁屏
            if (km.inKeyguardRestrictedInputMode()) {
                //设置解锁标志，以判断抢完红包能否锁屏
                enableKeyguard = false;
                //解锁
                kl.disableKeyguard();
                MyLog.e(TAG, "解锁");
            }
        } else {
            //如果之前解过锁则加锁以恢复原样
            if (!enableKeyguard) {
                //锁屏
                kl.reenableKeyguard();
                MyLog.e(TAG, "加锁");
            }
            //若之前唤醒过屏幕则释放之使屏幕不保持常亮
            if (wl != null) {
                wl.release();
                wl = null;
                MyLog.e(TAG, "灭屏");
            }
        }
    }

    //通过文本查找节点
    public AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    //通过id查找节点
    public AccessibilityNodeInfo findNodeInfosByViewId(AccessibilityNodeInfo nodeInfo, String viewid) {
        List<AccessibilityNodeInfo> list = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            list = nodeInfo.findAccessibilityNodeInfosByViewId(viewid);
        }
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);

    }


    //模拟点击事件
    public boolean performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        if (nodeInfo.isClickable()) {
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            MyLog.e(TAG, "节点不可点击，递归尝试点击");
            return performClick(nodeInfo.getParent());
        }
    }

    //模拟返回事件
    public boolean performBack(AccessibilityService service) {
        if (service == null) {
            return false;
        }
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }

    //模拟home事件
    public boolean performHome(AccessibilityService service) {
        if (service == null) {
            return false;
        }
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }

    //模拟点击按钮
    private void nextClick(List<AccessibilityNodeInfo> infos) {
        if (infos != null) {
            for (AccessibilityNodeInfo info : infos) {
                if (info.isEnabled() && info.isClickable()) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    //模拟输入
    public void performInput(AccessibilityNodeInfo nodeInfo, String text) {
        //Android 5.0 版本及以上：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            sendMessage = null;
        }
        //Android 4.3 版本及以上
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(Constants.INPUT, text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            sendMessage = null;
        }

    }


    //实现辅助功能
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        String packageName = event.getPackageName().toString();
        String className = event.getClassName().toString();

        for (int i = 0; i < event.getRecordCount(); i++) {
            MyLog.e(TAG, "event:" + "i->" + event.getRecord(i));

        }


        switch (eventType) {
            //第一步：监听通知栏消息
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();

                MyLog.e(TAG, "收到的消息为:" + texts);
                MyLog.e(TAG, "event.getParcelableData(): " + event.getParcelableData());
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();

                        int index = content.indexOf(Constants.SEPARATED, 1);

                        String receiveName = content.substring(0, index);
                        final String receiveMessage = content.substring(index + 1, content.length()).trim();


                        //创建message bean对象  为了显示提在判断外面
                        final MessageBean messageBean = new MessageBean();
                        if (packageName.equals(Constants.PKG_QQ)) {
                            messageBean.setMessageType(MessageBean.MSG_QQ);

                        } else if (packageName.equals(Constants.PKG_WECHAT)) {
                            messageBean.setMessageType(MessageBean.MSG_WECHAT);
                        }
                        messageBean.setFromName(receiveName);
                        messageBean.setReceiveContent(receiveMessage);
                        messageBean.setReceiveTime(System.currentTimeMillis());


                        String monitorUsername = GlobleManager.getSharePreferenceMonitor().getString(Constants.SP_MONITOR_USERNAME,
                                getString(R.string.monitor_username_default));
                        String monitorKeyword = GlobleManager.getSharePreferenceMonitor().getString(Constants.SP_MONITOR_KEYWORD,
                                getString(R.string.monitor_keyword_default));
                        MyLog.e(TAG, "monitorUsername:" + monitorUsername + " ,monitorKeyword:" + monitorKeyword);

                        //发送广播
                        //sendUpdateBroadcast(messageBean);


                        if (monitorUsername.trim().isEmpty() || monitorKeyword.trim().isEmpty()) {
                            return;
                        }

                        //收到@提醒或者收到指定用户信息
                        if (receiveMessage.startsWith(monitorKeyword) || receiveName.equals(monitorUsername)) {

                            MyLog.e(TAG, "收到@消息:" + receiveMessage);


                            if (receiveMessage.startsWith(monitorKeyword)) {
                                mNotifyData = event.getParcelableData();
                                if (receiveMessage.trim().equals(monitorKeyword)
                                        || receiveMessage.trim().equals(monitorKeyword + " ")) {  //手机@时返回有后面的字符串

                                    MyLog.e(TAG, "发送欢迎消息");
                                    sendMessage = getString(R.string.welcome_msg);
                                    messageBean.setSendContent(sendMessage);

                                    sendUpdateBroadcast(messageBean);
                                    monitorOpenNotify(event.getParcelableData());

                                } else {

                                    int msgIndex = receiveMessage.trim().indexOf(monitorKeyword);
                                    if (msgIndex < 0) {
                                        break;
                                    }

                                    final String receiveMsg = receiveMessage.trim().substring(msgIndex + monitorKeyword.length());
                                    MyLog.e(TAG, "msgIndex:" + msgIndex + " , receiveMsg:" + receiveMsg);

                                    Flowable.create(new FlowableOnSubscribe<MessageBean>() {
                                        @Override
                                        public void subscribe(@NonNull FlowableEmitter<MessageBean> e) throws Exception {
                                            //获取返回数据
                                            sendMessage = RobotUtil.getChatMessage(receiveMsg);
                                            messageBean.setSendContent(sendMessage);
                                            e.onNext(messageBean);
                                            e.onComplete();
                                        }
                                    }, BackpressureStrategy.ERROR)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Consumer<MessageBean>() {
                                                @Override
                                                public void accept(@NonNull MessageBean e) throws Exception {
                                                    //模拟打开通知栏消息
                                                    MyLog.e(TAG, "subscribe->messageBean:" + messageBean.toString());
                                                    sendUpdateBroadcast(messageBean);
                                                    monitorOpenNotify(mNotifyData);
                                                }
                                            }, new Consumer<Throwable>() {
                                                @Override
                                                public void accept(@NonNull Throwable throwable) throws Exception {
                                                    MyLog.e(TAG, "ERR: " + throwable.getMessage());
                                                }
                                            });

                                }
                            } else if (receiveName.equals(monitorUsername)) {

                                Flowable.create(new FlowableOnSubscribe<MessageBean>() {
                                    @Override
                                    public void subscribe(@NonNull FlowableEmitter<MessageBean> e) throws Exception {
                                        //获取返回数据
                                        sendMessage = RobotUtil.getChatMessage(receiveMessage);
                                        messageBean.setSendContent(sendMessage);
                                        e.onNext(messageBean);
                                        e.onComplete();
                                    }
                                }, BackpressureStrategy.ERROR)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<MessageBean>() {
                                            @Override
                                            public void accept(@NonNull MessageBean e) throws Exception {
                                                //模拟打开通知栏消息
                                                MyLog.e(TAG, "subscribe->messageBean:" + messageBean.toString());
                                                sendUpdateBroadcast(messageBean);
                                                monitorOpenNotify(mNotifyData);
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(@NonNull Throwable throwable) throws Exception {
                                                MyLog.e(TAG, "ERR: " + throwable.getMessage());
                                            }
                                        });

                            }

                            break;
                        }


                    }
                }
                break;
            //第二步：监听是否进入微信红包消息界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                MyLog.e(TAG, "TYPE_WINDOW_STATE_CHANGED");

                if (className.equals(Constants.CLASS_WECHAT_LAUNCHERUI)) {
                    mCurrentWindow = WINDOW_LAUNCHER;
                    //进入聊天界面
                    MyLog.e(TAG, "进入聊天界面...");

                    simulationInputEvent();

                } else {
                    MyLog.e(TAG, "other window");
                    mCurrentWindow = WINDOW_OTHER;
                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

                MyLog.e(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
                break;
        }
    }

    private void sendUpdateBroadcast(MessageBean messageBean) {
        MyLog.e(TAG, "messageBean:" + messageBean.toString());
        Intent intent = new Intent();
        intent.putExtra(Constants.MSG, messageBean);
        intent.setAction(Constants.ACTION_MSG);
        sendBroadcast(intent);
    }

    private void monitorOpenNotify(Parcelable data) {
        if (data != null && data instanceof Notification) {
            //播放提示音
            //playSound(this);
            wakeAndUnlock(true);
            try {
                Notification notification = (Notification) data;
                PendingIntent pendingIntent = notification.contentIntent;
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //模拟输入事件
    private void simulationInputEvent() {


        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }

        MyLog.e(TAG, "rootNodeInfo.getChildCount:" + rootNodeInfo.getChildCount());
        for (int index = 0; index < rootNodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo nodeInfo = rootNodeInfo.getChild(index);

            findEditTExtNodeInfo(nodeInfo);
            nodeInfo.recycle();
        }

        rootNodeInfo.recycle();

    }

    //模拟发送事件
    private void simulationSendEvent() {


        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }

        MyLog.e(TAG, "rootNodeInfo.getChildCount:" + rootNodeInfo.getChildCount());
        for (int index = 0; index < rootNodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo nodeInfo = rootNodeInfo.getChild(index);

            findButtonNodeInfo(nodeInfo);
            nodeInfo.recycle();
        }

        rootNodeInfo.recycle();

    }


    private void simulationSendEventByTextName() {


        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();

        List<AccessibilityNodeInfo> sendNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByText("发送");
        MyLog.e(TAG, "sendNodeInfos:" + sendNodeInfos);
        nextClick(sendNodeInfos);

        //performHome(SimulationService.this);
        goToMainActivity();

    }


    //模拟事件
    private void simulationEventByType(int type) {


        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }

        MyLog.e(TAG, "rootNodeInfo.getChildCount:" + rootNodeInfo.getChildCount());
        for (int index = 0; index < rootNodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo nodeInfo = rootNodeInfo.getChild(index);

            MyLog.e(TAG, "type:" + type);
            findNodeInfo(nodeInfo, type);
            nodeInfo.recycle();
        }

        rootNodeInfo.recycle();

    }

    private void findEditTExtNodeInfo(AccessibilityNodeInfo nodeInfo) {

        for (int index = 0; index < nodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(index);
            if (childNodeInfo.getClassName().equals(Constants.CLASS_EDITTEXT)) {
                MyLog.e(TAG, "find edit text");

                Message message = handler.obtainMessage(Constants.HANDLE_AUTO_INPUT, childNodeInfo);
                message.sendToTarget();
                return;

            } else {
                CharSequence className = nodeInfo.getClassName();
                if (className.equals(Constants.CLASS_LINEARLAYOUT) ||
                        className.equals(Constants.CLASS_RELATIVELAYOUT) ||
                        className.equals(Constants.CLASS_FRAMELAYOUT)) {
                    findEditTExtNodeInfo(childNodeInfo);
                }
            }

        }
    }

    private void findButtonNodeInfo(AccessibilityNodeInfo nodeInfo) {

        for (int index = 0; index < nodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(index);
            if (childNodeInfo.getClassName().equals(Constants.CLASS_BUTTON)) {
                MyLog.e(TAG, "find button");
                Message message = handler.obtainMessage(Constants.HANDLE_AUTO_SEND, childNodeInfo);
                message.sendToTarget();
                return;

            } else {
                CharSequence className = nodeInfo.getClassName();
                if (className.equals(Constants.CLASS_LINEARLAYOUT) ||
                        className.equals(Constants.CLASS_RELATIVELAYOUT) ||
                        className.equals(Constants.CLASS_FRAMELAYOUT)) {
                    findButtonNodeInfo(childNodeInfo);
                }
            }

        }
    }


    private void findNodeInfo(AccessibilityNodeInfo nodeInfo, int type) {
        MyLog.e(TAG, "type:" + type);

        for (int index = 0; index < nodeInfo.getChildCount(); index++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(index);
            if (type == Constants.NODEINFO_EDITTEXT
                    && childNodeInfo.getClassName().equals(Constants.CLASS_EDITTEXT)) {
                MyLog.e(TAG, "find edit text");

                Message message = handler.obtainMessage(Constants.HANDLE_AUTO_INPUT, childNodeInfo);
                message.sendToTarget();
                return;

            } else if (type == Constants.NODEINFO_BUTTON
                    && childNodeInfo.getClassName().equals(Constants.CLASS_BUTTON)) {
                MyLog.e(TAG, "find button");
                Message message = handler.obtainMessage(Constants.HANDLE_AUTO_SEND, childNodeInfo);
                message.sendToTarget();
                return;

            } else {
                CharSequence className = nodeInfo.getClassName();
                if (className.equals(Constants.CLASS_LINEARLAYOUT) ||
                        className.equals(Constants.CLASS_RELATIVELAYOUT) ||
                        className.equals(Constants.CLASS_FRAMELAYOUT)) {
                    findNodeInfo(childNodeInfo, type);
                }
            }

        }
    }


    //发送广播
    private void sendServiceStateBroadcast(int serviceState) {
        Intent intent = new Intent();
        intent.putExtra(Constants.MSG_SERVICE, serviceState);
        intent.setAction(Constants.ACTION_MSG_SERVICE);
        sendBroadcast(intent);
    }

    @Override
    public void onInterrupt() {
        sendServiceStateBroadcast(Constants.MSG_SERVICE_OFF);
        ToastUtil.showShortToast(R.string.envelope_service_interrupt);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        MyLog.e(TAG, "开启");

        sendServiceStateBroadcast(Constants.MSG_SERVICE_ON);

        initSystemConfig();

        initAccessibilityServiceInfo();

        ToastUtil.showShortToast(R.string.envelope_service_start);
    }

    private void initSystemConfig() {
        //获取电源管理器对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        //初始化一个键盘锁管理器对象
        kl = km.newKeyguardLock("unLock");
        //初始化音频
        player = MediaPlayer.create(this, R.raw.lowbattery);
    }

    private void initAccessibilityServiceInfo() {
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.flags = AccessibilityServiceInfo.DEFAULT;
        serviceInfo.packageNames = new String[]{Constants.PKG_QQ, Constants.PKG_WECHAT};
        serviceInfo.notificationTimeout = 100;
        setServiceInfo(serviceInfo);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.e(TAG, "关闭");
        wakeAndUnlock(false);
        sendServiceStateBroadcast(Constants.MSG_SERVICE_OFF);
        ToastUtil.showShortToast(R.string.envelope_service_end);
    }
}