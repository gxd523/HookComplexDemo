package com.demo.hook.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.demo.hook.activity.LoginActivity;
import com.demo.hook.activity.ProxyActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 专门处理绕过AMS检测，让LoginActivity可以正常通过
 */
public class HookUtil {
    private static final String TARGET_INTENT = "intent";

    /**
     * 由于Activity跳转时要经过IActivityManager的startActivity()，因此我们就是要在此方法调用前给传入的参数intent添加额外参数
     * 1、拿到ActivityManager中的IActivityManager
     * 2、拿到ActivityManager中的IActivityManagerSingleton
     * 3、动态代理IActivityManager中的startActivity()，并生成动态代理对象
     * 4、将IActivityManagerSingleton里面的IActivityManager换成动态代理生成的代理类
     * 适配API29以下
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityManager(final Context context) throws Exception {
        final Object IActivityManager = getIActivityManagerInstance();
        final Object IActivityManagerSingleton = getIActivityManagerSingletonInstance();

        if (IActivityManagerSingleton == null || IActivityManager == null) {
            throw new IllegalStateException("实在是没有检测到这种系统，需要对这种系统单独处理...");
        }

        Object IActivityManagerProxy = Proxy.newProxyInstance(
                context.getClassLoader(),
                new Class[]{Class.forName("android.app.IActivityManager")},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
                        // int startActivity(in IApplicationThread caller, in String callingPackage, in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho, int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options);
                        if ("startActivity".equals(method.getName())) {
                            // 把不能经过检测的LoginActivity替换成能够经过检测的ProxyActivity
                            Intent proxyIntent = new Intent(context, ProxyActivity.class);
                            // 把目标的LoginActivity取出来携带过去
                            Intent intent = (Intent) args[2];
                            proxyIntent.putExtra(HookUtil.TARGET_INTENT, intent);
                            args[2] = proxyIntent;
                        }
                        return method.invoke(IActivityManager, args);
                    }
                });

        Class singletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        // 把系统里面的 IActivityManager 换成 我们自己写的动态代理
        mInstanceField.set(IActivityManagerSingleton, IActivityManagerProxy);
    }

    /**
     * 添加完额外参数，接下来，由于跳转Activity最终都是ActivityThread中的Handler.handleMessage()中处理的，因此我们只要获取这个Handler实例，并添加我们自己的Handler.Callback实现，就能够跳过handleMessage()执行我们的跳转
     * 此方法适用于API29以下
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityThreadHandlerCallback(Context context) throws Exception {
        Object activityThread = getActivityThreadInstance();

        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Field handlerField = activityThreadClass.getDeclaredField("mH");
        handlerField.setAccessible(true);
        Object handler = handlerField.get(activityThread);
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        Handler.Callback callback = generateHandlerCallback(context);
        mCallbackField.set(handler, callback);
    }

    /**
     * IActivityManager实例也就是IActivityManager.aidl接口对应的实例对象
     */
    private static Object getIActivityManagerInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object IActivityManager;
        if (Build.VERSION.SDK_INT > 25) {
            Class activityManagerClass = Class.forName("android.app.ActivityManager");
            IActivityManager = activityManagerClass.getMethod("getService").invoke(null);
        } else {
            Class activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            IActivityManager = getDefaultMethod.invoke(null);
        }
        return IActivityManager;
    }

    /**
     * ActivityManager里的成员变量Singleton<IActivityManager>
     */
    private static Object getIActivityManagerSingletonInstance() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Object IActivityManagerSingleton;

        if (Build.VERSION.SDK_INT > 25) {
            Class activityManagerClass = Class.forName("android.app.ActivityManager");
            Field IActivityManagerSingletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            IActivityManagerSingletonField.setAccessible(true);
            IActivityManagerSingleton = IActivityManagerSingletonField.get(null);
        } else {
            Class activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            IActivityManagerSingleton = gDefaultField.get(null);
        }
        return IActivityManagerSingleton;
    }

    private static Object getActivityThreadInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Object activityThread;
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        if (Build.VERSION.SDK_INT > 25) {
            activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } else {
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            activityThread = sCurrentActivityThreadField.get(null);
        }
        return activityThread;
    }

    /**
     * 替换Handler的callback成员变量
     */
    private static Handler.Callback generateHandlerCallback(final Context context) {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (Build.VERSION.SDK_INT > 25) {
                    if (msg.what == 159) {// EXECUTE_TRANSACTION
                        Object clientTransaction = msg.obj;

                        try {
                            Class clientTransactionClass = Class.forName("android.app.servertransaction.ClientTransaction");
                            // private List<ClientTransactionItem> mActivityCallbacks;
                            Field mActivityCallbacksField = clientTransactionClass.getDeclaredField("mActivityCallbacks");
                            mActivityCallbacksField.setAccessible(true);
                            // List<LaunchActivityItem>
                            List mActivityCallbacks = (List) mActivityCallbacksField.get(clientTransaction);

                            // 高版本存在多次权限检测，所以添加需要判断
                            if (mActivityCallbacks == null || mActivityCallbacks.size() == 0) {
                                return false;
                            }

                            // LaunchActivityItem
                            Object launchActivityItem = mActivityCallbacks.get(0);
                            Class launchActivityItemClass = Class.forName("android.app.servertransaction.LaunchActivityItem");

                            if (!launchActivityItemClass.isInstance(launchActivityItem)) {
                                return false;
                            }

                            revertIntent(context, launchActivityItem, launchActivityItemClass.getDeclaredField("mIntent"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (msg.what == 100) {
                        Object mActivityClientRecord = msg.obj;
                        try {
                            revertIntent(context, mActivityClientRecord, mActivityClientRecord.getClass().getDeclaredField("intent"));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        };
    }

    private static void revertIntent(Context context, Object obj, Field intentField) throws IllegalAccessException {
        intentField.setAccessible(true);
        Intent proxyIntent = (Intent) intentField.get(obj);
        Intent targetIntent = proxyIntent.getParcelableExtra(HookUtil.TARGET_INTENT);
        if (targetIntent != null) {
            if (LoginUtil.instance.isNeedLogin()) {
                ComponentName componentName = new ComponentName(context, LoginActivity.class);
                targetIntent.putExtra(LoginActivity.EXTRA_INTENT, targetIntent.getComponent().getClassName());
                targetIntent.setComponent(componentName);
            } else {
                targetIntent.setComponent(targetIntent.getComponent());
            }
            intentField.set(obj, targetIntent);
        }
    }
}
