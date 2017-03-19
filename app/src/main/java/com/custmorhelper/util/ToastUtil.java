package com.custmorhelper.util;


import android.widget.Toast;

import com.custmorhelper.manager.GlobleManager;

public class ToastUtil {

    private static Toast toast = null;
     

    public static void showShortToast(String content){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(GlobleManager.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }
    public static void showLongToast(String content){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(GlobleManager.getContext(), content, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showShortToast(int resId){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(GlobleManager.getContext(), GlobleManager.getContext().getString(resId), Toast.LENGTH_SHORT);
        toast.show();
    }
    public static void showLongToast(int resId){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(GlobleManager.getContext(),GlobleManager.getContext().getString(resId), Toast.LENGTH_LONG);
        toast.show();
    }


}
