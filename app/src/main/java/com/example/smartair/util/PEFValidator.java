package com.example.smartair.util;

/**
 * Utility class for validating PEF (Peak Expiratory Flow) values.
 * Provides kid-friendly warnings for out-of-range values.
 */
public class PEFValidator {
    
    // Typical PEF ranges for children (ages 6-16)
    // These are general guidelines; actual ranges vary by age, height, gender, etc.
    private static final int MIN_REASONABLE_PEF = 50;  // Very low, but possible for young children
    private static final int MAX_REASONABLE_PEF = 800;  // High end for older(maybe larger) children
    private static final int MIN_WARNING_PEF = 100;    // Below this is unusually low
    private static final int MAX_WARNING_PEF = 600;     // Above this is unusually high

    /**
     * Validates a PEF value and returns a validation result.
     * 
     * @param pefValue The PEF value to validate
     * @return ValidationResult containing whether the value is valid and a warning message if needed
     */
    public static ValidationResult validatePEF(Integer pefValue) {
        if (pefValue == null) {
            return new ValidationResult(false, "Please enter a PEF value");
        }

        if (pefValue <= 0) {
            return new ValidationResult(false, "PEF value must be greater than 0");
        }

        if (pefValue < MIN_REASONABLE_PEF) {
            return new ValidationResult(false, 
                "This value seems very low. Please check your meter and try again. " +
                "If this is correct, talk to your parent or doctor.");
        }

        if (pefValue > MAX_REASONABLE_PEF) {
            return new ValidationResult(false, 
                "This value seems very high. Please check your meter and make sure you entered the number correctly.");
        }

        // Check for warning ranges (values that are unusual but not impossible)
        if (pefValue < MIN_WARNING_PEF) {
            return new ValidationResult(true, 
                "⚠️ This value is lower than usual. Double-check your meter reading.");
        }

        if (pefValue > MAX_WARNING_PEF) {
            return new ValidationResult(true, 
                "⚠️ This value is higher than usual. Make sure you entered the number correctly.");
        }

        // Value is in normal range
        return new ValidationResult(true, null);
    }

    /**
     * Result of PEF validation.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String warningMessage;

        public ValidationResult(boolean isValid, String warningMessage) {
            this.isValid = isValid;
            this.warningMessage = warningMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public boolean hasWarning() {
            return warningMessage != null && !warningMessage.isEmpty();
        }
    }
}

