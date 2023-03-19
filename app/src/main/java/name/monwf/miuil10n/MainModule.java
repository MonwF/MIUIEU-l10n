package name.monwf.miuil10n;

import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.monwf.miuil10n.utils.Helpers;
import name.monwf.miuil10n.utils.Helpers.MethodHook;
import name.monwf.miuil10n.utils.ResourceHooks;


public class MainModule implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;

        if (!pkg.equals("com.android.systemui") && !pkg.equals("android")) {
            Helpers.hookAllMethodsSilently("com.xiaomi.onetrack.OneTrack", lpparam.classLoader, "track", XC_MethodReplacement.DO_NOTHING);
            Helpers.hookAllMethodsSilently("com.xiaomi.onetrack.OneTrack", lpparam.classLoader, "trackPluginEvent", XC_MethodReplacement.DO_NOTHING);
            Helpers.findAndHookMethodSilently("com.xiaomi.onetrack.OneTrack", lpparam.classLoader, "isDisable", XC_MethodReplacement.returnConstant(true));
        }

        if (pkg.equals("com.android.contacts")
                || pkg.equals("com.android.incallui")
                || pkg.equals("com.miui.weather2")
                || pkg.equals("com.android.deskclock")
                || pkg.equals("com.android.thememanager")
                || pkg.equals("com.miui.yellowpage")
                || pkg.equals("com.miui.personalassistant")
                || pkg.equals("com.android.calendar")
                || pkg.equals("com.android.settings")
                || pkg.equals("com.android.phone")
                || pkg.equals("com.miui.player")
        ) {
            Class<?> classBuild = XposedHelpers.findClass("miui.os.Build", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(classBuild, "IS_INTERNATIONAL_BUILD", false);
            XposedHelpers.setStaticBooleanField(classBuild, "IS_GLOBAL_BUILD", false);
        }
        if (pkg.equals("com.android.calendar")) {
            ResourceHooks resHooks = new ResourceHooks();
            resHooks.setObjectReplacement(pkg, "bool", "is_greater_china", true);
            resHooks.setObjectReplacement(pkg, "bool", "is_mainland_china", true);
        }
        else if (pkg.equals("com.android.thememanager")) {
//            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//            Settings.Global.putString(mContext.getContentResolver(), "passport_ad_status", "OFF");
            Helpers.findAndHookMethodSilently("com.android.thememanager.basemodule.ad.model.AdInfoResponse", lpparam.classLoader, "getAdInfo", boolean.class, XC_MethodReplacement.returnConstant(null));
            Helpers.findAndHookMethodSilently("com.android.thememanager.basemodule.ad.model.AdInfoResponse", lpparam.classLoader, "checkAndGetAdInfo", String.class, boolean.class, XC_MethodReplacement.returnConstant(null));
        }
        else if (pkg.equals("com.android.mms")) {
            Helpers.findAndHookMethod("miui.provider.ExtraTelephony", lpparam.classLoader, "getSmsURLScanResult",
                    Context.class, String.class, String.class, XC_MethodReplacement.returnConstant(0));
        }
//        else if (pkg.equals("android")) {
//            Helpers.findAndHookMethod("com.android.server.notification.NotificationManagerServiceImpl", lpparam.classLoader, "isDeniedLocalNotification",
//                    AppOpsManager.class, Notification.class, int.class, String.class, XC_MethodReplacement.returnConstant(false));
//        }
    }
}