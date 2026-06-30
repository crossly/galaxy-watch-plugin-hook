package dev.ricky.galaxywatchselfheal;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class HookEntrySourceTest {
    @Test
    public void xposedEntryClassUsesDeclaredPackage() throws Exception {
        String source = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");

        assertTrue(source.contains("package dev.ricky.galaxywatchselfheal;"));
    }

    @Test
    public void frameworkScopeIsDeclaredForCompanionDeviceRecovery() throws Exception {
        String strings = read("app/src/main/res/values/strings.xml");

        assertTrue(strings.contains("<item>android</item>"));
    }

    @Test
    public void frameworkHookTargetsCompanionDevicePresenceProcessor() throws Exception {
        String source = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");

        assertTrue(source.contains("com.android.server.companion.CompanionDeviceManagerService"));
        assertTrue(source.contains("onBluetoothCompanionDeviceConnected"));
    }

    @Test
    public void frameworkRecoveryMarksAssociationObservedBeforeConnectedEvent() throws Exception {
        String source = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");

        assertTrue(source.contains("android.companion.AssociationInfo$Builder"));
        assertTrue(source.contains("setNotifyOnDeviceNearby"));
        assertTrue(source.contains("updateAssociation"));
    }

    @Test
    public void watch7PluginHookTargetsOnlyCompanionIdentityPreferences() throws Exception {
        String source = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");

        assertTrue(source.contains("com.samsung.android.basicdata.capability.CapabilityDataSetter"));
        assertTrue(source.contains("setPreference"));
        assertTrue(source.contains("CompanionIdentityPolicy.shouldSpoof"));
    }

    @Test
    public void scopeDoesNotIncludeSamsungHealthMonitor() throws Exception {
        String strings = read("app/src/main/res/values/strings.xml");
        String scopeList = read("app/src/main/resources/META-INF/xposed/scope.list");
        String manifest = read("app/src/main/AndroidManifest.xml");

        assertTrue(strings.contains("com.google.android.wearable.app.cn"));
        assertTrue(scopeList.contains("com.google.android.wearable.app.cn"));
        assertTrue(manifest.contains("com.google.android.wearable.app.cn"));
        assertTrue(!strings.contains("com.samsung.android.shealthmonitor"));
        assertTrue(!scopeList.contains("com.samsung.android.shealthmonitor"));
        assertTrue(!manifest.contains("com.samsung.android.shealthmonitor"));
    }

    @Test
    public void googleWearHookSpoofsPhoneIdentityWithoutWatchManagerBuildSpoof() throws Exception {
        String source = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");

        assertTrue(source.contains("com.google.android.wearable.app.cn"));
        assertTrue(source.contains("setStaticObjectField(android.os.Build.class"));
        assertTrue(source.contains("android.os.SystemProperties"));
        assertTrue(source.contains("Samsung process hook is passive for"));
    }

    @Test
    public void watch7PluginHookSpoofsWearCompanionIdentityModelOnly() throws Exception {
        String entrySource = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");
        String hookSource = read(
                "app/src/main/java/dev/ricky/galaxywatchselfheal/Watch7CompanionIdentityHook.java");

        assertTrue(entrySource.contains("Watch7CompanionIdentityHook.install"));
        assertTrue(hookSource.contains("com.google.android.gms.internal.wear_companion.wt"));
        assertTrue(hookSource.contains("\"e\""));
        assertTrue(hookSource.contains("findSingleArgumentMethod"));
        assertTrue(hookSource.contains("XposedBridge.hookMethod"));
        assertTrue(hookSource.contains("\"q\""));
        assertTrue(hookSource.contains("CompanionIdentityPolicy.samsungManufacturer()"));
    }

    @Test
    public void watch7PluginHookSpoofsFeatureExchangeDataForWcsProvider() throws Exception {
        String entrySource = read("app/src/main/java/dev/ricky/galaxywatchselfheal/HookEntry.java");
        String hookSource = read(
                "app/src/main/java/dev/ricky/galaxywatchselfheal/Watch7CapabilityExchangeHook.java");

        assertTrue(entrySource.contains("Watch7CapabilityExchangeHook.install"));
        assertTrue(hookSource.contains(
                "com.samsung.android.companionservice.capability.CapabilityExchangeMessage"));
        assertTrue(hookSource.contains("\"data\""));
        assertTrue(hookSource.contains("key_manufacturer"));
        assertTrue(hookSource.contains("key_model_number"));
        assertTrue(hookSource.contains("key_sales_code"));
        assertTrue(hookSource.contains("CompanionIdentityPolicy.usaSalesCode()"));
    }

    private static String read(String path) throws Exception {
        Path root = Paths.get(System.getProperty("user.dir")).getParent();
        return new String(Files.readAllBytes(root.resolve(path)), StandardCharsets.UTF_8);
    }
}
