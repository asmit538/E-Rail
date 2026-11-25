# E-Rail - Railway Reservation Management System

A terminal-based railway reservation system written in **Core Java (Java 17+)**.
Users can view trains, search trains and routes, check seat availability, book tickets
for up to four passengers, pay (simulated), check a PNR, cancel tickets and see reports.

> All stations, trains, timings and fares are **made-up sample data**. This is a learning
> project and is not connected to any real railway system.

---

## 1. Project Objectives

- Demonstrate Lectures 1-15 of the Java syllabus in one connected project: OOP, arrays and
  strings, exception handling and integrated coding practice.
- Show real relationships between classes (a `Booking` *has* `Passenger`s and `Ticket`s, a
  `Ticket` *has* a `Train`, a `Train` *has* a `Route` and `Coach`es, a `Coach` *has* `Seat`s).
- Keep every class small and focused on one job, so it can be explained in a viva.
- Make sure ordinary wrong input (letters instead of numbers, empty names, wrong PNRs...)
  never crashes the program.

## 2. Technologies Used

| Item | Used |
|---|---|
| Language | Java 17 or newer (Core Java only) |
| IDE | IntelliJ IDEA |
| Input | `Scanner` (`nextLine()` only) |
| Output | `System.out.println()` and `System.out.printf()` |
| Data storage | Arrays only (no `ArrayList`, `HashMap`, streams or lambdas) |
| Not used | GUI, database, JDBC, files, serialization, frameworks, external libraries |

