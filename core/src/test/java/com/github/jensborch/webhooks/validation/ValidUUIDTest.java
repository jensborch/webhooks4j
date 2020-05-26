package com.github.jensborch.webhooks.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ValidUUID}.
 */
class ValidUUIDTest {

    @Test
    void testIsNotValid() {
        ValidUUID.UUIDValidator validator = new ValidUUID.UUIDValidator();
        assertFalse(validator.isValid("", null));
    }

    @Test
    void testIsValid() {
        ValidUUID.UUIDValidator validator = new ValidUUID.UUIDValidator();
        assertTrue(validator.isValid("676334c8-d399-49f4-b0f7-c8da072e24af", null));
    }

    @Test
    void testNullIsValid() {
        ValidUUID.UUIDValidator validator = new ValidUUID.UUIDValidator();
        assertTrue(validator.isValid(null, null));
    }

}
