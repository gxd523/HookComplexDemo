package com.demo.hook.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.demo.hook.activity.ProxyActivity;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

/**
 * 专门处理绕过AMS检测，让InterceptorActivity可以正常通过
 */
public class HookUtil {
    public static final String PLUGIN_FILE_NAME = "plugin.apk";
    private static final String TARGET_INTENT = "intent";

    /**
     * 将plugin.apk的dex转为classLoader，并获取element数组，合并到应用的dexPathList的element数组中，最终于应用的dex合并
     */
    public static void mergeDex(Context context) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        File pluginApk = new File(context.getExternalFilesDir(null), PLUGIN_FILE_NAME);
        if (!pluginApk.exists()) {
            Log.e("gxd", "没找到插件apk..." + pluginApk.getAbsolutePath());
            return;
        }

        BaseDexClassLoader pluginClassLoader = new DexClassLoader(
                pluginApk.getAbsolutePath(),
                context.getExternalFilesDir("apkdex").getAbsolutePath(),
                null,
                context.getClassLoader()
        );
        ClassLoader classLoader = context.getClassLoader();

        // 获取获取应用和plugin的dexPathList对象
        Field pathListFiled = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListFiled.setAccessible(true);

        Object dexPathList = pathListFiled.get(classLoader);
        Object pluginDexPathList = pathListFiled.get(pluginClassLoader);

        // 获取获取应用和plugin的element数组对象
        Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
        Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        Object pluginElementArray = dexElementsField.get(pluginDexPathList);
        Object elementArray = dexElementsField.get(dexPathList);

        // 合并应用和plugin的element数组
        int pluginLength = Array.getLength(pluginElementArray);
        int length = Array.getLength(elementArray);
        int totalLength = pluginLength + length;

        Class<?> elementClass = elementArray.getClass().getComponentType();
        Object mergeElementArray = Array.newInstance(elementClass, totalLength);

        for (int i = 0; i < totalLength; i++) {
            if (i < pluginLength) {// 先把plugin的element元素放入新element数组，再放应用的element元素
                Array.set(mergeElementArray, i, Array.get(pluginElementArray, i));
            } else {
                Array.set(mergeElementArray, i, Array.get(elementArray, i - pluginLength));
            }
        }

