package name.monwf.miuil10n;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import name.monwf.miuil10n.utils.Helpers;
import name.monwf.miuil10n.utils.ResourceHooks;
import name.monwf.miuil10n.utils.Helpers.MethodHook;


public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private ResourceHooks resHooks;

    public void initZygote(StartupParam startParam) {
        resHooks = new ResourceHooks();
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;

        MethodHook statInitHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
            XposedHelpers.callStaticMethod(findClass("com.xiaomi.stat.MiStat", lpparam.classLoader), "setStatisticEnabled", false);
            Helpers.findAndHookMethodSilently("com.xiaomi.stat.MiStat", lpparam.classLoader, "setStatisticEnabled", boolean.class, XC_MethodReplacement.DO_NOTHING);
            }
        };
        if (!pkg.equals("android")) {
            Helpers.findAndHookMethodSilently("com.xiaomi.onetrack.OneTrack", lpparam.classLoader, "isDisable", XC_MethodReplacement.returnConstant(true));
            Helpers.findAndHookMethodSilently("com.xiaomi.stat.MiStat", lpparam.classLoader, "initialize", Context.class, String.class, String.class, boolean.class, statInitHook);
        }

        if (!pkg.equals("com.android.systemui")) {
            Helpers.findAndHookMethodSilently("com.xiaomi.stat.MiStat", lpparam.classLoader, "initialize", Context.class, String.class, String.class, boolean.class, String.class, statInitHook);
        }

        if (pkg.equals("com.android.contacts")
                || pkg.equals("com.android.incallui")
                || pkg.equals("com.miui.weather2")
                || pkg.equals("com.android.deskclock")
                || pkg.equals("com.android.thememanager")
                || pkg.equals("com.miui.yellowpage")
                || pkg.equals("com.miui.personalassistant")
                || pkg.equals("com.android.calendar")
        ) {
            Class<?> classBuild = XposedHelpers.findClass("miui.os.Build", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(classBuild, "IS_INTERNATIONAL_BUILD", false);
            XposedHelpers.setStaticBooleanField(classBuild, "IS_GLOBAL_BUILD", false);
        }
        if (pkg.equals("com.android.calendar")) {
            resHooks.setObjectReplacement(pkg, "bool", "is_greater_china", true);
            resHooks.setObjectReplacement(pkg, "bool", "is_mainland_china", true);
        }
        else if (pkg.equals("com.android.thememanager")) {
//            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//            Settings.Global.putString(mContext.getContentResolver(), "passport_ad_status", "OFF");
            Helpers.findAndHookMethodSilently("com.android.thememanager.basemodule.ad.model.AdInfoResponse", lpparam.classLoader, "getAdInfo", boolean.class, XC_MethodReplacement.returnConstant(null));
            Helpers.findAndHookMethodSilently("com.android.thememanager.basemodule.ad.model.AdInfoResponse", lpparam.classLoader, "checkAndGetAdInfo", String.class, boolean.class, XC_MethodReplacement.returnConstant(null));
        }
        else if (pkg.equals("com.miui.securitycenter")) {
            Helpers.findAndHookMethod("com.miui.permcenter.privacymanager.i", lpparam.classLoader, "l", XC_MethodReplacement.returnConstant(2));
            this.AppInfoHook(lpparam);
        }
        else if (pkg.equals("com.android.mms")) {
            Helpers.findAndHookMethod("miui.provider.ExtraTelephony", lpparam.classLoader, "getSmsURLScanResult", Context.class, String.class, String.class, XC_MethodReplacement.returnConstant(-1));
        }
        else if (pkg.equals("android")) {
            Helpers.findAndHookMethod("com.android.server.notification.NotificationManagerServiceInjector", lpparam.classLoader, "isAllowLocalNotification", XC_MethodReplacement.returnConstant(true));
        }
        else if (pkg.equals("com.android.systemui")) {
            this.MobileIconStateHook(lpparam);
        }
        else if (pkg.equals("com.miui.home")) {
            this.LauncherHook(lpparam);
            this.FSGesturesHook(lpparam);
        }
        else if (pkg.equals("com.miui.packageinstaller")) {
            this.AppInfoDuringMiuiInstallHook(lpparam);
        }
    }

    private void LauncherHook(LoadPackageParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mLauncher"), "hideAppView");
            }
        };
        Helpers.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.AppsListFragment", lpparam.classLoader, "onClick", View.class, hook);
        Helpers.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.RecommendCategoryAppListFragment", lpparam.classLoader, "onClick", View.class, hook);
        Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "launch", "com.miui.home.launcher.ShortcutInfo", View.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(param.thisObject, "mHasLaunchedAppFromFolder");
                if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(param.thisObject, "closeFolder");
            }
        });

        Helpers.findAndHookMethod("com.miui.home.launcher.shortcuts.AppShortcutMenuItem", lpparam.classLoader, "getOnClickListener", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                final View.OnClickListener listener = (View.OnClickListener)param.getResult();
                param.setResult(new View.OnClickListener() {
                    public final void onClick(View view) {
                        listener.onClick(view);
                        Class<?> appCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.Application", lpparam.classLoader);
                        if (appCls == null) return;
                        Object launcher = XposedHelpers.callStaticMethod(appCls, "getLauncher");
                        if (launcher == null) return;
                        XposedHelpers.callMethod(launcher, "hideAppView");
                        XposedHelpers.callMethod(launcher, "closeFolder");
                    }
                });
            }
        });
    }

    private void AppInfoDuringMiuiInstallHook(LoadPackageParam lpparam) {
        Method[] methods = XposedHelpers.findMethodsByExactParameters(findClass("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader), void.class, String.class);
        if (methods.length == 0) {
            Helpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
            return;
        }
        for (Method method: methods)
            Helpers.hookMethod(method, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Activity act = (Activity)param.thisObject;
                    TextView version = act.findViewById(act.getResources().getIdentifier("install_version", "id", lpparam.packageName));
                    Field fPkgInfo = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), PackageInfo.class);
                    PackageInfo mPkgInfo = (PackageInfo)fPkgInfo.get(param.thisObject);
                    if (version == null || mPkgInfo == null) return;

                    TextView source = act.findViewById(act.getResources().getIdentifier("install_source", "id", lpparam.packageName));
                    source.setGravity(Gravity.CENTER_HORIZONTAL);
                    source.setText(mPkgInfo.packageName);

                    PackageInfo mAppInfo = null;
                    try {
                        mAppInfo = act.getPackageManager().getPackageInfo(mPkgInfo.packageName, 0);
                    } catch (Throwable ignore) {}

                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append("版本名称").append(":\t\t");
                    if (mAppInfo != null) builder.append(mAppInfo.versionName).append("  ➟  ");
                    builder.append(mPkgInfo.versionName).append("\n");
                    builder.append("版本代码").append(":\t\t");
                    if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.getLongVersionCode())).append("  ➟  ");
                    builder.append(String.valueOf(mPkgInfo.getLongVersionCode())).append("\n");
                    builder.append("支持SDK").append(":\t\t");
                    if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mAppInfo.applicationInfo.targetSdkVersion)).append("  ➟  ");
                    builder.append(String.valueOf(mPkgInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mPkgInfo.applicationInfo.targetSdkVersion));

                    version.setGravity(Gravity.CENTER_HORIZONTAL);
                    version.setSingleLine(false);
                    version.setMaxLines(10);
                    version.setText(builder);
                    version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.09f);
                }
            });
    }

    private void AppInfoHook(LoadPackageParam lpparam) {
        Class<?> amaCls = XposedHelpers.findClassIfExists("com.miui.appmanager.AMAppInfomationActivity", lpparam.classLoader);
        if (amaCls == null) {
            Helpers.log("AppInfoHook", "Cannot find activity class!");
            return;
        }

        Helpers.findAndHookMethod(amaCls, "onCreate", Bundle.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Activity act = (Activity)param.thisObject;
                        PackageInfo mLastPackageInfo;
                        Object frag;

                        try {
                            frag = XposedHelpers.callMethod(XposedHelpers.callMethod(param.thisObject, "getSupportFragmentManager"), "a", android.R.id.content);
                            if (frag == null) {
                                Helpers.log("AppInfoHook", "Unable to find fragment");
                                return;
                            }
                            Field piField = XposedHelpers.findFirstFieldByExactType(frag.getClass(), PackageInfo.class);
                            mLastPackageInfo = (PackageInfo)piField.get(frag);
                            Method[] addPref = XposedHelpers.findMethodsByExactParameters(frag.getClass(), void.class, String.class, String.class, String.class);
                            if (mLastPackageInfo == null || addPref.length == 0) {
                                Helpers.log("AppInfoHook", "Unable to find field/class/method in SecurityCenter to hook");
                                return;
                            } else {
                                addPref[0].setAccessible(true);
                            }
                            addPref[0].invoke(frag, "apk_filename", "APK文件名", mLastPackageInfo.applicationInfo.sourceDir);
                            addPref[0].invoke(frag, "data_path", "数据路径", mLastPackageInfo.applicationInfo.dataDir);
                            addPref[0].invoke(frag, "app_uid", "用户标识符", String.valueOf(mLastPackageInfo.applicationInfo.uid));
                            addPref[0].invoke(frag, "target_sdk", "目标SDK", String.valueOf(mLastPackageInfo.applicationInfo.targetSdkVersion));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                            return;
                        }

                        XposedBridge.hookAllMethods(frag.getClass(), "onPreferenceTreeClick", new MethodHook() {
                            @Override
                            protected void before(final MethodHookParam param) throws Throwable {
                                String key = (String)XposedHelpers.callMethod(param.args[0], "getKey");
                                String title = (String)XposedHelpers.callMethod(param.args[0], "getTitle");
                                switch (key) {
                                    case "apk_filename":
                                        ((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.sourceDir));
                                        Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
                                        param.setResult(true);
                                        break;
                                    case "data_path":
                                        ((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.dataDir));
                                        Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
                                        param.setResult(true);
                                        break;
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    private void FSGesturesHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "usingFsGesture", XC_MethodReplacement.returnConstant(true));

        Helpers.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "createAndAddNavStubView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR");
                if (!fsg) param.setResult(null);
            }
        });

        Helpers.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "updateFsgWindowState", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR");
                if (fsg) return;

                Object mNavStubView = XposedHelpers.getObjectField(param.thisObject, "mNavStubView");
                Object mWindowManager = XposedHelpers.getObjectField(param.thisObject, "mWindowManager");
                if (mWindowManager != null && mNavStubView != null) {
                    XposedHelpers.callMethod(mWindowManager, "removeView", mNavStubView);
                    XposedHelpers.setObjectField(param.thisObject, "mNavStubView", null);
                }
            }
        });

        Helpers.findAndHookMethodSilently("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.classLoader, "getGlobalBoolean", ContentResolver.class, String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!"force_fsg_nav_bar".equals(param.args[1])) return;

                for (StackTraceElement el: Thread.currentThread().getStackTrace())
                    if ("com.miui.home.recents.BaseRecentsImpl".equals(el.getClassName())) {
                        XposedHelpers.setAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR", param.getResult());
                        param.setResult(true);
                        return;
                    }
            }
        });
    }

    private void MobileIconStateHook(LoadPackageParam lpparam) {
        MethodHook mobileIconHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean isWifi = (boolean) XposedHelpers.getObjectField(param.args[0], "wifiAvailable");
                ImageView hdIcon = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mVolte");
                hdIcon.setVisibility(View.GONE);
                hdIcon = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mSmallHd");
                if (isWifi) {
                    hdIcon.setVisibility(View.VISIBLE);
                }
                else {
                    hdIcon.setVisibility(View.GONE);
                }
            }
        };
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "initViewState",
                "com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState", mobileIconHook
        );
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState",
                "com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState", mobileIconHook
        );
    }
}