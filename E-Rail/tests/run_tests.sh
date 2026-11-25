#!/bin/bash
# Runs every scripted scenario in tests/inputs and checks that the output
# contains the expected sentence. Run from the E-Rail folder:  bash tests/run_tests.sh
# (On Windows you can run the same files with IntelliJ's "Redirect input from" option.)

javac -d out $(find src -name "*.java") || exit 1

pass=0
fail=0

check() {   # check <input file> <expected text> <description>
    output=$(java -cp out com.erail.main.Main < "tests/inputs/$1" 2>&1)
    if echo "$output" | grep -qF "$2" && ! echo "$output" | grep -q "Exception in thread"; then
        echo "PASS  $3"
        pass=$((pass + 1))
    else
        echo "FAIL  $3   (expected: $2)"
        fail=$((fail + 1))
    fi
}

check test01_search_success.txt   "1 train(s) found:"                       "01 Successful train search"
check test02_search_failure.txt   "No trains found from Delhi to Chennai."  "02 Unsuccessful train search"
check test03_booking_success.txt  "Seats reserved."                         "03 Successful ticket booking"
check test04_seats_unavailable.txt "is fully booked. No seats available."   "04 Booking when seats are unavailable"
check test05_invalid_age.txt      "Age must be between 1 and 120."          "05 Invalid passenger age"
check test06_invalid_name.txt     "Name cannot be empty."                   "06 Invalid passenger name"
check test07_payment_success.txt  "Payment successful. Booking 1001 is CONFIRMED." "07 Successful payment"
check test08_payment_failed.txt   "Card declined by the bank (simulated)."  "08 Failed payment"
check test09_pnr_success.txt      "Status        : CONFIRMED"               "09 Successful PNR search"
check test10_pnr_invalid.txt      "No ticket found for PNR PNR999999."      "10 Invalid PNR search"
check test11_cancel_success.txt   "has been CANCELLED."                     "11 Successful cancellation"
check test12_cancel_twice.txt     "is already cancelled."                   "12 Cancelling an already cancelled ticket"
check test13_invalid_menu.txt     "Menu choice must be between 1 and 11."   "13 Invalid menu input"
check test14_invalid_train.txt    "No train found with number 99999."       "14 Booking with invalid train number"
check test15_full_demo.txt        "Seat 1 is already selected for passenger 1." "15 Duplicate seat in one booking + discounts"
check test16_booking_pay_later.txt "Refund of Rs. 350.00"                    "16 Pay later, partial cancel, refund"

echo
echo "Passed: $pass   Failed: $fail"
