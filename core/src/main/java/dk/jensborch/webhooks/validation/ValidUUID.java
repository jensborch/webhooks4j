package dk.jensborch.webhooks.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import dk.jensborch.webhooks.validation.ValidUUID.UUIDValidator;

/**
 * UUID validator annotation.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UUIDValidator.class)
@Documented
public @interface ValidUUID {

    String message() default "Invalid zoned data time format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * UUID validator.
     */
    class UUIDValidator implements ConstraintValidator<ValidUUID, String> {

        @Override
        public void initialize(final ValidUUID constraintAnnotation) {
            //Do nothing...
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            try {
                if (value != null) {
                    UUID.fromString(value);
                }
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
