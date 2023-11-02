package moe.misty.elinkdisablemod;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MainHook implements IXposedHookLoadPackage {
    boolean checkService(Intent serviceIntent) {
        ComponentName component = serviceIntent.getComponent();
        if (component == null) {
            return false;
        }
        if (component.getClassName().contains("ElinkAutoTestService")) {
            return true;
        }
        return false;
    }

    public void handleBroadcastHook(LoadPackageParam lpparam) {
        if (!lpparam.packageName.contains("com.android"))
            return;

        XposedBridge.log("[ElinkDisabled] Entered system package: " + lpparam.packageName);

        ClassLoader loader = lpparam.classLoader;
        Class clazz = null;

        try {
            clazz = XposedHelpers.findClass("android.telephony.TelephonyManager", loader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("[ElinkDisableMod] $$$ class not found: android.telephony.TelephonyManager");
        }
        if (clazz != null) {
            XposedHelpers.findAndHookMethod(clazz, "set_led_alloff", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("[ElinkDisableMod] intercept set_led_alloff  :)");
                    param.setResult(null);
                }
            });
        }
        clazz = null;

        try {
            clazz = XposedHelpers.findClass("android.content.ContextWrapper", loader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("[ElinkDisableMod] $$$ class not found: android.content.ContextWrapper");
        }

        if(clazz != null) {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "attachBaseContext", Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Context base = (Context)param.args[0];

                            XposedHelpers.findAndHookMethod(
                                    base.getClass(),
                                    "sendBroadcast", Intent.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            Intent service = (Intent)param.args[0];
//                                            XposedBridge.log("[ElinkDisableMod] $$$ ContextWrapper sendBroadcast: " + service.getAction());
                                            if (service.getAction().equals("android.intent.action.REBOOT")) {
                                                Context context = (Context) param.thisObject;
                                                XposedBridge.log("[ElinkDisableMod] intercept REBOOT intent in " + context.getPackageName());
                                                XposedBridge.log(new Exception());
                                                param.setResult(null);
                                            }
                                        }
                                    }
                            );
                        }
                    }
            );
            clazz = null;
        }
    }
    public void handleSystemServer(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android"))
            return;

        XposedBridge.log("[ElinkDisabled] Entered system_server!");

        ClassLoader loader = lpparam.classLoader;
        Class clazz = null;
        try {
            clazz = XposedHelpers.findClass("android.content.ContextWrapper", loader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("[ElinkDisableMod] $$$ class not found: android.content.ContextWrapper");
        }

        if(clazz != null) {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "attachBaseContext", Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Context base = (Context)param.args[0];

                            XposedHelpers.findAndHookMethod(
                                    base.getClass(),
                                    "startService", Intent.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            Intent service = (Intent)param.args[0];
//                                            XposedBridge.log("[ElinkDisableMod] $$$ ContextWrapper startService: " + service.getAction());
                                            if (checkService(service)) {
                                                Context context = (Context) param.thisObject;
                                                XposedBridge.log("[ElinkDisableMod] intercept ElinkAutoTest in " + context.getPackageName());
                                                param.setResult(null);
                                            }
                                        }
                                    }
                            );

                            XposedHelpers.findAndHookMethod(
                                    base.getClass(),
                                    "startServiceAsUser", Intent.class, UserHandle.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            Intent service = (Intent)param.args[0];
//                                            XposedBridge.log("[ElinkDisableMod] $$$ ContextWrapper startServiceAsUser: " + service.getAction());
                                            if (checkService(service)) {
                                                Context context = (Context) param.thisObject;
                                                XposedBridge.log("[ElinkDisableMod] intercept ElinkAutoTest in " + context.getPackageName());
                                                param.setResult(null);
                                            }
                                        }
                                    }
                            );
                        }
                    }
            );
            clazz = null;
        }
    }
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        handleBroadcastHook(lpparam);
        handleSystemServer(lpparam);
//        XposedBridge.log("[ElinkDisabled] Loaded app: " + lpparam.packageName);
//        if (!lpparam.packageName.equals("com.android.systemui"))
//            return;

        // don't know why, xposed says can't find ElinkAutoTestService class, even if I run the hook on all packages

//        try {
//            findAndHookMethod("com.android.systemui.ElinkAutoTestService", lpparam.classLoader, "onCreate", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("[ElinkDisableMod] Overrided Elink onCreate!");
//                    param.setResult(null);
//                }
//            });
//
//            findAndHookMethod("com.android.systemui.ElinkAutoTestService", lpparam.classLoader, "onBind", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("[ElinkDisableMod] Overrided Elink onBind!");
//                    param.setResult(null);
//                }
//            });
//
//            findAndHookMethod("com.android.systemui.ElinkAutoTestService", lpparam.classLoader, "onStartCommand", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("[ElinkDisableMod] Overrided Elink onStartCommand!");
//                    param.setResult(START_STICKY_COMPATIBILITY);
//                }
//            });
//
//            findAndHookMethod("com.android.systemui.ElinkAutoTestService", lpparam.classLoader, "onUnbind", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("[ElinkDisableMod] Overrided Elink onUnbind!");
//                    param.setResult(false);
//                }
//            });
//
//
//            findAndHookMethod("com.android.systemui.ElinkAutoTestService", lpparam.classLoader, "onDestroy", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("[ElinkDisableMod] Overrided Elink onDestroy!");
//                    param.setResult(null);
//                }
//            });
//        } catch (Exception e) {
//            return;
//        }
//        XposedBridge.log("[ElinkDisableMod] Hooked Elink in " + lpparam.packageName);
    }
}