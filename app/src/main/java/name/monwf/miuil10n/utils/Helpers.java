package name.monwf.miuil10n.utils;


import android.annotation.SuppressLint;

import android.app.Application;

import android.content.Context;

import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;

import android.os.Build;

import java.io.File;

import java.lang.reflect.Method;

import java.util.Locale;
import java.util.Set;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import miui.os.SystemProperties;


@SuppressWarnings("WeakerAccess")
public class Helpers {

	@SuppressLint("StaticFieldLeak")
	public static Context mModuleContext = null;
	public static final String modulePkg = "name.monwf.miuil10n";
	public static final String logFile = "xposed_log";
	public static SharedPreferences prefs = null;

	public static boolean is11() {
		return SystemProperties.getInt("ro.miui.ui.version.code", 8) >= 9;
	}

	public static boolean is12() {
		return SystemProperties.getInt("ro.miui.ui.version.code", 9) >= 10;
	}

	public static boolean isPiePlus() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
	}

	public static boolean isQPlus() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	}

	public static String getNewEdXposedPath() {
		File[] files = new File("/data/misc").listFiles();
		String edxpPath = null;
		if (files != null && files.length > 0)
		for (File file: files)
		if (file.getName().startsWith("edxp_")) {
			edxpPath = file.getName();
			break;
		}
		return edxpPath;
	}


	public static boolean containsStringPair(Set<String> hayStack, String needle) {
		boolean res = false;
		if (hayStack == null || hayStack.size() == 0) return false;
		for (String pair: hayStack) {
			String[] needles = pair.split("\\|");
			if (needles[0].equalsIgnoreCase(needle)) {
				res = true;
				break;
			}
		}
		return res;
	}

	public static synchronized Context getLocaleContext(Context context) throws Throwable {
		if (prefs != null) {
			String locale = prefs.getString("pref_key_miuizer_locale", "auto");
			if (locale == null || "auto".equals(locale) || "1".equals(locale)) return context;
			Configuration config = context.getResources().getConfiguration();
			config.setLocale(Locale.forLanguageTag(locale));
			return context.createConfigurationContext(config);
		} else {
			return context;
		}
	}

	public static synchronized Context getModuleContext(Context context) throws Throwable {
		return getModuleContext(context, null);
	}

	public static synchronized Context getModuleContext(Context context, Configuration config) throws Throwable {
		if (mModuleContext == null)
		mModuleContext = context.createPackageContext(modulePkg, Context.CONTEXT_IGNORE_SECURITY).createDeviceProtectedStorageContext();
		return config == null ? mModuleContext : mModuleContext.createConfigurationContext(config);
	}

	public static synchronized Context getProtectedContext(Context context) {
		return getProtectedContext(context, null);
	}

	public static synchronized Context getProtectedContext(Context context, Configuration config) {
		try {
			Context mContext = context.isDeviceProtectedStorage() ? context : context.createDeviceProtectedStorageContext();
			return getLocaleContext(config == null ? mContext : mContext.createConfigurationContext(config));
		} catch (Throwable t) {
			return context;
		}
	}

	public static synchronized Resources getModuleRes(Context context) throws Throwable {
		Configuration config = context.getResources().getConfiguration();
		Context moduleContext = getModuleContext(context);
		return (config == null ? moduleContext.getResources() : moduleContext.createConfigurationContext(config).getResources());
	}

	private static String getCallerMethod() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement el: stackTrace)
		if (el != null && el.getClassName().startsWith(modulePkg + ".mods")) return el.getMethodName();
		return stackTrace[4].getMethodName();
	}

	public static void log(String line) {
		XposedBridge.log("[CustoMIUIzer] " + line);
	}

	public static void log(String mod, String line) {
		XposedBridge.log("[CustoMIUIzer][" + mod + "] " + line);
	}

	public static class MethodHook extends XC_MethodHook {
		protected void before(MethodHookParam param) throws Throwable {}
		protected void after(MethodHookParam param) throws Throwable {}

		public MethodHook() {
			super();
		}

		public MethodHook(int priority) {
			super(priority);
		}

		@Override
		public final void beforeHookedMethod(MethodHookParam param) throws Throwable {
			try {
				this.before(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		@Override
		public final void afterHookedMethod(MethodHookParam param) throws Throwable {
			try {
				this.after(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}

	public static void hookMethod(Method method, MethodHook callback) {
		try {
			XposedBridge.hookMethod(method, callback);
		} catch (Throwable t) {
			log(getCallerMethod(), "Failed to hook " + method.getName() + " method");
		}
	}

	public static void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
		} catch (Throwable t) {
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + className);
		}
	}

	public static void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
		} catch (Throwable t) {
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + clazz.getCanonicalName());
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean findAndHookMethodSilently(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean findAndHookMethodSilently(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
		} catch (Throwable t) {
			log(getCallerMethod(), "Failed to hook constructor in " + className);
		}
	}

	public static void hookAllConstructors(String className, ClassLoader classLoader, MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			if (hookClass == null || XposedBridge.hookAllConstructors(hookClass, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + className + " constructor");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllConstructors(Class<?> hookClass, MethodHook callback) {
		try {
			if (XposedBridge.hookAllConstructors(hookClass, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + hookClass.getCanonicalName() + " constructor");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllMethods(String className, ClassLoader classLoader, String methodName, XC_MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			if (hookClass == null || XposedBridge.hookAllMethods(hookClass, methodName, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + className);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
		try {
			if (XposedBridge.hookAllMethods(hookClass, methodName, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + hookClass.getCanonicalName());
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static boolean hookAllMethodsSilently(String className, ClassLoader classLoader, String methodName, XC_MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			return hookClass != null && XposedBridge.hookAllMethods(hookClass, methodName, callback).size() > 0;
		} catch (Throwable t) {
			return false;
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static Context findContext() {
		Context context = null;
		try {
			context = (Application)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentApplication");
			if (context == null) {
				Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
				if (currentActivityThread != null) context = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
			}
		} catch (Throwable ignore) {}
		return context;
	}

}