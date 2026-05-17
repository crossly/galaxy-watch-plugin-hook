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

    private static String read(String path) throws Exception {
        Path root = Paths.get(System.getProperty("user.dir")).getParent();
        return new String(Files.readAllBytes(root.resolve(path)), StandardCharsets.UTF_8);
    }
}
