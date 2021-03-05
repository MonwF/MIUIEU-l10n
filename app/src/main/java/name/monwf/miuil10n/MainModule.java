package name.monwf.miuil10n;

import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.monwf.miuil10n.utils.Helpers;
import name.monwf.miuil10n.utils.ResourceHooks;


public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	private ResourceHooks resHooks;

	public void initZygote(StartupParam startParam) {
		resHooks = new ResourceHooks();
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("com.android.contacts")
				|| pkg.equals("com.miui.weather2")
				|| pkg.equals("com.android.mms")
				|| pkg.equals("com.android.deskclock")
				|| pkg.equals("com.miui.yellowpage")
				|| pkg.equals("com.miui.securitycenter")
		) {
			Class<?> classBuild = XposedHelpers.findClass("miui.os.Build", lpparam.classLoader);
			XposedHelpers.setStaticBooleanField(classBuild, "IS_INTERNATIONAL_BUILD", false);
			XposedHelpers.setStaticBooleanField(classBuild, "IS_GLOBAL_BUILD", false);
		}
		else if (pkg.equals("com.android.calendar")) {
			resHooks.setObjectReplacement(pkg, "bool", "is_greater_china", true);
			resHooks.setObjectReplacement(pkg, "bool", "is_mainland_china", true);
		}
		if (pkg.equals("com.android.mms")) {
			Helpers.findAndHookMethodSilently("com.miui.smsextra.sdk.SDKManager", lpparam.classLoader, "supportClassify", XC_MethodReplacement.returnConstant(true));
		}
	}
}