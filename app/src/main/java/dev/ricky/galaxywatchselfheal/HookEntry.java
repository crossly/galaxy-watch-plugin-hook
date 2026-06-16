package dev.ricky.galaxywatchselfheal;

import dev.ricky.galaxywatchselfheal.core.WatchdogPolicy;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;
import java.util.List;

public final class HookEntry implements IXposedHookLoadPackage {
    private static final String ANDROID_PACKAGE = "android";
    private static final String CDM_SERVICE =
            "com.android.server.companion.CompanionDeviceManagerService";
    private static final String TAG_PREFIX = "GalaxyWatchPluginHook: ";
    private static final long BOOT_RECOVERY_DELAY_MS = 45_000L;
    private static final long RETRY_RECOVERY_DELAY_MS = 120_000L;
    private static volatile boolean frameworkHookInstalled;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (ANDROID_PACKAGE.equals(lpparam.packageName)) {
            hookCompanionDeviceManager(lpparam.classLoader);
            return;
        }

        if (!WatchdogPolicy.isTargetPackage(lpparam.packageName)) {
            return;
        }

        log("loaded for " + lpparam.packageName);
        log("Samsung process hook is passive; framework companion recovery handles presence");
    }

    private static void hookCompanionDeviceManager(ClassLoader classLoader) {
        if (frameworkHookInstalled) {
            return;
        }
        frameworkHookInstalled = true;

        try {
            XposedHelpers.findAndHookMethod(
                    CDM_SERVICE,
                    classLoader,
                    "onStart",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            scheduleCompanionPresenceRecovery(param.thisObject);
                        }
                    });
            log("hooked " + CDM_SERVICE + "#onStart()");
        } catch (Throwable t) {
            log("CompanionDeviceManagerService#onStart hook unavailable: " + t);
        }
    }

    private static void scheduleCompanionPresenceRecovery(Object companionDeviceManagerService) {
        if (companionDeviceManagerService == null) {
            log("skip framework recovery: service is null");
            return;
        }

        Thread worker = new Thread(() -> {
            sleep(BOOT_RECOVERY_DELAY_MS);
            recoverSamsungWatchAssociations(companionDeviceManagerService);
            sleep(RETRY_RECOVERY_DELAY_MS);
            recoverSamsungWatchAssociations(companionDeviceManagerService);
        }, "GalaxyWatchPluginHook-CDM");
        worker.setDaemon(true);
        worker.start();
        log("scheduled companion presence recovery");
    }

    private static void recoverSamsungWatchAssociations(Object service) {
        try {
            Object associationStore = XposedHelpers.getObjectField(service, "mAssociationStore");
            Object devicePresenceProcessor =
                    XposedHelpers.getObjectField(service, "mDevicePresenceProcessor");
            if (associationStore == null || devicePresenceProcessor == null) {
                log("skip framework recovery: CDM internals unavailable");
                return;
            }

            Object associationsObject = XposedHelpers.callMethod(
                    associationStore,
                    "getActiveAssociationsByPackage",
                    0,
                    WatchdogPolicy.WATCH7_PLUGIN_PACKAGE);
            if (!(associationsObject instanceof List)) {
                log("skip framework recovery: association list unavailable");
                return;
            }

            List<?> associations = (List<?>) associationsObject;
            if (associations.isEmpty()) {
                log("skip framework recovery: no active Watch7 Plugin association");
                return;
            }

            Method connectedMethod = devicePresenceProcessor.getClass().getMethod(
                    "onBluetoothCompanionDeviceConnected",
                    int.class,
                    int.class);
            for (Object association : associations) {
                int associationId = ((Integer) XposedHelpers.callMethod(association, "getId"));
                int userId = ((Integer) XposedHelpers.callMethod(association, "getUserId"));
                String packageName = String.valueOf(
                        XposedHelpers.callMethod(association, "getPackageName"));
                if (!WatchdogPolicy.WATCH7_PLUGIN_PACKAGE.equals(packageName)) {
                    continue;
                }

                ensureObservedWhenPresent(associationStore, association);
                connectedMethod.invoke(devicePresenceProcessor, associationId, userId);
                log("requested companion BT connected event, associationId="
                        + associationId
                        + ", userId="
                        + userId);
            }
        } catch (Throwable t) {
            log("framework recovery failed: " + t);
        }
    }

    private static void ensureObservedWhenPresent(Object associationStore, Object association) {
        try {
            Boolean isObserved = (Boolean) XposedHelpers.callMethod(
                    association,
                    "isNotifyOnDeviceNearby");
            if (Boolean.TRUE.equals(isObserved)) {
                return;
            }

            Class<?> associationInfoClass = Class.forName("android.companion.AssociationInfo");
            Class<?> builderClass = Class.forName("android.companion.AssociationInfo$Builder");
            Object builder = builderClass.getConstructor(associationInfoClass).newInstance(association);
            XposedHelpers.callMethod(builder, "setNotifyOnDeviceNearby", true);
            Object updatedAssociation = XposedHelpers.callMethod(builder, "build");
            XposedHelpers.callMethod(associationStore, "updateAssociation", updatedAssociation);
            log("enabled companion presence observation for Watch7 association");
        } catch (Throwable t) {
            log("unable to enable companion presence observation: " + t);
        }
    }

    private static void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String message) {
        XposedBridge.log(TAG_PREFIX + message);
    }
}
