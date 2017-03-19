package com.custmorhelper.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by Administrator on 2017/2/8.
 */
public class ScreenUtil {

    /** 获取屏幕的宽度 */
    public final static int getWindowsWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

}
