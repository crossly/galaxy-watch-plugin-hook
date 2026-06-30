package dev.ricky.galaxywatchselfheal;

import dev.ricky.galaxywatchselfheal.core.CompanionIdentityPolicy;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Watch7CapabilityExchangeHook {
    private static final String TAG_PREFIX = "GalaxyWatchPluginHook: ";
    private static final String CAPABILITY_EXCHANGE_MESSAGE =
            "com.samsung.android.companionservice.capability.CapabilityExchangeMessage";
    private static final String KEY_MANUFACTURER = "key_manufacturer";
    private static final String KEY_MODEL_NUMBER = "key_model_number";
    private static final String KEY_SALES_CODE = "key_sales_code";
    private static volatile boolean installed;

    private Watch7CapabilityExchangeHook() {
    }

    public static void install(ClassLoader classLoader) {
        if (installed) {
            return;
        }
        installed = true;

        try {
            Constructor<?> constructor = findTwoArgumentConstructor(
                    XposedHelpers.findClass(CAPABILITY_EXCHANGE_MESSAGE, classLoader));
            XposedBridge.hookMethod(constructor, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    spoofFeatureExchangeData(param.thisObject);
                }
            });
            log("hooked Watch7 capability feature_exchange data");
        } catch (Throwable t) {
            log("Watch7 capability feature_exchange hook unavailable: " + t);
        }
    }

    private static Constructor<?> findTwoArgumentConstructor(Class<?> messageClass)
            throws NoSuchMethodException {
        for (Constructor<?> constructor : messageClass.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 2) {
                return constructor;
            }
        }
        throw new NoSuchMethodException(messageClass.getName() + "#<init>(*, *)");
    }

    private static void spoofFeatureExchangeData(Object message) {
        Object data = XposedHelpers.getObjectField(message, "data");
        if (!(data instanceof Map)) {
            log("skip feature_exchange spoof: data is unavailable");
            return;
        }

        Map<Object, Object> mutableData = new LinkedHashMap<>((Map<?, ?>) data);
        Object originalVendor = mutableData.get(KEY_MANUFACTURER);
        mutableData.put(KEY_MANUFACTURER, CompanionIdentityPolicy.samsungManufacturer());
        mutableData.put(KEY_MODEL_NUMBER, CompanionIdentityPolicy.samsungModel());
        mutableData.put(KEY_SALES_CODE, CompanionIdentityPolicy.usaSalesCode());
        XposedHelpers.setObjectField(message, "data", mutableData);
        log("spoofed Watch7 feature_exchange vendor " + originalVendor
                + " -> " + CompanionIdentityPolicy.samsungManufacturer());
    }

    private static void log(String message) {
        XposedBridge.log(TAG_PREFIX + message);
    }
}
