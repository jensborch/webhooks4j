package dk.jensborch.webhooks.validation;

import dk.jensborch.webhooks.validation.ValidZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ValidZonedDateTime}.
 */
public class ValidZonedDateTimeTest {

    @Test
    public void testIsNotValid() {
        ValidZonedDateTime.ZonedDateTimeValidator validator = new ValidZonedDateTime.ZonedDateTimeValidator();
        assertFalse(validator.isValid("", null));
    }

    @Test
    public void testIsValid() {
        ValidZonedDateTime.ZonedDateTimeValidator validator = new ValidZonedDateTime.ZonedDateTimeValidator();
        assertTrue(validator.isValid("2007-12-03T10:15:30+01:00", null));
    }

    @Test
    public void testNullIsValid() {
        ValidZonedDateTime.ZonedDateTimeValidator validator = new ValidZonedDateTime.ZonedDateTimeValidator();
        assertTrue(validator.isValid(null, null));
    }

}
