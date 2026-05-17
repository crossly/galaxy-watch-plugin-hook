# Galaxy Watch Plugin Hook

LSPosed/Xposed module that recovers Samsung Galaxy Watch Plugin connection on Honor MagicOS phones without clearing Samsung app data.

This project follows the same small module architecture as
[`crossly/honor-installer-hook`](https://github.com/crossly/honor-installer-hook):

- Android application package with no launcher activity
- `assets/xposed_init` legacy Xposed entrypoint
- manifest Xposed metadata and `xposedscope`
- local `:xposed-stubs` compile-only module
- Java 17 / Android Gradle Plugin 8.7.x baseline
- GitHub Actions CI and signed release workflow

## What it does

On some Honor MagicOS builds, after reboot the Samsung watch pairing still exists and Bluetooth is available, but Android's `CompanionDeviceManager` does not reliably deliver the companion device connected state to Samsung Watch Plugin.

The module hooks `com.android.server.companion.CompanionDeviceManagerService` in the `android` process. After system Companion Device Manager starts, it finds active associations for:

```text
com.samsung.wearable.watch7plugin
```

and calls the same internal presence path used by Android's companion debug command:

```text
DevicePresenceProcessor#onBluetoothCompanionDeviceConnected(associationId, userId)
```

This nudges Android to rebind Samsung's `WearCompanionDeviceService` and lets Galaxy Watch Manager recover the Wear node.

## Safety

The module is intentionally conservative:

- It does not clear Galaxy Wearable or Watch Plugin data.
- It does not unpair the watch.
- It does not reset Bluetooth.
- It does not freeze, disable, or re-enable Samsung packages.
- It only targets existing active companion associations for Samsung Watch7 Plugin.

## LSPosed Scope

Enable the module for:

- `android`
- `com.samsung.android.app.watchmanager`
- `com.samsung.wearable.watch7plugin`

Reboot after changing scope. The recovery hook runs in `system_server`, so a reboot is required for the `android` scope to load.

## Build

Requires:

- JDK 17
- Android SDK

Run unit tests:

```sh
./gradlew :app:testDebugUnitTest
```

Build debug APK:

```sh
./gradlew :app:assembleDebug
```

Build release APK:

```sh
./gradlew :app:assembleRelease
```

## Install

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Some Honor builds require an explicit installer identity for adb installs:

```sh
adb shell pm install -r -i com.android.packageinstaller /data/local/tmp/galaxywatchselfheal-debug.apk
```

## GitHub Release Signing

The release workflow mirrors `honor-installer-hook` and signs APKs online with `apksigner`.

Configure these repository secrets to use a stable release signing key:

```text
RELEASE_KEYSTORE_BASE64
RELEASE_KEYSTORE_PASSWORD
RELEASE_KEY_ALIAS
RELEASE_KEY_PASSWORD
```

If any secret is missing, GitHub Actions falls back to a temporary CI keystore so the workflow remains testable, but that APK should not be treated as a stable upgrade path.

Create a release by pushing a semantic version tag:

```sh
git tag v0.1.0
git push origin v0.1.0
```
