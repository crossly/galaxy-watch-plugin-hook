package dev.ricky.galaxywatchselfheal.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompanionIdentityPolicyTest {
    @Test
    public void returnsSamsungIdentityForCompanionKeys() {
        assertEquals("SAMSUNG", CompanionIdentityPolicy.spoofedValueFor("key_manufacturer", "HONOR"));
        assertEquals("SM-S938U", CompanionIdentityPolicy.spoofedValueFor("key_model_number", "BKQ-AN90"));
        assertEquals("XAA", CompanionIdentityPolicy.spoofedValueFor("key_sales_code", "CHC"));
    }

    @Test
    public void leavesUnrelatedCapabilityValuesUntouched() {
        assertEquals("original", CompanionIdentityPolicy.spoofedValueFor("key_watch_sdk_version", "original"));
        assertFalse(CompanionIdentityPolicy.shouldSpoof("key_watch_sdk_version"));
    }

    @Test
    public void recognizesOnlyCompanionIdentityKeys() {
        assertTrue(CompanionIdentityPolicy.shouldSpoof("key_manufacturer"));
        assertTrue(CompanionIdentityPolicy.shouldSpoof("key_model_number"));
        assertTrue(CompanionIdentityPolicy.shouldSpoof("key_sales_code"));
    }
}
