package com.im.inspection.annotation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/09/03 15:11:22
 */

@Component
public class SimIdValidator implements ConstraintValidator<QtechValidSimId, String> {
    @Override
    public void initialize(QtechValidSimId constraintAnnotation) {
        // Initialization method, typically left empty
    }

    @Override
    public boolean isValid(String simId, ConstraintValidatorContext context) {
        if (simId == null) {
            return false;
        }

        String processedSimId = simId;

        // Only decode if it looks like it was URL-encoded
        if (simId.contains("%")) {
            try {
                processedSimId = URLDecoder.decode(simId, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                // Should never happen as UTF-8 is guaranteed to be supported
                return false;
            } catch (IllegalArgumentException e) {
                // Invalid percent-encoded sequence
                return false;
            }
        }

        // Validate against expected pattern: starts with "86" followed by digits only
        return processedSimId.matches("^86\\d+$");
    }
}
