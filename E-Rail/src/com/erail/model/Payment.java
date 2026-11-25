package com.erail.model;

/**
 * A SIMULATED payment. No real payment gateway is used.
 *
 * Simulation rules (so results are predictable and easy to test):
 *   Cash : always succeeds.
 *   UPI  : succeeds only if the UPI ID looks like name@bank.
 *   Card : needs exactly 16 digits; a card number ending in 0 is "declined by the bank".
 */
public class Payment {

    public static final String MODE_CASH = "Cash";
    public static final String MODE_UPI = "UPI";
    public static final String MODE_CARD = "Card";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    // static: one shared counter gives every payment a unique ID
    private static int nextPaymentId = 5001;

    private int paymentId;
    private double amount;
    private String mode;
    private String reference;                  // UPI ID or masked card number (never the full card number)
    private String status;
    private String failureReason;
    private double refundedAmount;

    // Constructor 1: amount and payment mode are given
    public Payment(double amount, String mode) {
        this.paymentId = nextPaymentId;
        nextPaymentId++;
        this.amount = amount;
        this.mode = mode;
        this.reference = "N/A";
        this.status = STATUS_PENDING;
        this.failureReason = "";
        this.refundedAmount = 0;
    }

    // Constructor 2 (overloading): if no mode is given we assume Cash
    public Payment(double amount) {
        this(amount, MODE_CASH);
    }

    // Runs the simulated payment. 'reference' is the UPI ID or card number the user typed.
    // Returns true when the payment succeeds.
    public boolean process(String reference) {
        String text;
        if (reference == null) {
            text = "";
        } else {
            text = reference.trim();
        }

        if (mode.equals(MODE_CASH)) {
            status = STATUS_SUCCESS;
        } else if (mode.equals(MODE_UPI)) {
            if (isValidUpiId(text)) {
                this.reference = text;
                status = STATUS_SUCCESS;
            } else {
                markFailed("Invalid UPI ID. Use the format name@bank.");
            }
        } else if (mode.equals(MODE_CARD)) {
            String digits = text.replace(" ", "");   // allow "1234 5678 ..." with spaces
            if (!hasSixteenDigits(digits)) {
                markFailed("Card number must contain exactly 16 digits.");
            } else if (digits.endsWith("0")) {
                markFailed("Card declined by the bank (simulated).");
            } else {
                // Keep only the last 4 digits for display
                this.reference = "XXXX-XXXX-XXXX-" + digits.substring(12);
                status = STATUS_SUCCESS;
            }
        } else {
            markFailed("Unknown payment mode.");
        }
        return isSuccessful();
    }

    private void markFailed(String reason) {
        status = STATUS_FAILED;
        failureReason = reason;
    }

    // UPI ID rule: exactly one '@', with at least one character before and after it
    private boolean isValidUpiId(String text) {
        int atPosition = text.indexOf('@');
        if (atPosition <= 0 || atPosition == text.length() - 1) {
            return false;
        }
        if (atPosition != text.lastIndexOf('@')) {
            return false;                      // more than one '@'
        }
        return !text.contains(" ");
    }

    private boolean hasSixteenDigits(String text) {
        if (text.length() != 16) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isSuccessful() {
        return status.equals(STATUS_SUCCESS);
    }

    // Called when a ticket of a paid booking is cancelled (full refund is simulated)
    public void addRefund(double refund) {
        refundedAmount = refundedAmount + refund;
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" Payment ID    : %d%n", paymentId));
        sb.append(String.format(" Mode          : %s%n", mode));
        sb.append(String.format(" Reference     : %s%n", reference));
        sb.append(String.format(" Amount        : Rs. %.2f%n", amount));
        sb.append(String.format(" Status        : %s", status));
        if (status.equals(STATUS_FAILED)) {
            sb.append(String.format("%n Reason        : %s", failureReason));
        }
        if (refundedAmount > 0) {
            sb.append(String.format("%n Refunded      : Rs. %.2f", refundedAmount));
        }
        return sb.toString();
    }

    public int getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMode() {
        return mode;
    }

    public String getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public double getRefundedAmount() {
        return refundedAmount;
    }
}
