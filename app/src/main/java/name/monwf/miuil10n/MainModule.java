package name.monwf.miuil10n;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

//	public static ResourceHooks resHooks;

	public void initZygote(StartupParam startParam) {
//		resHooks = new ResourceHooks();


	}

	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("com.android.contacts")
				|| pkg.equals("com.miui.weather2")
				|| pkg.equals("com.android.mms")
				|| pkg.equals("com.android.calendar")
				|| pkg.equals("com.android.deskclock")
				|| pkg.equals("com.miui.yellowpage")
		) {
			Class<?> classBuild = XposedHelpers.findClass("miui.os.Build", lpparam.classLoader);
			XposedHelpers.setStaticBooleanField(classBuild, "IS_INTERNATIONAL_BUILD",false);
		}
	}
}