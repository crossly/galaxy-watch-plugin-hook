package dev.ricky.galaxywatchselfheal;

import dev.ricky.galaxywatchselfheal.core.CompanionIdentityPolicy;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.lang.reflect.Method;

public final class Watch7CompanionIdentityHook {
    private static final String TAG_PREFIX = "GalaxyWatchPluginHook: ";
    private static final String WEAR_COMPANION_IDENTITY_MODEL =
            "com.google.android.gms.internal.wear_companion.wt";
    private static volatile boolean installed;

    private Watch7CompanionIdentityHook() {
    }

    public static void install(ClassLoader classLoader) {
        if (installed) {
            return;
        }
        installed = true;

        try {
            Method refreshMethod = findSingleArgumentMethod(
                    XposedHelpers.findClass(WEAR_COMPANION_IDENTITY_MODEL, classLoader),
                    "e");
            XposedBridge.hookMethod(refreshMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    spoofCompanionIdentityModel(param.thisObject);
                }
            });
            log("hooked Watch7 companion identity model");
        } catch (Throwable t) {
            log("Watch7 companion identity model hook unavailable: " + t);
        }
    }

    private static Method findSingleArgumentMethod(Class<?> modelClass, String methodName)
            throws NoSuchMethodException {
        for (Method method : modelClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName()) && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException(modelClass.getName() + "#" + methodName + "(*)");
    }

    private static void spoofCompanionIdentityModel(Object identityModel) {
        if (identityModel == null) {
            return;
        }

        String originalVendor = String.valueOf(XposedHelpers.getObjectField(identityModel, "q"));
        XposedHelpers.setObjectField(identityModel, "m", CompanionIdentityPolicy.samsungModel());
        XposedHelpers.setObjectField(identityModel, "n", CompanionIdentityPolicy.samsungManufacturer());
        XposedHelpers.setObjectField(identityModel, "q", CompanionIdentityPolicy.samsungManufacturer());
        if (!CompanionIdentityPolicy.samsungManufacturer().equals(originalVendor)) {
            log("spoofed Watch7 companion vendor " + originalVendor
                    + " -> " + CompanionIdentityPolicy.samsungManufacturer());
        }
    }

    private static void log(String message) {
        XposedBridge.log(TAG_PREFIX + message);
    }
}
