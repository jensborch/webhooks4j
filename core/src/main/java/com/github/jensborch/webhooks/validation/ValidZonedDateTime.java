package com.github.jensborch.webhooks.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import com.github.jensborch.webhooks.validation.ValidZonedDateTime.ZonedDateTimeValidator;

/**
 * ZonedDateTime validator annotation.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ZonedDateTimeValidator.class)
@Documented
public @interface ValidZonedDateTime {

    String message() default "Invalid zoned data time format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * ZonedDateTime validator.
     */
    class ZonedDateTimeValidator implements ConstraintValidator<ValidZonedDateTime, String> {

        @Override
        public void initialize(final ValidZonedDateTime constraintAnnotation) {
            //Do nothing...
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            try {
                if (value != null) {
                    ZonedDateTime.parse(value);
                }
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
    }
}
