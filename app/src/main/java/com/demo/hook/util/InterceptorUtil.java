package com.demo.hook.util;

/**
 * Created by guoxiaodong on 2020/2/28 16:08
 */
public class InterceptorUtil {
    public static final InterceptorUtil instance = new InterceptorUtil();
    private boolean isIntercept;

    private InterceptorUtil() {
    }

    public boolean isIntercept() {
        return isIntercept;
    }

    public boolean setInterceptor() {
        isIntercept = !isIntercept;
        return isIntercept;
    }
}
