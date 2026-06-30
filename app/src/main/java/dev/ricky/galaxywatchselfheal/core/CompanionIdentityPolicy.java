package dev.ricky.galaxywatchselfheal.core;

public final class CompanionIdentityPolicy {
    public static final String KEY_MANUFACTURER = "key_manufacturer";
    public static final String KEY_MODEL_NUMBER = "key_model_number";
    public static final String KEY_SALES_CODE = "key_sales_code";

    private static final String SAMSUNG_MANUFACTURER = "SAMSUNG";
    private static final String SAMSUNG_S25_ULTRA_MODEL = "SM-S938U";
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
}
