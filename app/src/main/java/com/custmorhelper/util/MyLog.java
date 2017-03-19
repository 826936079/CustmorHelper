package com.custmorhelper.util;


/**日志管理
 *
 *
 * [统一管理日志，包括各种级别的日志]
 */
public class MyLog {

    private static final int LDBG_VERBOSE 	= 1;
    private static final int LDBG_DEBUG  	= 2;
    private static final int LDBG_INFO 		= 3;
    private static final int LDBG_WARN 		= 4;
    private static final int LDBG_ERROR  	= 5;
    @SuppressWarnings("unused")
    private static final int LDBG_ASSERT    = 6;

    private static final int LOG_FILE_LEVEL = LDBG_VERBOSE;

    /**
     * 控制日志开关
     */
    private static boolean LogSwitch = true;

    public static final String LOG_PREFIX = "---";

    /**
     * 打印verbose级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     */
    public static void v(String tag, String text)
    {
        if (LogSwitch) {
            android.util.Log.w(tag, LOG_PREFIX + getFunctionName() + text);
        }
        if (LDBG_VERBOSE >= LOG_FILE_LEVEL) {
        }
    }

    /**
     *
     * 打印debug级别的日志<BR>
     * [功能详细描述]
     *
     * @param obj tag标记，传入当前调用的类对象即可，方法会转化为该对象对应的类名
     * @param text 日志内容
     */
    public static void d(Object obj, String text)
    {
        if (LogSwitch) {
            if (obj != null) {
                d(obj.getClass().getSimpleName(), LOG_PREFIX + getFunctionName() + text);
            }
        }
    }

    /**
     * 打印debug级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     */
    public static void d(String tag, String text)
    {
        if (LogSwitch) {
            android.util.Log.d(tag, LOG_PREFIX + getFunctionName() + text);
        }
        if (LDBG_DEBUG >= LOG_FILE_LEVEL) {
        }
    }

    /**
     * 打印info级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     */
    public static void i(String tag, String text)
    {
        if (LogSwitch) {
            android.util.Log.i(tag, LOG_PREFIX + getFunctionName() + text);
        }
        if (LDBG_INFO >= LOG_FILE_LEVEL) {
        }
    }

    /**
     * 打印warn级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     */
    public static void w(String tag, String text)
    {
        if (LogSwitch) {
            android.util.Log.w(tag, LOG_PREFIX + getFunctionName() + text);
        }
        if (LDBG_WARN >= LOG_FILE_LEVEL) {
        }
    }

    /**
     * 打印warn级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     * @param throwable 异常信息
     */
    public static void w(String tag, String text, Throwable throwable)
    {
        if (LogSwitch) {
            android.util.Log.w(tag, LOG_PREFIX + getFunctionName() + text, throwable);
        }
        if (LDBG_WARN >= LOG_FILE_LEVEL) {
        }
    }

    /**
     * 打印error级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     */
    public static void e(String tag, String text)
    {
        if (LogSwitch) {
            android.util.Log.e(tag, LOG_PREFIX + getFunctionName() + text);
        }
        if (LDBG_ERROR >= LOG_FILE_LEVEL) {
        }
    }

    /**
     * 打印error级别的日志
     *
     * @param tag 标记
     * @param text 日志内容
     * @param throwable 异常信息
     */
    public static void e(String tag, String text, Throwable throwable)
    {
        if (LogSwitch) {
            android.util.Log.e(tag, LOG_PREFIX + getFunctionName() + text, throwable);
        }
        if (LDBG_ERROR >= LOG_FILE_LEVEL) {
        }
    }


    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(MyLog.class.getName())) {
                continue;
            }
            return "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }

}