        // 最后将新的element数组替换应用dexPathList中的element数组
        dexElementsField.set(dexPathList, mergeElementArray);
    }

    /**
     * 由于Activity跳转时要经过IActivityManager的startActivity()，因此我们就是要在此方法调用前给传入的参数intent添加额外参数
     * 1、拿到ActivityManager中的IActivityManager
     * 2、拿到ActivityManager中的IActivityManagerSingleton
     * 3、动态代理IActivityManager中的startActivity()，并生成动态代理对象
     * 4、将IActivityManagerSingleton里面的IActivityManager换成动态代理生成的代理类
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityManager(final Context context) throws Exception {
        final Object iActivityManager = getIActivityManagerInstance();
        final Object iActivityManagerSingleton = getIActivityManagerSingletonInstance();

        if (iActivityManagerSingleton == null || iActivityManager == null) {
            throw new IllegalStateException("实在是没有检测到这种系统，需要对这种系统单独处理...");
        }

        Class<?> iActivityManagerInterface;
        if (Build.VERSION.SDK_INT >= 29) {
            iActivityManagerInterface = Class.forName("android.app.IActivityTaskManager");
        } else {
            iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        }

        Object IActivityManagerProxy = Proxy.newProxyInstance(
                context.getClassLoader(),
                new Class[]{iActivityManagerInterface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
                        // int startActivity(in IApplicationThread caller, in String callingPackage, in Intent intent, in String resolvedType, in
                        // IBinder resultTo, in String resultWho, int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options);
                        if ("startActivity".equals(method.getName())) {
                            // 把不能经过检测的TargetActivity替换成能够经过检测的ProxyActivity
                            Intent proxyIntent = new Intent(context, ProxyActivity.class);
                            // 把目标的TargetActivity取出来携带过去
                            Intent intent = (Intent) args[2];
                            proxyIntent.putExtra(HookUtil.TARGET_INTENT, intent);
                            args[2] = proxyIntent;
                        }
                        return method.invoke(iActivityManager, args);
                    }
                });

        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        // 把系统里面的 IActivityManager 换成 我们自己写的动态代理
        mInstanceField.set(iActivityManagerSingleton, IActivityManagerProxy);
    }

    /**
     * 添加完额外参数，接下来，由于跳转Activity最终都是ActivityThread中的Handler.handleMessage()中处理的，因此我们只要获取这个Handler实例，并添加我们自己的Handler.Callback实现，就能够跳过handleMessage()
     * 执行我们的跳转
     * 此方法适用于API29以下
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityThreadHandlerCallback(Context context) throws Exception {
        Object activityThread = getActivityThreadInstance();

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
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
    private static Object getIActivityManagerInstance()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object iActivityManager;
        if (Build.VERSION.SDK_INT >= 29) {
            Class<?> activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");
            iActivityManager = activityTaskManagerClass.getMethod("getService").invoke(null);
        } else if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            iActivityManager = activityManagerClass.getMethod("getService").invoke(null);
        } else {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            iActivityManager = getDefaultMethod.invoke(null);
        }
        return iActivityManager;
    }

    /**
     * ActivityManager里的成员变量Singleton<IActivityManager>
     */
    private static Object getIActivityManagerSingletonInstance() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Object IActivityManagerSingleton;

        if (Build.VERSION.SDK_INT >= 29) {
            Class<?> activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");
            Field IActivityManagerSingletonField = activityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
            IActivityManagerSingletonField.setAccessible(true);
            IActivityManagerSingleton = IActivityManagerSingletonField.get(null);
        } else if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            Field IActivityManagerSingletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            IActivityManagerSingletonField.setAccessible(true);
            IActivityManagerSingleton = IActivityManagerSingletonField.get(null);
        } else {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            IActivityManagerSingleton = gDefaultField.get(null);
        }
        return IActivityManagerSingleton;
    }

    private static Object getActivityThreadInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {
        Object activityThread;
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
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
                try {
                    if (Build.VERSION.SDK_INT > 25 && msg.what == 159/*EXECUTE_TRANSACTION*/) {
                        Object clientTransaction = msg.obj;
                        Class<?> clientTransactionClass = Class.forName("android.app.servertransaction.ClientTransaction");
                        // private List<ClientTransactionItem> mActivityCallbacks;
                        Field mActivityCallbacksField = clientTransactionClass.getDeclaredField("mActivityCallbacks");
                        mActivityCallbacksField.setAccessible(true);
                        // List<LaunchActivityItem>
                        List<?> mActivityCallbacks = (List<?>) mActivityCallbacksField.get(clientTransaction);

                        // 高版本存在多次权限检测，所以添加需要判断
                        if (mActivityCallbacks == null || mActivityCallbacks.size() == 0) {
                            return false;
                        }

                        // LaunchActivityItem
                        Object launchActivityItem = mActivityCallbacks.get(0);
                        Class<?> launchActivityItemClass = Class.forName("android.app.servertransaction.LaunchActivityItem");

                        if (!launchActivityItemClass.isInstance(launchActivityItem)) {
                            return false;
                        }

                        revertIntent(context, launchActivityItem, launchActivityItemClass.getDeclaredField("mIntent"));
                    } else if (msg.what == 100) {
                        Object mActivityClientRecord = msg.obj;
                        revertIntent(context, mActivityClientRecord, mActivityClientRecord.getClass().getDeclaredField("intent"));
                    }
                } catch (Exception e) {
                    Log.e("gxd", "HookUtil.handleMessage-->", e);
                }
                return false;
            }
        };
    }

    /**
     * @param obj 含有intent成员变量的实例对象
     */
    private static void revertIntent(Context context, Object obj, Field intentField) throws IllegalAccessException, ClassNotFoundException {
        intentField.setAccessible(true);
        Intent proxyIntent = (Intent) intentField.get(obj);
        Intent targetIntent = proxyIntent.getParcelableExtra(HookUtil.TARGET_INTENT);
        if (targetIntent != null) {
            if (InterceptorUtil.instance.isIntercept()) {
                Class<?> InterceptorActivityClass = Class.forName("com.demo.plugin.InterceptorActivity");
                ComponentName componentName = new ComponentName(context, InterceptorActivityClass);
                targetIntent.putExtra("extraIntent", targetIntent.getComponent().getClassName());
                targetIntent.setComponent(componentName);
            } else {
                targetIntent.setComponent(targetIntent.getComponent());
            }
            intentField.set(obj, targetIntent);
        }
    }
}