Besides `java.lang` and `Scanner`, only two other standard classes are used, each in one place:
`java.time.LocalDate` (in `Booking`, just to stamp today's date on a booking) and
`java.util.NoSuchElementException` (in `Main`, to end cleanly when the input stream ends).

## 3. Features

| # | Feature | Where in the menu |
|---|---|---|
| 1 | Train management: list trains, timings, fares, seat availability, seat map, stations | 1 |
| 2 | Passenger management: register passengers while booking (name, age, gender are validated), view passengers of a booking, view all passengers | 2 and 5 |
| 3 | Train search by source + destination (station code, name or city) or by train number | 3 |
| 4 | Route management: list routes, search routes between two stations, distance and duration | 4 |
| 5 | Ticket booking: choose train, up to 4 passengers, a coach and a seat for each, fare with age discount, unique PNR per ticket | 5 |
| 6 | Payment simulation: Cash, UPI or Card, with successful and failed payments | 6 (or right after booking) |
| 7 | PNR status | 7 |
| 8 | Ticket cancellation: seat is released, status updated, double cancellation prevented | 8 |
| 9 | Booking reports: all, confirmed, cancelled, pending and statistics | 9 and 10 |
| 10 | Input validation everywhere | all menus |

### How booking and payment fit together

1. **Book Ticket** reserves the seats and creates the tickets with status `PENDING`.
2. **Payment** (offered straight away, or later with menu option 6 and the Booking ID) turns the
   tickets into `CONFIRMED`. If the payment fails, the booking stays `PENDING`, the seats stay
   reserved and the user can try again.
3. **Cancel Ticket** works for `PENDING` and `CONFIRMED` tickets. If the booking was paid, a full
   refund is *recorded* (simulated). When every ticket of a booking is cancelled, the booking
   becomes `CANCELLED`.

### Payment simulation rules

| Mode | Succeeds when | Fails when |
|---|---|---|
| Cash | always | never |
| UPI | ID looks like `name@bank` | ID has no `@`, nothing before/after the `@`, spaces, or two `@` |
| Card | exactly 16 digits (spaces allowed) | not 16 digits, **or the number ends in 0** (simulated bank decline) |

### Fare rules (`FareCalculator`)

`fare = train base fare x coach multiplier - age discount`

| Coach | Multiplier | | Age | Discount |
|---|---|---|---|---|
| Sleeper | 1.0 | | 12 or younger | 50% |
| AC 3-Tier | 1.5 | | 60 or older | 40% |
| AC 2-Tier | 2.0 | | everyone else | none |

---

## 4. Package Structure

```
E-Rail/
├── README.md
├── src/com/erail/
│   ├── main/
│   │   └── Main.java
│   ├── model/
│   │   ├── User.java          (abstract)
│   │   ├── Passenger.java
│   │   ├── Station.java
│   │   ├── Route.java
│   │   ├── Seat.java
│   │   ├── Coach.java
│   │   ├── Train.java
│   │   ├── Ticket.java
│   │   ├── Booking.java
│   │   └── Payment.java
│   ├── service/
│   │   ├── RailwaySystem.java
│   │   ├── FareCalculator.java
│   │   ├── TrainService.java
│   │   ├── TicketService.java
│   │   ├── BookingService.java
│   │   └── ReportService.java
│   ├── validation/
│   │   └── InputValidator.java
│   ├── interfaces/
│   │   ├── Reservable.java
│   │   └── Displayable.java
│   └── exceptions/
│       ├── TrainNotFoundException.java
│       ├── SeatNotAvailableException.java
│       ├── InvalidPNRException.java
│       └── InvalidPassengerException.java
└── tests/
    ├── run_tests.sh           (automatic pass/fail check, Mac/Linux)
    └── inputs/                (16 scripted scenarios, one .txt file each)
```

**24 files = 18 core classes + 2 interfaces + 4 custom exceptions.**
Dependencies only point "downwards": `Main` -> `RailwaySystem` -> services -> models.
Models never call services.

### How the objects fit together

```
RailwaySystem ──creates──> Station[]  Route[]  Train[]
      │                       Route has 2 Stations;  Train has a Route and Coach[]
      │                       Coach has Seat[][]  (2-D array)
      ├── FareCalculator
      ├── TrainService   (uses Station[], Route[], Train[], FareCalculator)
      ├── TicketService  (owns Ticket[])
      ├── BookingService (owns Booking[], uses TicketService + FareCalculator)
      └── ReportService  (reads BookingService + TicketService + FareCalculator)

Booking ── has ──> Passenger[]  +  Ticket[]  +  Payment
Ticket  ── has ──> Passenger, Train, Coach, Seat
```

---

## 5. Complete Class Descriptions

For every class: **Purpose**, **Why it is needed**, **Key members** and **Interacts with**.

### A. Main application

#### `Main` (package `main`)
- **Purpose:** the program entry point and the whole console screen: menus, prompts, messages.
- **Why:** the user interface must be separate from the business rules, so the rules can be read and tested without menus getting in the way.
- **Key members:** `main()` (the menu loop, with `try/catch/finally`); one method per menu (`trainManagementMenu()`, `reportsMenu()`...); one method per action (`bookTicket()`, `payForBooking()`, `cancelTicket()`...); input helpers `readLine()`, `readInt()`, `readMenuChoice()`, `readYesNo()`, `readValidName()` etc., which repeat the question until the answer is valid.
- **Interacts with:** only `RailwaySystem` (and the services it hands out) and `InputValidator`. It catches every exception and turns it into a friendly message.

#### `RailwaySystem` (package `service`)
- **Purpose:** the control centre. Builds the sample data, creates all services and joins them together, and offers methods that need several classes at once.
- **Why:** somebody has to create the objects in the right order and hold them; otherwise `Main` would be full of `new` statements.
- **Key members:** `createStations()`, `createRoutes()`, `createTrains()` (sample data); `findTrain(String)` (validator + `TrainService`); `searchTrains()`, `searchRoutes()`, `bookTicket()`, `makePayment()`, `cancelTicket()`; getters for each service.
- **Interacts with:** every service, plus `Station`, `Route`, `Train`, `Coach`.

### B. User and passenger management

#### `User` (abstract, package `model`)
- **Purpose:** common data of any person: name, age, gender (all `private`, with getters/setters).
- **Why:** shows abstraction and inheritance. A plain `User` makes no sense, so the class is abstract.
- **Key members:** abstract `getDetails()`; constructor; getters and setters.
- **Interacts with:** extended by `Passenger`; used as a type in `ReportService`.

#### `Passenger` (extends `User`)
- **Purpose:** a person who travels. Adds a unique `passengerId` (from a `static` counter) and an age category.
- **Why:** passenger-specific data does not belong in `User`; also the place where the age limits (12 and 60) live.
- **Key members:** constants `CHILD_MAX_AGE`, `SENIOR_MIN_AGE`; static `nextPassengerId`; `getAgeCategory()`; `getDetails()` (overrides the abstract method).
- **Interacts with:** `Ticket` and `Booking` store passengers; `FareCalculator` reads the age limits.

### C. Railway management

#### `Train` (implements `Reservable`, `Displayable`)
- **Purpose:** train number, name, `Route`, departure/arrival, base fare and a `Coach[]` array.
- **Why:** the central object of the system; everything is booked on a train.
- **Key members:** two constructors (standard 3 coaches, or custom coaches); `getTotalSeats()` / `getAvailableSeats()` add up the coaches; arrival time is calculated from the route; `getSummary()` returns one table row.
- **Interacts with:** `Route`, `Coach`, `TrainService`, `Ticket`.

#### `Station`
- **Purpose:** station ID, name, code and city.
- **Why:** routes are built from stations, and `matches(text)` lets the user type a code (`NDL`), a name (`New Delhi`) or a city (`Delhi`).
- **Key members:** `matches()`, `getSummary()`.

#### `Route`
- **Purpose:** source `Station`, destination `Station`, distance and duration.
- **Why:** separates *where* a train goes from the train itself, and supports route search.
- **Key members:** two constructors (duration given, or estimated at 55 km/h); `connects(from, to)`; `getDurationText()`; `calculateArrival("16:30")` which uses `substring()` and `Integer.parseInt()`.

#### `Coach` (implements `Reservable`)
- **Purpose:** one coach with its type and a **2-D array** `Seat[][]` (rows x columns).
- **Why:** the seat layout is naturally a grid; also keeps seat logic out of `Train`.
- **Key members:** two constructors; `findSeat()` and `bookSeat()` traverse the grid; `getAvailableSeats()` counts free seats; `displaySeatMap()` prints `[05]` for free and `[XX]` for booked seats.

#### `Seat`
- **Purpose:** seat number, type (Window/Aisle) and whether it is free.
- **Why:** a seat knows its own state; `book()` refuses a seat that is already taken.
- **Key members:** `book()` (throws `SeatNotAvailableException`), `release()`, `isAvailable()`.

### D. Booking management

#### `Ticket` (implements `Displayable`)
- **Purpose:** one passenger, one seat: PNR, booking ID, `Passenger`, `Train`, `Coach`, `Seat`, fare and status.
- **Why:** this is what PNR status and cancellation work on.
- **Key members:** status constants `PENDING`, `CONFIRMED`, `CANCELLED`; two constructors; `getTicketDetails()` builds the printed ticket with `StringBuilder`.

#### `Booking`
- **Purpose:** one reservation request: booking ID (static counter), date, `Passenger[]`, `Ticket[]`, `Payment` and status.
- **Why:** groups the tickets of up to four passengers and tracks payment.
- **Key members:** two constructors; `refreshStatus()` (the single place that decides PENDING / CONFIRMED / CANCELLED); `getActiveTicketCount()`; `isPaid()`.

#### `Payment`
- **Purpose:** a simulated payment: ID (static counter), amount, mode, status, failure reason, refunded amount.
- **Why:** keeps payment rules in one place and makes success/failure testable.
- **Key members:** two constructors; `process(reference)` applies the simulation rules; `addRefund()`; `getDetails()` (`StringBuilder`).

### E. Business logic

#### `FareCalculator`
- **Purpose:** all fare maths (see the fare table above).
- **Why:** a price change should mean editing one class only.
- **Key members:** three overloaded `calculateFare(...)` methods; `getDiscountRate(age)`; `calculateTotalAmount(Ticket[])` (ignores cancelled tickets).

#### `TrainService`
- **Purpose:** displays and searches trains, stations and routes; shows seat availability.
- **Why:** read-only lookup logic for the railway network.
- **Key members:** `searchTrains()` (throws `TrainNotFoundException`), `findTrainByNumber()`, `displayTrainDetails()`, `checkSeatAvailability()` and `printAvailability(Reservable)` (polymorphism), `printSummaries(Displayable[])` (polymorphism), `searchRoutes()`.

#### `BookingService`
- **Purpose:** the core rules: validate a booking request, check seat choices, allocate seats, create tickets and bookings, take payments, cancel tickets.
- **Why:** all rules that change data are in one place.
- **Key members:** `validateBookingRequest()`, `checkSeatChoice()`, `createBooking()` (rolls back seats if one fails), `makePayment()` (`finally` counts attempts), `checkCancellable()`, `cancelTicket()`.
- **Interacts with:** `TicketService`, `FareCalculator`, `Booking`, `Ticket`, `Coach`, `Payment`.

#### `TicketService`
- **Purpose:** stores all tickets in an array, generates PNRs (`PNR100001`, `PNR100002`...) and answers PNR queries.
- **Key members:** static `nextPnrNumber`, `generatePnr()`, `findTicketByPnr()` (throws `InvalidPNRException`), `checkPnrStatus()`.

#### `InputValidator`
- **Purpose:** checks every piece of user input before it is used.
- **Why:** one place for all validation rules; every method is `static` because it keeps no data.
- **Key members:** `parseInteger()`, `validateName()`, `validateAge()`, `validateGender()`, `validateTrainNumber()`, `validateStationInput()`, `validateSourceAndDestination()`, `validatePnr()`.

#### `ReportService`
- **Purpose:** all report-style output: booking lists (all / confirmed / cancelled / pending), booking details, passenger lists and statistics.
- **Why:** keeps printing of reports away from the classes that change bookings. It never modifies anything.

### F. Interfaces

| Interface | Methods | Implemented by | Why |
|---|---|---|---|
| `Reservable` | `getTotalSeats()`, `getAvailableSeats()`, `hasAvailableSeats()` | `Train`, `Coach` | Both own seats; one method in `TrainService` can print the availability of either |
| `Displayable` | `getSummary()` | `Station`, `Route`, `Train`, `Ticket` | One method can print a table of any of them |

### G. Custom exceptions

| Exception | Kind | Thrown when |
|---|---|---|
| `TrainNotFoundException` | checked | no train matches a search or a train number |
| `SeatNotAvailableException` | checked | seat is taken / does not exist, or the train has too few free seats |
| `InvalidPNRException` | checked | PNR has a wrong format, or no ticket has that PNR |
| `InvalidPassengerException` | **unchecked** | name, age or gender is invalid |

---

## 6. OOP Concepts Used

| Concept | Example in the code |
|---|---|
| Classes and objects | `Train`, `Ticket`, `Booking` objects created in `RailwaySystem` and `BookingService` |
| Methods | every class; overloaded `FareCalculator.calculateFare(...)` (3 versions) |
| Constructors | every model class |
| **Constructor overloading** | `Route` (3 or 4 arguments), `Coach`, `Train`, `Ticket`, `Booking`, `Payment` |
| `this` keyword | `this.name = name` in constructors; `this(...)` chains, e.g. `Route(source, destination, distance)` calls the 4-argument constructor |
| `static` keyword | counters `Passenger.nextPassengerId`, `Booking.nextBookingId`, `Payment.nextPaymentId`, `TicketService.nextPnrNumber`; constants such as `Ticket.STATUS_CONFIRMED`; static methods in `InputValidator`; `Train.createDefaultCoaches()`; `getTableHeader()` methods |
| Encapsulation | all fields are `private`; access through getters/setters and behaviour methods |
| Inheritance | `Passenger extends User`; the four exceptions extend `Exception` / `RuntimeException` |
| Method overriding | `Passenger.getDetails()`; `getSummary()` in four classes; `getTotalSeats()` etc. in `Train` and `Coach` |
| Runtime polymorphism | `ReportService`: `User person = passengers[i]; person.getDetails()`; `TrainService.printAvailability(Reservable)` receives a `Train` or a `Coach`; `printSummaries(Displayable[])` prints trains, stations and routes |
| Abstraction / abstract class | `User` with abstract `getDetails()` |
| Interfaces | `Reservable`, `Displayable` |
| Object modelling (has-a) | `Booking` has `Passenger[]`/`Ticket[]`/`Payment`; `Ticket` has `Train`, `Coach`, `Seat`; `Train` has `Route` and `Coach[]`; `Route` has two `Station`s |

## 7. Arrays and Strings Concepts Used

### Arrays

| Data | Array | Where |
|---|---|---|
| Stations | `Station[]` | `RailwaySystem`, `TrainService` |
| Routes | `Route[]` | `RailwaySystem`, `TrainService` |
| Trains | `Train[]` | `RailwaySystem`, `TrainService` |
| Coaches | `Coach[]` | `Train` |
| **Seats (2-D)** | `Seat[][]` | `Coach` - rows x columns, filled with nested loops |
| Passengers | `Passenger[]` | `Booking`, `Main.bookTicket()` |
| Tickets | `Ticket[]` | `Booking`, `TicketService` |
| Bookings | `Booking[]` | `BookingService` |
| Seat choices | `Coach[]` + `int[]` | `Main.bookTicket()` |

Array techniques: traversal with `for` loops everywhere; searching (`findTrainByNumber`, `findTicketByPnr`, `Coach.findSeat`); comparison (`checkSeatChoice` compares the chosen coach and seat with earlier choices); copying into an exactly-sized array (`searchTrains`, `searchRoutes`).
Arrays have a fixed size, so `BookingService` and `TicketService` keep a counter (`bookingCount`, `ticketCount`) and refuse new data when the array is full.

### Strings

| Topic | Example |
|---|---|
| `trim()` | `InputValidator.validateName()`, `Station.matches()` |
| `equals()` | `Ticket.isCancelled()`, `Payment.process()` (mode), `TicketService.findTicketByPnr()` |
| `equalsIgnoreCase()` | `Station.matches()`, `Main.readYesNo()` |
| `isEmpty()` | `InputValidator` (empty name, empty PNR, empty station) |
| `substring()` | `Route.calculateArrival()`, `Payment.process()` (last 4 card digits), `InputValidator.toTitleCase()`, `validatePnr()` |
| `toUpperCase()` / `toLowerCase()` | `Station` (code), `validatePnr()`, `validateGender()`, `toTitleCase()` |
| Other methods | `split`, `replace`, `charAt`, `length`, `indexOf`, `lastIndexOf`, `contains`, `endsWith` |
| String immutability | `trim()`, `toUpperCase()` and `replace()` never change the original; they return a new `String`, which the code stores in a variable (`String cleaned = name.trim();`) |
| String comparison | `equals()` for text, `==` only for object identity (`BookingService.checkSeatChoice` compares two `Coach` objects) |
| Formatting | `printf` and `String.format` in every table |
| `StringBuilder` | `Ticket.getTicketDetails()`, `Payment.getDetails()`, `ReportService.displayBookingDetails()`, `InputValidator.toTitleCase()` |
| Wrapper classes | `Integer.valueOf()` with auto-unboxing and `Integer.parseInt()` (`InputValidator`, `Route`); `Double.valueOf()` (`ReportService.displayStatistics()`); `Character.isLetter()` / `isDigit()` (validation) |

## 8. Exception Handling Concepts Used

| Concept | Where |
|---|---|
| `try` / `catch` | every action in `Main` |
| Multiple `catch` blocks | `Main.showTrainDetailsByNumber()` (`TrainNotFoundException`, then `IllegalArgumentException`); `Main.cancelTicket()` |
| **Multi-catch** (`A \| B`) | `Main.bookTicket()`: `catch (TrainNotFoundException \| SeatNotAvailableException e)` and `catch (IllegalArgumentException \| IllegalStateException e)`; `Main.makePaymentFromMenu()` |
| `finally` | `Main.main()` (closes the `Scanner`), `Main.payForBooking()` (prints "Payment session closed"), `BookingService.makePayment()` (counts every payment attempt) |
| `throw` | `Seat.book()`, `InputValidator`, `TrainService.searchTrains()`, `BookingService.checkCancellable()` |
| `throws` | `Coach.bookSeat()`, `TrainService.findTrainByNumber()`, `TicketService.findTicketByPnr()`, `BookingService.createBooking()`, `RailwaySystem.findTrain()` |
| Checked exceptions | `TrainNotFoundException`, `SeatNotAvailableException`, `InvalidPNRException` (the compiler forces you to handle them) |
| Unchecked exceptions | `InvalidPassengerException`, `IllegalArgumentException`, `IllegalStateException`, `NumberFormatException` |
| Custom exceptions | the four classes in `exceptions/` |
| **Exception propagation** | Cancelling: `InputValidator.validatePnr()` -> `TicketService.findTicketByPnr()` -> `BookingService.cancelTicket()` -> `RailwaySystem.cancelTicket()` -> caught in `Main.cancelTicket()`. Also `createBooking()` catches `SeatNotAvailableException`, releases the seats already taken, then re-throws it |
| Exception translation | `parseInteger()` turns `NumberFormatException` into `IllegalArgumentException`; `validateAge()` turns that into `InvalidPassengerException` |
| Never crash on bad input | every `readXxx()` helper loops until valid; `Main.main()` also has a safety-net `catch (RuntimeException e)` and handles the end of input (`NoSuchElementException`) |

## 9. Running the Project in IntelliJ IDEA

1. **Open the project:** `File > Open...` and choose the `E-Rail` folder.
2. **Mark the source folder:** right-click `src` > `Mark Directory as` > `Sources Root` (the folder turns blue).
3. **Set the JDK:** `File > Project Structure > Project`, choose SDK **17 or newer**, and set *Language level* to 17.
4. **Run:** open `src/com/erail/main/Main.java`, click the green arrow next to `main()`, and choose *Run 'Main.main()'*.
5. **Use the program:** click inside the *Run* window at the bottom, type a number and press Enter.

**Running a scripted test inside IntelliJ:** after step 4 once, open `Run > Edit Configurations...`, select *Main*, click *Modify options > Redirect input*, tick it, and choose a file from `tests/inputs/`. Run again: the program reads the file instead of the keyboard.

**From a terminal instead:**

```
javac -d out $(find src -name "*.java")        # Windows PowerShell: javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.erail.main.Main
```

**All tests at once (Mac / Linux / Git Bash):** `bash tests/run_tests.sh`

---

## 10. Sample Terminal Output

### Main menu

```
=============================================
          E-RAIL RAILWAY RESERVATION
=============================================

1. Train Management
2. Passenger Management
3. Search Trains
4. Search Routes
5. Book Ticket
6. Make Payment
7. Check PNR Status
8. Cancel Ticket
9. View All Bookings
10. Booking Reports
11. Exit

=============================================

Enter your choice:
```

### Train list

```
AVAILABLE TRAINS
No.    Train Name           From       To         Dep    Arr          Seats  Base Fare
-----------------------------------------------------------------------------------
10101  Capital Express      Delhi      Mumbai     16:30  09:00 (+1d)     48     850.00
10102  Gateway Express      Mumbai     Delhi      17:00  09:30 (+1d)     48     850.00
10301  Pink City Express    Delhi      Jaipur     06:00  11:36           48     250.00
10701  Charminar Mini       Hyderabad  Chennai    10:00  20:00            4     400.00
...
```

### Train details

```
TRAIN 10101 - Capital Express
  Route      : New Delhi -> Mumbai Central
  Departure  : 16:30
  Arrival    : 09:00 (+1d)
  Distance   : 1384 km
  Duration   : 16h 30m

  Fare per passenger (adult):
    S1         Sleeper      Rs.   850.00
    B1         AC 3-Tier    Rs.  1275.00
    A1         AC 2-Tier    Rs.  1700.00
  Discounts: children up to 12 years 50% off, seniors 60+ years 40% off

  Seat availability:
    Whole train                  :  48 of  48 seats free
    Coach S1 (Sleeper)           :  20 of  20 seats free
    Coach B1 (AC 3-Tier)         :  16 of  16 seats free
    Coach A1 (AC 2-Tier)         :  12 of  12 seats free
```

### Booking a ticket (3 passengers, one tries a seat already chosen)

```
--- Passenger 2 of 3 ---
  Name   : Tina Verma
  Age    : 10
  Gender (M/F/O): F

Coaches on train 10301:
  No.  Coach    Type         Free      Your fare
  1    S1       Sleeper      20     Rs.   125.00
  2    B1       AC 3-Tier    16     Rs.   187.50
  3    A1       AC 2-Tier    12     Rs.   250.00
Select coach number 1-3 (0 to cancel the booking): 1

Seat map of coach S1 (Sleeper)
 W    A    A    W
[01] [02] [03] [04]
[05] [06] [07] [08]
...
W = Window, A = Aisle, [XX] = already booked
Enter seat number (0 to choose another coach): 1
Cannot select that seat: Seat 1 is already selected for passenger 1.
```

### Booking created (not yet confirmed)

```
Seats reserved. Your booking is created but NOT confirmed until payment is made.

------------------ BOOKING 1001 ------------------
 Booking date   : 2026-11-25
 Train          : 10301 - Pink City Express
 Booking status : PENDING
 Payment status : UNPAID
 Amount (active): Rs. 600.00

PNR        Passenger                 Train  Coach  Seat        Fare  Status
-----------------------------------------------------------------------
PNR100001  Amit Verma                10301  S1     1         250.00  PENDING
PNR100002  Tina Verma                10301  S1     2         125.00  PENDING
PNR100003  Dadaji Verma              10301  B1     4         225.00  PENDING
```

### Payment and confirmed ticket

```
PAYMENT RESULT
 Payment ID    : 5001
 Mode          : Card
 Reference     : XXXX-XXXX-XXXX-1111
 Amount        : Rs. 600.00
 Status        : SUCCESS

Payment successful. Booking 1001 is CONFIRMED.

=========================== E-RAIL TICKET ===========================
 PNR           : PNR100001
 Booking ID    : 1001
 Status        : CONFIRMED
---------------------------------------------------------------------
 Passenger     : P001 | Amit Verma | Age 35 | Male | Adult
 Train         : 10301 - Pink City Express
 Journey       : New Delhi (NDL) -> Jaipur Junction (JAI)
 Departure     : 06:00
 Arrival       : 11:36
 Distance      : 308 km   Duration: 5h 36m
 Coach / Seat  : S1 (Sleeper) / seat 1 (Window)
 Fare          : Rs. 250.00
=====================================================================
```

### Failed payment, cancellation and errors

```
 Status        : FAILED
 Reason        : Card declined by the bank (simulated).
Payment failed. The booking stays PENDING and the seats stay reserved.

Ticket PNR100001 has been CANCELLED.
Seat 5 in coach S1 is available again.
Refund of Rs. 850.00 will be returned to the original payment mode (simulated).

Cannot cancel: Ticket PNR100001 is already cancelled.
Invalid PNR: Invalid PNR format. A PNR looks like PNR100001.
Invalid choice: Menu choice must be between 1 and 11.
```

### Statistics

```
BOOKING STATISTICS
---------------------------------------------
 Total bookings        : 1
   Confirmed           : 1
   Pending (unpaid)    : 0
   Cancelled           : 0
 Total tickets issued  : 3
   Active tickets      : 3
   Cancelled tickets   : 0
 Payment attempts      : 1
 Revenue (after refunds): Rs. 600.00
 Average per booking   : Rs. 600.00
```

---

## 11. Testing Scenarios

Every scenario has a ready-made input file in `tests/inputs/`. Each file is a list of the lines you would type, one per line, so you can also type the same lines by hand.
Each run starts with fresh data (the program keeps data in memory only), so scenarios that need an existing ticket first book and pay for one.

Run them all with `bash tests/run_tests.sh`; the script checks that the expected sentence appears and that no Java stack trace appears.

| # | Scenario | Input file | What you type (summary) | Expected result | How it works in the code |
|---|---|---|---|---|---|
| 1 | Successful train search | `test01_search_success.txt` | `3`, `1`, `Delhi`, `Mumbai` | `1 train(s) found:` and the row for train 10101 | `Route.connects()` calls `Station.matches()` on both stations; matching trains are copied to a result array |
| 2 | Unsuccessful train search | `test02_search_failure.txt` | `3`, `1`, `Delhi`, `Chennai` (also: same place twice, digits in the name, empty text) | `No trains found from Delhi to Chennai.`; the other inputs give `Source and destination cannot be the same.`, `... only letters and spaces.`, `... cannot be empty.` | `TrainService.searchTrains()` throws `TrainNotFoundException` when the match count is 0; `InputValidator` throws `IllegalArgumentException` for bad text |
| 3 | Successful ticket booking | `test03_booking_success.txt` | `5`, `10101`, `1` passenger, `rahul sharma`, `21`, `m`, coach `1`, seat `5`, `N` (pay later) | `Seats reserved.`, booking 1001 with PNR100001 as `PENDING`; the name is tidied to `Rahul Sharma` | `BookingService.createBooking()` books the seat, creates the `Ticket` and `Booking`, `FareCalculator` gives the fare |
| 4 | Booking when seats are unavailable | `test04_seats_unavailable.txt` | book all 4 seats of the tiny train 10701, then try to book it again | second attempt: `Train 10701 is fully booked. No seats available.` | `BookingService.validateBookingRequest()` throws `SeatNotAvailableException`; caught by the multi-catch in `Main.bookTicket()`. (Asking for more passengers than free seats gives `Only N seat(s) available...`) |
| 5 | Invalid passenger age | `test05_invalid_age.txt` | ages `abc`, `0`, `150`, `-5`, empty, then `21` | `Age must be a whole number.`, `Age must be between 1 and 120.`, `Age cannot be empty.`, then the program accepts 21 | `InputValidator.validateAge()` -> `parseInteger()`; the error becomes an `InvalidPassengerException` caught in `Main.readValidAge()`, which asks again |
| 6 | Invalid passenger name | `test06_invalid_name.txt` | names empty, `R2D2`, `A`, `Priya@Patel`, then `priya patel` | `Name cannot be empty.`, `Name can contain only letters and spaces.`, `Name must be 2 to 25 characters long.`, then accepted as `Priya Patel` | `InputValidator.validateName()` checks each character with `Character.isLetter()` |
| 7 | Successful payment | `test07_payment_success.txt` | book, `Y` (pay now), `2` (UPI), `rahul@upi` | `Status : SUCCESS`, `Booking 1001 is CONFIRMED`, ticket printed as `CONFIRMED` | `Payment.process()` accepts the UPI ID; `BookingService.makePayment()` sets the tickets to `CONFIRMED`; `Booking.refreshStatus()` updates the booking |
| 8 | Failed payment | `test08_payment_failed.txt` | pay by card `1234567890123450` (ends in 0), then retry through option 6 with `1234 5678 9012 3456` | first: `FAILED - Card declined by the bank (simulated).` booking stays `PENDING`; retry: `SUCCESS`, reference shown as `XXXX-XXXX-XXXX-3456` | `Payment.process()` returns false; the seats stay reserved; the second attempt creates a new `Payment` |
| 9 | Successful PNR search | `test09_pnr_success.txt` | book and pay, then `7` and `pnr100001` (lower case is accepted) | full ticket: passenger, train, journey, status `CONFIRMED` | `InputValidator.validatePnr()` converts to upper case; `TicketService.findTicketByPnr()` searches the array |
| 10 | Invalid PNR search | `test10_pnr_invalid.txt` | `ABC`, `PNR999999`, empty, `PNR12345X` | `Invalid PNR format...`, `No ticket found for PNR PNR999999.`, `PNR cannot be empty.` | wrong format and unknown PNR both throw `InvalidPNRException`; caught in `Main.checkPnrStatus()` |
| 11 | Successful cancellation | `test11_cancel_success.txt` | book, pay, `8`, `PNR100001`, `Y` | `Ticket PNR100001 has been CANCELLED.`, `Seat 5 in coach S1 is available again.`, refund line | `BookingService.cancelTicket()` calls `seat.release()` (available seat counts update automatically because they are counted from the seats), sets the status, records the refund |
| 12 | Cancelling an already cancelled ticket | `test12_cancel_twice.txt` | as test 11, then `8`, `PNR100001` again | `Cannot cancel: Ticket PNR100001 is already cancelled.` | `BookingService.checkCancellable()` throws `IllegalStateException` (unchecked) |
| 13 | Invalid menu input | `test13_invalid_menu.txt` | `abc`, `0`, `12`, empty, `-1`, `1.5`, `99` | `Menu choice must be a whole number.` / `must be between 1 and 11.` / `cannot be empty.`; the menu keeps working | `readMenuChoice()` loops until `parseInteger()` accepts the input |
| 14 | Booking with invalid train number | `test14_invalid_train.txt` | `5`, then `99999`, `abcde`, `123`, empty | `No train found with number 99999.`, `Train number must be a whole number.`, `... between 10000 and 99999.`, `... cannot be empty.` | `validateTrainNumber()` rejects bad format; `findTrainByNumber()` throws `TrainNotFoundException` for a valid but unknown number |
| 15 | Extra: full demo | `test15_full_demo.txt` | 3 passengers (adult, child, senior) in two coaches, a duplicate seat, card payment, passenger lists, seat map, route search, reports | child pays 50%, senior 40% off; duplicate seat rejected; seat map shows `[XX]`; reports and statistics are consistent | `FareCalculator`, `checkSeatChoice()`, `Coach.displaySeatMap()`, `ReportService` |
| 16 | Extra: pay later, partial cancel, refund | `test16_booking_pay_later.txt` | book 2 passengers unpaid, cancel one, pay the rest, try to pay again, cancel the paid ticket | payable amount drops from 700 to 350; `Booking 1001 is already paid.`; refund recorded; booking becomes `CANCELLED`; revenue Rs. 0.00 | `FareCalculator.calculateTotalAmount()` ignores cancelled tickets; `Booking.refreshStatus()` |

Besides these, `BookingService.createBooking()` has a rollback path: if a seat cannot be booked in the middle of a booking, the seats already taken are released and the exception is re-thrown (exception propagation).

---

## 12. Syllabus Mapping Table

| Lecture topic | Where it is implemented |
|---|---|
| **Unit 1: Object-Oriented Programming** | |
| Classes and objects | `Train`, `Ticket`, `Booking`...; objects created in `RailwaySystem.createTrains()` |
| Methods | all classes; overloaded `FareCalculator.calculateFare()` |
| Constructors | all model classes |
| Constructor overloading | `Route`, `Coach`, `Train`, `Ticket`, `Booking`, `Payment` |
| `this` keyword | `this.field = field` in constructors; `this(...)` in `Route`, `Coach`, `Train`, `Ticket`, `Booking`, `Payment` |
| `static` keyword | `Passenger.nextPassengerId`, `Booking.nextBookingId`, `Payment.nextPaymentId`, `TicketService.nextPnrNumber`; `InputValidator` methods; `Train.createDefaultCoaches()`; constants like `Coach.TYPE_SLEEPER`; `Main` fields and methods |
| Encapsulation | private fields + getters/setters in every model class |
| Inheritance | `Passenger extends User` |
| Method overriding | `Passenger.getDetails()`; `getSummary()` in `Station`, `Route`, `Train`, `Ticket`; interface methods in `Train` and `Coach` |
| Runtime polymorphism | `ReportService.displayPassengersOfBooking()` (`User` reference), `TrainService.printAvailability(Reservable)`, `TrainService.printSummaries(Displayable[])` |
| Abstraction / abstract classes | `User` |
| Interfaces | `Reservable`, `Displayable` |
| Object modelling | `Booking` -> `Passenger[]`, `Ticket[]`, `Payment`; `Ticket` -> `Train`, `Coach`, `Seat`; `Train` -> `Route`, `Coach[]`; `Route` -> `Station` |
| **Unit 2: Arrays and Strings** | |
| One-dimensional arrays | `Train[]`, `Station[]`, `Route[]`, `Passenger[]`, `Ticket[]`, `Booking[]`, `Coach[]`, `int[]` |
| Multidimensional arrays | `Seat[][]` in `Coach` |
| Array traversal | `Train.getAvailableSeats()`, `Coach.getAvailableSeats()`, `TrainService.printSummaries()`, `ReportService.displayBookings()` |
| Searching and comparison | `TrainService.findTrainByNumber()`, `TicketService.findTicketByPnr()`, `Coach.findSeat()`, `BookingService.checkSeatChoice()` |
| String immutability | `InputValidator.validateName()` (`name.trim()` returns a new String) and `toTitleCase()` |
| String comparison | `equals()` / `equalsIgnoreCase()` in `Station.matches()`, `Ticket.isCancelled()`, `Main.readYesNo()` |
| String manipulation | `Route.calculateArrival()` (`substring`), `InputValidator.toTitleCase()` (`split`, `substring`, `toUpperCase`, `toLowerCase`), `Payment.process()` (`replace`, `endsWith`) |
| String formatting | `printf` / `String.format` in all tables, `Ticket.getSummary()` |
| `StringBuilder` | `Ticket.getTicketDetails()`, `Payment.getDetails()`, `ReportService.displayBookingDetails()` |
| Wrapper classes | `Integer.valueOf()`, `Integer.parseInt()`, `Double.valueOf()`, `Character.isLetter()`, `Character.isDigit()` |
| **Unit 3: Exception Handling** | |
| `try` / `catch` | `Main` (all actions) |
| `finally` | `Main.main()`, `Main.payForBooking()`, `BookingService.makePayment()` |
| Multiple catch | `Main.showTrainDetailsByNumber()`, `Main.cancelTicket()` |
| Multi-catch | `Main.bookTicket()`, `Main.makePaymentFromMenu()` |
| Checked exceptions | `TrainNotFoundException`, `SeatNotAvailableException`, `InvalidPNRException` |
| Unchecked exceptions | `InvalidPassengerException`, `IllegalArgumentException`, `IllegalStateException` |
| Exception propagation | cancel chain `validatePnr` -> `findTicketByPnr` -> `cancelTicket` -> `RailwaySystem.cancelTicket` -> `Main.cancelTicket`; `createBooking()` rollback and re-throw |
| `throw` | `Seat.book()`, `InputValidator`, `TrainService.searchTrains()`, `BookingService.checkCancellable()` |
| `throws` | `Coach.bookSeat()`, `TicketService.findTicketByPnr()`, `BookingService.createBooking()`, `RailwaySystem.findTrain()` |
| Custom exceptions | `exceptions` package (4 classes) |
| **Unit 4: Integrated Coding Practice** | |
| Input validation | `InputValidator`; `Main.readInt()`, `readMenuChoice()`, `readValidName()` ... |
| Debugging-friendly code | small methods, descriptive error messages that name the field and the rule, constants instead of magic numbers |
| Multiple console operations | 11-option main menu with five submenus |
| Proper exception handling | every user action is wrapped; the program only ends when the user chooses Exit or the input ends |
| Meaningful test cases | `tests/inputs/` (16 scenarios) and `tests/run_tests.sh` |
| Clean program structure | six packages, one responsibility per class |

---

## 13. Possible Extensions (outside the current scope)

Saving data to a file, a travel date per ticket, waiting lists and a login system would all be natural next steps, but they need topics from later lectures (file handling, collections), so they are deliberately not included.
