package com.erail.validation;

import com.erail.exceptions.InvalidPNRException;
import com.erail.exceptions.InvalidPassengerException;

/**
 * Checks everything the user types BEFORE the program uses it.
 * All methods are static because the validator keeps no data of its own:
 * call them like InputValidator.validateName("...").
 *
 * Passenger errors  -> InvalidPassengerException (unchecked)
 * PNR errors        -> InvalidPNRException       (checked)
 * Other bad input   -> IllegalArgumentException  (unchecked, built into Java)
 */
public class InputValidator {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 25;
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 120;
    private static final int MIN_TRAIN_NUMBER = 10000;
    private static final int MAX_TRAIN_NUMBER = 99999;

    // Converts text to a whole number between min and max.
    // The 'fieldName' is only used to make the error message friendly.
    public static int parseInteger(String text, int min, int max, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }

        Integer number;
        try {
            number = Integer.valueOf(text.trim());      // wrapper class: text -> Integer object
        } catch (NumberFormatException e) {
            // Re-throw as a clearer exception (exception propagation to the caller)
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }

        int value = number;                              // auto-unboxing: Integer -> int
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    // Name rules: not empty, 2-25 characters, only letters and spaces.
    // Returns the name tidied up, e.g. "  rAHUL   sharma " -> "Rahul Sharma".
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidPassengerException("Name cannot be empty.");
        }

        String cleaned = name.trim();
        if (cleaned.length() < MIN_NAME_LENGTH || cleaned.length() > MAX_NAME_LENGTH) {
            throw new InvalidPassengerException("Name must be " + MIN_NAME_LENGTH
                    + " to " + MAX_NAME_LENGTH + " characters long.");
        }

        for (int i = 0; i < cleaned.length(); i++) {
            char ch = cleaned.charAt(i);
            if (!Character.isLetter(ch) && ch != ' ') {
                throw new InvalidPassengerException("Name can contain only letters and spaces.");
            }
        }
        return toTitleCase(cleaned);
    }

    // Makes the first letter of each word a capital and the rest small
    private static String toTitleCase(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;                                // skip extra spaces
            }
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase());
            result.append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    // Age must be a whole number from 1 to 120
    public static int validateAge(String text) {
        try {
            return parseInteger(text, MIN_AGE, MAX_AGE, "Age");
        } catch (IllegalArgumentException e) {
            // Translate the general error into a passenger-specific one
            throw new InvalidPassengerException(e.getMessage());
        }
    }

    // Accepts M / Male, F / Female, O / Other in any letter case
    public static String validateGender(String text) {
        String gender;
        if (text == null) {
            gender = "";
        } else {
            gender = text.trim().toLowerCase();
        }

        if (gender.equals("m") || gender.equals("male")) {
            return "Male";
        } else if (gender.equals("f") || gender.equals("female")) {
            return "Female";
        } else if (gender.equals("o") || gender.equals("other")) {
            return "Other";
        }
        throw new InvalidPassengerException("Gender must be M (Male), F (Female) or O (Other).");
    }

    // Train numbers are 5-digit numbers (10000 to 99999)
    public static int validateTrainNumber(String text) {
        return parseInteger(text, MIN_TRAIN_NUMBER, MAX_TRAIN_NUMBER, "Train number");
    }

    // A station can be typed as a code, name or city: letters and spaces only
    public static String validateStationInput(String text, String label) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String cleaned = text.trim();
        for (int i = 0; i < cleaned.length(); i++) {
            char ch = cleaned.charAt(i);
            if (!Character.isLetter(ch) && ch != ' ') {
                throw new IllegalArgumentException(label + " can contain only letters and spaces.");
            }
        }
        return cleaned;
    }

    // Both places must be valid and they must be different
    public static void validateSourceAndDestination(String source, String destination) {
        String from = validateStationInput(source, "Source");
        String to = validateStationInput(destination, "Destination");
        if (from.equalsIgnoreCase(to)) {
            throw new IllegalArgumentException("Source and destination cannot be the same.");
        }
    }

    // PNR format: "PNR" followed by 6 digits, e.g. PNR100001. Returns it in capitals.
    public static String validatePnr(String text) throws InvalidPNRException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidPNRException("PNR cannot be empty.");
        }

        String pnr = text.trim().toUpperCase();
        if (pnr.length() != 9 || !pnr.substring(0, 3).equals("PNR")) {
            throw new InvalidPNRException("Invalid PNR format. A PNR looks like PNR100001.");
        }

        String digits = pnr.substring(3);
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                throw new InvalidPNRException("Invalid PNR format. A PNR looks like PNR100001.");
            }
        }
        return pnr;
    }
}
