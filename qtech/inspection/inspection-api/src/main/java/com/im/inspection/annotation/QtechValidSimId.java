package com.im.inspection.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author :  gaozhilin
 * @email  :  gaoolin@gmail.com
 * @date   :  2024/09/03 15:09:46
 */

/**
 * Bean Validation annotation for SIM ID format validation.
 *
 * @author gaozhilin
 * @since 2024/09/03
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimIdValidator.class)
public @interface QtechValidSimId {

    /**
     * Default error message template.
     *
     * @return the error message template
     */
    String message() default "Invalid simId format";

    /**
     * Constraint groups.
     *
     * @return the constraint groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for client applications.
     *
     * @return the payload classes
     */
    Class<? extends Payload>[] payload() default {};
}
