package dev.ricky.galaxywatchselfheal.core;

public final class CompanionIdentityPolicy {
    public static final String KEY_MANUFACTURER = "key_manufacturer";
    public static final String KEY_MODEL_NUMBER = "key_model_number";
    public static final String KEY_SALES_CODE = "key_sales_code";

    private static final String SAMSUNG_MANUFACTURER = "SAMSUNG";
    private static final String SAMSUNG_BRAND = "samsung";
    private static final String SAMSUNG_S25_ULTRA_MODEL = "SM-S938U";
    private static final String SAMSUNG_S25_ULTRA_DEVICE = "pa3q";
    private static final String SAMSUNG_S25_ULTRA_PRODUCT = "pa3quew";
    private static final String USA_SALES_CODE = "XAA";

    private CompanionIdentityPolicy() {
    }

    public static String spoofedValueFor(String preferenceKey, String originalValue) {
        if (KEY_MANUFACTURER.equals(preferenceKey)) {
            return SAMSUNG_MANUFACTURER;
        }
        if (KEY_MODEL_NUMBER.equals(preferenceKey)) {
            return SAMSUNG_S25_ULTRA_MODEL;
        }
        if (KEY_SALES_CODE.equals(preferenceKey)) {
            return USA_SALES_CODE;
        }
        return originalValue;
    }

    public static boolean shouldSpoof(String preferenceKey) {
        return KEY_MANUFACTURER.equals(preferenceKey)
                || KEY_MODEL_NUMBER.equals(preferenceKey)
                || KEY_SALES_CODE.equals(preferenceKey);
    }

    public static String systemPropertyValueFor(String key, String originalValue) {
        if ("ro.product.manufacturer".equals(key)
                || "ro.product.vendor.manufacturer".equals(key)
                || "ro.product.odm.manufacturer".equals(key)
                || "ro.product.system.manufacturer".equals(key)) {
            return SAMSUNG_MANUFACTURER;
        }
        if ("ro.product.brand".equals(key)
                || "ro.product.vendor.brand".equals(key)
                || "ro.product.odm.brand".equals(key)
                || "ro.product.system.brand".equals(key)) {
            return SAMSUNG_BRAND;
        }
        if ("ro.product.model".equals(key)
                || "ro.product.vendor.model".equals(key)
                || "ro.product.odm.model".equals(key)
                || "ro.product.system.model".equals(key)) {
            return SAMSUNG_S25_ULTRA_MODEL;
        }
        if ("ro.product.device".equals(key)
                || "ro.product.vendor.device".equals(key)
                || "ro.product.odm.device".equals(key)
                || "ro.product.system.device".equals(key)) {
            return SAMSUNG_S25_ULTRA_DEVICE;
        }
        if ("ro.product.name".equals(key)
                || "ro.product.vendor.name".equals(key)
                || "ro.product.odm.name".equals(key)
                || "ro.product.system.name".equals(key)) {
            return SAMSUNG_S25_ULTRA_PRODUCT;
        }
        if ("ro.csc.sales_code".equals(key)
                || "ril.sales_code".equals(key)
                || "persist.sys.omc.sales_code".equals(key)) {
            return USA_SALES_CODE;
        }
        return originalValue;
    }

    public static String samsungManufacturer() {
        return SAMSUNG_MANUFACTURER;
    }

    public static String samsungBrand() {
        return SAMSUNG_BRAND;
    }

    public static String samsungModel() {
        return SAMSUNG_S25_ULTRA_MODEL;
    }

    public static String samsungDevice() {
        return SAMSUNG_S25_ULTRA_DEVICE;
    }

    public static String samsungProduct() {
        return SAMSUNG_S25_ULTRA_PRODUCT;
    }
}
