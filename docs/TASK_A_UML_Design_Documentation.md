# TASK A: SYSTEM DESIGN WITH UML DIAGRAMS

## Ocean View Resort — Room Reservation System

**Module:** Advanced Programming  
**University:** Cardiff Metropolitan University  

---

## Table of Contents

2.1 System Overview and Requirements  
2.2 UML Diagrams  
&emsp;2.2.1 Use Case Diagram: User Interactions (Staff / Manager / Administrator)  
&emsp;2.2.2 Class Diagram: Object-Oriented Design with Multiplicity and Relationships  
&emsp;&emsp;Service Layer  
&emsp;&emsp;Repository Layer  
&emsp;2.2.3 Sequence Diagrams: Authentication, Reservation CRUD, Bill Calculation and Notification Flow  
2.3 Design Evaluation  
&emsp;2.3.1 Justification of Object-Oriented Concepts (Encapsulation, Aggregation, Composition)  
&emsp;2.3.2 Critical Reflection on Design Fluency and Assumptions  
&emsp;2.3.3 Alignment with Industry Standards  
References  

---

## 2.1 System Overview and Requirements

Ocean View Resort is a beachside hotel situated in Galle, Sri Lanka, requiring a computerised room reservation system to replace its existing manual booking processes. The current paper-based workflow is prone to double-bookings, delayed billing, and inconsistent guest records — inefficiencies that a structured software solution must eliminate. This document presents the Unified Modelling Language (UML) design documentation for the proposed system, serving as the architectural blueprint that guided and validated the implementation.

The system addresses six core functional requirements derived from the project brief:

1. **User Authentication** — secure login with username and password, role-based access control (Staff, Manager, Administrator), session management, and password hashing.
2. **Add New Reservation** — storing reservation number, guest name, address, contact number, room type, check-in/check-out dates, number of guests, and special requests.
3. **Display Reservation Details** — searching, listing, filtering by status, and viewing comprehensive reservation records with associated guest and room data.
4. **Calculate and Print Bill** — deriving the total amount from nightly rate and stay duration, processing payments via cash or card, and generating print-ready receipts.
5. **Help Section** — contextual guidance accessible to all authenticated users.
6. **Exit System** — secure logout with session invalidation.

Beyond these core requirements, the implementation extends functionality to include guest management (add, update, search, delete), room management (add, update, toggle maintenance, delete), staff account administration (create, activate/deactivate, reset password), reporting (monthly/weekly reports, CSV export), and asynchronous email notifications for reservation confirmations, payment receipts, and staff credential distribution.

The system is implemented as a Java web application following a **three-tier architecture**: a presentation layer (JSP pages and Servlet controllers), a business logic layer (Service interfaces and implementations), and a data access layer (DAO pattern with MySQL via JDBC). Appropriate data structures — `List<T>` for collections, `Map<String, Object>` for report aggregation, `UUID` for entity identification — and established design patterns (Singleton, Strategy, DAO, Façade, Front Controller) underpin the architecture. This design documentation comprises three categories of UML diagrams — a use case diagram, a class diagram, and four sequence diagrams — each accompanied by detailed analysis and justification. Together, these diagrams provide complementary structural, behavioural, and functional views of the system, demonstrating adherence to object-oriented design principles and UML 2.5 notation standards (OMG, 2017).

---

## 2.2 UML Diagrams

### 2.2.1 Use Case Diagram: User Interactions (Staff / Manager / Administrator)

**[Figure 1: Use Case Diagram for Ocean View Resort Reservation System]**

*This diagram was created using PlantUML following UML 2.5 notation standards. The PlantUML source file is located at `docs/diagrams/use-case-diagram.puml`. The diagram shows the complete system boundary for the Ocean View Resort Reservation System, depicting all actors, use cases, and their relationships including `<<include>>` and `<<extend>>` stereotypes. To render the diagram, open the `.puml` file in any PlantUML-compatible tool (e.g., PlantUML Online Server at plantuml.com, IntelliJ IDEA with PlantUML plugin, or Visual Studio Code with the PlantUML extension) and export as PNG or SVG.*

#### Actors and Their Roles

The use case diagram identifies twenty-eight distinct use cases organised within the system boundary. Three human actors and one system actor were identified through analysis of the implemented role-based access control:

- **Staff** represents front-desk employees who perform day-to-day operations including guest registration, reservation creation, check-in/check-out processing, and payment handling. In the implementation, this corresponds to users with the `STAFF` role, whose access is validated by `SessionUtil.hasRole()` and enforced by `AuthFilter`. Staff members interact with nineteen use cases spanning authentication, guest management, reservation management, billing, dashboard viewing, and the help section.

- **Manager** inherits all Staff capabilities through UML actor generalisation and additionally accesses reporting functions (monthly reports, weekly reports, CSV export) and room management. This inheritance is implemented through the `MANAGER` role enumeration in the database schema, with access checks in `ReportController` permitting both `MANAGER` and `ADMIN` roles.

- **System Administrator** inherits all Manager capabilities and additionally manages staff accounts (creation, activation/deactivation, password resets) and can delete guests and rooms. The `ADMIN` role in the implementation has unrestricted access, as verified by `StaffController` which checks `SessionUtil.hasRole(req, "ADMIN")` before permitting access.

- **Email Service** is a system actor representing the external SMTP service consumed by `EmailUtil`. This actor receives asynchronous email notifications for reservation confirmations, payment receipts, and staff credential distribution.

The actor inheritance hierarchy (Staff ← Manager ← Administrator) accurately models the implemented privilege escalation where each higher role subsumes the permissions of all roles below it. This design avoids duplicating use case associations across actors — a Manager inherits all Staff connections without explicit re-declaration in the diagram, keeping the notation clean and maintainable.

#### Use Case Relationships

Five **`<<include>>`** relationships model mandatory functional dependencies that cannot be bypassed:

1. **Login includes Validate Credentials** — Every authentication attempt necessarily invokes credential validation via `PasswordUtil.check()` and `UserDAOImpl.findByUsername()`. This is an invariant step embedded within the login process.

2. **Add New Reservation includes Check Room Availability** — The `ReservationServiceImpl.createReservation()` method mandatorily verifies room existence and capacity via `RoomDAO.findById()`, and the database trigger `trg_prevent_double_booking` enforces date-range availability. Room availability checking is therefore an integral, non-optional part of reservation creation.

3. **Process Payment includes Calculate Bill** — Payment processing in `PaymentServiceImpl.processPayment()` retrieves the reservation's `totalAmount` (calculated by the database trigger `trg_calc_total` as nights × rate_per_night) to set the payment amount. Bill calculation is thus a prerequisite for payment processing.

4. **Check-In includes View Reservation** — The check-in process in `ReservationServiceImpl.checkIn()` must first retrieve and validate the reservation's current status before transitioning it to `CHECKED_IN`.

5. **Check-Out includes View Reservation** — Similarly, checkout requires loading the reservation to verify it is in `CHECKED_IN` status before transitioning to `CHECKED_OUT`.

Nine **`<<extend>>`** relationships model conditional or optional behaviours triggered only under specific circumstances:

- **Send Email Notification extends** Add New Reservation, Confirm Reservation, and Process Payment — Email dispatch occurs conditionally only when the guest has a valid email address, implemented via `opt` checks such as `if (guest != null && guest.getEmail() != null)` in the service layer.

- **Print Bill extends Process Payment** — After successful payment, the staff member may optionally navigate to the receipt view for printing; this is not a mandatory step.

- **Calculate Bill extends View Reservation** — When viewing a reservation, the staff may optionally navigate to the bill view, which is a separate action (`action=bill`).

- **Manage Rooms** is extended by Add Room, Update Room, Delete Room, and Toggle Maintenance — These represent specific sub-operations within the broader room management context, each invoked conditionally based on the staff member's intent.

- **Manage Staff** is extended by Create Staff Account, Toggle Active Status, and Reset Password — These are conditional administrative actions within staff management.

- **Export CSV extends Monthly Report** — CSV export is an optional action available after generating a monthly report.

The selection of use cases reflects the complete operational workflow of a hotel reservation system. Core transactional use cases such as "Add New Reservation," "Process Payment," and "Calculate Bill" represent the primary value-delivering functions. Supporting use cases including "Search Guests," "View Dashboard," and "View Help Section" enhance usability and operational efficiency. Administrative use cases such as "Manage Staff Accounts" and "Manage Rooms" provide system governance capabilities essential for long-term operation.

**Assumptions:** It was assumed that all system users must authenticate before accessing any protected resource, enforced uniformly by `AuthFilter`. It was assumed that guests do not interact with the system directly; all operations are mediated by staff members. Managers have read-only access to reports but cannot modify reservation or payment data beyond their Staff-level permissions.

---

### 2.2.2 Class Diagram: Object-Oriented Design with Multiplicity and Relationships

**[Figure 2: Class Diagram for Ocean View Resort Reservation System]**

*This class diagram was developed using PlantUML following UML 2.5 notation standards. The PlantUML source file is located at `docs/diagrams/class-diagram.puml`. The diagram illustrates the complete class structure across seven packages, showing all attributes with data types, methods with full signatures, access modifiers (+ public, - private, # protected), and relationships including associations, aggregation, composition, dependency, and interface realisation. Multiplicity and navigability are annotated on all association lines. To render, open the `.puml` file in a PlantUML-compatible environment and export as PNG or SVG.*

The system comprises forty-three classes and interfaces organised across seven packages, each corresponding to a distinct architectural layer. The following subsections detail the two most architecturally significant layers — the Service Layer and the Repository (DAO) Layer — before discussing the remaining layers and cross-cutting concerns.

#### Service Layer

The service layer (`com.oceanview.service` and `com.oceanview.service.impl`) forms the core of the business logic tier. Five service interfaces define the application's behavioural contracts, and five corresponding implementation classes encapsulate all business rules, validation logic, and cross-cutting concerns such as email notification.

- **`UserService` / `UserServiceImpl`** — manages authentication, registration, staff account creation, password operations (change, reset), and user lifecycle (toggle active, delete). The implementation aggregates `UserDAO` and coordinates with `PasswordUtil` for BCrypt hashing, `ValidationUtil` for input validation, and `EmailUtil` for dispatching staff credentials. A `private generateTempPassword()` helper method encapsulates internal password generation logic.

- **`GuestService` / `GuestServiceImpl`** — handles guest CRUD operations including add, update, search, and delete. The implementation aggregates `GuestDAO` and applies validation rules (non-empty name, valid NIC format) before delegating persistence.

- **`RoomService` / `RoomServiceImpl`** — manages room CRUD operations, availability queries (by date range, by filters), and maintenance toggling. The implementation aggregates `RoomDAO` and supports complex filter-based searching with optional parameters for room type, floor, check-in date, and check-out date.

- **`ReservationService` / `ReservationServiceImpl`** — the most complex service, orchestrating reservation creation, updates, confirmation, cancellation, check-in, check-out, and reporting. This implementation aggregates three DAO interfaces (`ReservationDAO`, `RoomDAO`, `GuestDAO`), demonstrating the **Façade pattern** (Gamma *et al.*, 1994) where a single service method (e.g., `createReservation()`) coordinates validation via `ValidationUtil`, capacity checking via `RoomDAO`, persistence via `ReservationDAO`, and asynchronous email dispatch via `EmailUtil`.

- **`PaymentService` / `PaymentServiceImpl`** — processes payments, checks for duplicate payments, and coordinates with the **Strategy pattern** for payment method validation. This implementation aggregates `PaymentDAO`, `ReservationDAO`, and `GuestDAO`, enabling it to validate the reservation, persist the payment, and send email receipts in a single orchestrated flow.

Each service method returns a **`ServiceResult`** object — a lightweight Result/Either pattern implementation with a `private` constructor and `public static` factory methods (`success()`, `failure()`). This provides a uniform response contract encapsulating success/failure status, a human-readable message, and optional data payload, eliminating the need for exception-based flow control in the controller layer.

The **Strategy design pattern** is realised through the `com.oceanview.payment` package: the `PaymentStrategy` interface declares an `execute(Payment)` method; `CashPaymentStrategy` and `CardPaymentStrategy` provide concrete implementations with distinct validation rules (receipt number ≥ 3 characters for cash; transaction ID ≥ 6 characters for card). The `PaymentContext` class holds a reference to a `PaymentStrategy` and delegates validation via `executePayment()`. This allows the payment validation algorithm to vary independently of the clients that use it, and new payment methods (e.g., bank transfer, mobile payment) can be added by implementing `PaymentStrategy` without modifying existing code — adhering to the Open/Closed Principle (Martin, 2018).

#### Repository Layer

The repository layer follows the **Data Access Object (DAO)** design pattern (Gamma *et al.*, 1994), comprising three sub-packages that together abstract all persistence concerns from the business logic:

**DAO Interfaces (`com.oceanview.dao`)** — Five interfaces (`UserDAO`, `GuestDAO`, `RoomDAO`, `ReservationDAO`, `PaymentDAO`) define data access contracts with method signatures for save, update, delete, and various find operations. The `DAOException` class provides a uniform exception abstraction for all persistence failures, insulating the service layer from JDBC-specific exceptions. This interface-based design enables the data access mechanism to be changed (e.g., from MySQL to PostgreSQL, or from JDBC to JPA) without modifying any business logic.

**DAO Implementations (`com.oceanview.dao.impl`)** — Five concrete classes (`UserDAOImpl`, `GuestDAOImpl`, `RoomDAOImpl`, `ReservationDAOImpl`, `PaymentDAOImpl`) provide MySQL-specific persistence logic using parameterised JDBC queries. Each implementation obtains database connections from `DBConnection.getInstance()` — a **Singleton** with lazy initialisation and automatic reconnection, ensuring a single shared connection instance across the application. Each DAO implementation holds a **composed** `Mapper` instance (composition relationship — filled diamond), instantiated within the DAO constructor with no independent lifecycle outside its parent DAO.

**Mapper Layer (`com.oceanview.mapper`)** — The generic `Mapper<T>` interface and five concrete implementations (`UserMapper`, `GuestMapper`, `RoomMapper`, `ReservationMapper`, `PaymentMapper`) implement the **Row Data Gateway** mapping pattern. Each mapper translates a JDBC `ResultSet` row into a domain entity object, separating the concern of data interpretation from data access logic. This improves testability (mappers can be unit-tested independently) and reduces code duplication across DAO methods that share the same mapping logic.

#### Additional Layers

**Model Layer (`com.oceanview.model`)** — Five entity classes (`User`, `Guest`, `Room`, `Reservation`, `Payment`) encapsulate domain data. Each implements `Serializable` for HTTP session storage and follows the JavaBean convention with `private` attributes and `public` getter/setter methods. The `Reservation` class includes a computed `getNights()` method demonstrating derived attribute calculation. Room types are modelled as enumerated string values (`STANDARD`, `DELUXE`, `SUITE`, `PENTHOUSE`) rather than a class hierarchy, as the type primarily affects pricing (data) rather than behaviour (methods) (Bloch, 2018).

**Controller Layer (`com.oceanview.controller`)** — Nine servlet controllers extend `HttpServlet` and implement the **Front Controller** pattern with action-based routing. Each controller depends on service interfaces (not implementations) via dependency arrows, enabling future dependency injection. Private helper methods (`handleLogin()`, `handleCreate()`, etc.) encapsulate action-specific logic, exposing only `doGet()` and `doPost()` as the public interface.

**Utility Layer (`com.oceanview.util`)** — `DBConnection` (Singleton), `PasswordUtil` (BCrypt hashing), `SessionUtil` (session management), `ValidationUtil` (input validation), and `EmailUtil` (asynchronous SMTP) provide cross-cutting infrastructure. `AuthFilter` and `LoggingFilter` in the filter package intercept HTTP requests for authentication enforcement and request logging respectively.

#### Relationships and Multiplicity

The class diagram depicts five categories of UML relationships:

**Associations with Multiplicity:**
- `Guest (1) ——— (0..*) Reservation`: A guest may have zero or many reservations; each reservation belongs to exactly one guest (foreign key `guest_id`).
- `Room (1) ——— (0..*) Reservation`: A room may be booked across many reservations (different date ranges); each reservation references exactly one room (`room_id`).
- `Reservation (1) ——— (0..1) Payment`: Each reservation has at most one payment record (`UNIQUE` constraint on `payments.reservation_id`). A payment cannot exist without a reservation.
- `User (1) ——— (0..*) Reservation`: A staff user creates zero or many reservations (`created_by`).
- `User (1) ——— (0..*) Payment`: A staff user processes zero or many payments (`processed_by`).

**Composition (filled diamond ◆):** DAO implementations compose their respective Mapper instances. The mapper has no independent lifecycle outside its DAO; if the DAO is destroyed, the mapper ceases to exist. This was chosen over aggregation because mappers are instantiated within DAO constructors and are tightly coupled to their parent's lifecycle.

**Aggregation (hollow diamond ◇):** Service implementations aggregate DAO interfaces. The DAO instances have an independent lifecycle and can be shared across services — as seen with `ReservationDAO` being used by both `ReservationServiceImpl` and `PaymentServiceImpl`. Constructor injection supports this sharing pattern.

**Interface Realisation (dashed line with triangle):** All DAO implementations realise their respective interfaces; all service implementations realise their service interfaces; all mappers realise the generic `Mapper<T>` interface; both payment strategies realise `PaymentStrategy`.

**Dependency (dashed arrow):** Controllers depend on service interfaces. Utility classes are used by services and controllers via static method calls.

#### Design Patterns Summary

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Singleton** | `DBConnection.getInstance()` | Ensures a single database connection instance, preventing resource exhaustion |
| **DAO** | `UserDAO`/`UserDAOImpl`, etc. | Abstracts persistence mechanism, enabling database technology changes without business logic modification |
| **Strategy** | `PaymentStrategy`, `PaymentContext`, `CashPaymentStrategy`, `CardPaymentStrategy` | Runtime selection of payment validation algorithm; supports Open/Closed Principle |
| **Façade** | Service layer classes (e.g., `ReservationServiceImpl`) | Simplified interface to complex subsystem interactions involving multiple DAOs and utilities |
| **Row Data Gateway** | `Mapper<T>` interface and implementations | Separates ResultSet-to-object mapping from data access logic |
| **Front Controller** | Servlet controllers with action-based routing | Centralises request handling and dispatching within each functional domain |

**Assumptions:** It was assumed that one guest is associated per reservation, though rooms may physically accommodate multiple guests (tracked by `numGuests`). Each reservation can have at most one payment record, reflecting a single-payment-per-booking model. Room types are fixed enumerations and do not require a separate class hierarchy. The `Mapper<T>` generic interface sufficiently addresses row-mapping without requiring a full ORM framework.

---

### 2.2.3 Sequence Diagrams: Authentication, Reservation CRUD, Bill Calculation and Notification Flow

Four sequence diagrams were developed to trace the system's most critical and architecturally significant interaction flows. These use cases were selected because they collectively demonstrate the full three-tier architecture, all implemented design patterns, and the complete range of UML interaction fragments (`alt`, `opt`, `loop`).

#### Sequence 1: User Authentication

**[Figure 3: Sequence Diagram — User Authentication]**

*This sequence diagram was created using PlantUML. The PlantUML source file is located at `docs/diagrams/sequence-authentication.puml`. The diagram follows the temporal flow of the user authentication process from login form submission through credential validation to session creation or error reporting. To render, open the `.puml` file in a PlantUML-compatible environment and export as PNG or SVG.*

The authentication sequence was selected because it is the gateway to all system functionality and demonstrates the security architecture including password hashing, session management, and filter-based access control.

The flow begins when a user (Staff, Manager, or Administrator) submits a POST request to `/auth` with username and password parameters. The `AuthController` servlet delegates to `UserServiceImpl.authenticate()`, which first validates that neither field is empty using `ValidationUtil.isNullOrEmpty()`. The service then queries the database via `UserDAOImpl.findByUsername()`, which obtains a connection from the `DBConnection` Singleton and executes a parameterised SQL query. The `UserMapper` transforms the `ResultSet` into a `User` domain object.

The diagram employs an **`alt` (alternatives) fragment** with three mutually exclusive branches: (1) user not found — returns a generic "Invalid username or password" message to prevent username enumeration; (2) user is deactivated — returns an account-specific error; (3) user found and active — proceeds to password verification via `PasswordUtil.check()`, which uses BCrypt comparison. A nested `alt` fragment handles password mismatch versus successful match. Upon successful authentication, `UserDAOImpl.updateLastLogin()` records the login timestamp, and `SessionUtil.createSession()` establishes an HTTP session with a 30-minute timeout, storing the `User` object and role.

An **`opt` (optional) fragment** shows the subsequent session-based request flow where `AuthFilter` intercepts all requests and verifies authentication via `SessionUtil.isLoggedIn()` before allowing access to protected resources. The logout flow is also depicted, showing session invalidation via `SessionUtil.invalidate()`.

#### Sequence 2: Create New Reservation (Save with Validation)

**[Figure 4: Sequence Diagram — Create New Reservation]**

*This sequence diagram was created using PlantUML. The PlantUML source file is located at `docs/diagrams/sequence-create-reservation.puml`. The diagram follows the temporal flow from form display through multi-layer validation, database persistence with trigger interaction, and asynchronous email notification. To render, open the `.puml` file in a PlantUML-compatible environment.*

Reservation creation was selected as the system's primary value-delivering operation, demonstrating the complete three-tier flow from controller through service to DAO, including multi-entity validation, database trigger interaction, and asynchronous email notification.

The sequence spans two HTTP interactions. First, a GET request to `/reservations?action=new` triggers `ReservationController.handleNewForm()`, which loads available rooms and all guests from their respective services, generates a CSRF token, and forwards to the `add.jsp` form view. The second interaction is a POST request submitting the reservation data. The controller validates the CSRF token, extracts parameters, and builds a `Reservation` object. `ReservationServiceImpl.createReservation()` performs comprehensive validation: null checks on guest and room IDs, date validation (check-out after check-in, check-in not in the past), guest count validation, and room capacity verification via `RoomDAO.findById()`.

The **`alt` fragment** demonstrates two persistence outcomes: the database trigger `trg_prevent_double_booking` may reject the insert with a "Room already booked" error (caught as `DAOException`), or the insert succeeds and triggers `trg_calc_total` and `trg_reservation_no` automatically compute the `total_amount` and generate the reservation number respectively. A reload via `findById()` retrieves these trigger-generated values.

An **`opt` fragment** shows the conditional email notification: if the guest has an email address, `EmailUtil.sendReservationConfirmation()` dispatches an asynchronous confirmation email on a separate thread, ensuring the HTTP response is not delayed by SMTP communication. This conditional dispatch directly corresponds to the `<<extend>>` relationship between "Send Email Notification" and "Add New Reservation" in the use case diagram.

#### Sequence 3: Calculate Bill and Process Payment

**[Figure 5: Sequence Diagram — Calculate and Print Bill]**

*This sequence diagram was created using PlantUML. The PlantUML source file is located at `docs/diagrams/sequence-calculate-bill.puml`. The diagram follows the complete billing workflow from bill viewing through Strategy-pattern-based payment processing and print-ready receipt generation. To render, open the `.puml` file in a PlantUML-compatible environment.*

Bill calculation and payment was selected because it demonstrates the Strategy design pattern in action, cross-service coordination (Payment, Reservation, Guest, Room services), and the complete financial workflow from calculation through payment to receipt generation.

The sequence comprises three phases. In the **bill viewing phase**, a GET request to `/reservations?action=bill` triggers loading of reservation, guest, room, and existing payment data. The bill amount is the `totalAmount` field, pre-calculated by the database trigger as `DATEDIFF(check_out_date, check_in_date) * rate_per_night`. The `bill.jsp` view displays guest details, room information, stay duration, rate calculations, and the total amount.

The **payment processing phase** begins with a POST to `/payments` containing the reservation ID, payment method (CASH or CARD), reference number, and optional notes. `PaymentServiceImpl.processPayment()` validates the reservation exists and is in a payable status, checks for duplicate payments via `PaymentDAO.existsForReservation()`, then invokes the **Strategy pattern**: `PaymentContext` delegates to either `CashPaymentStrategy` (requiring a receipt number ≥ 3 characters) or `CardPaymentStrategy` (requiring a transaction ID ≥ 6 characters). The **`alt` fragment** in the Strategy execution shows three possible outcomes — cash reference too short, card reference too short, or validation passes — demonstrating **polymorphic behaviour** where the same `execute()` call produces different validation logic depending on the runtime strategy. Upon successful validation, the payment record is persisted and an email receipt is conditionally sent via the `opt` fragment.

The **receipt phase** loads all related entities (payment, reservation, guest, room) and forwards to `receipt.jsp`, providing a print-optimised layout.

#### Sequence 4: Search and Display Reservation Details

**[Figure 6: Sequence Diagram — Search and Display Reservation Details]**

*This sequence diagram was created using PlantUML. The PlantUML source file is located at `docs/diagrams/sequence-search-reservation.puml`. To render, open the `.puml` file in a PlantUML-compatible environment.*

Reservation search and display was selected to demonstrate list retrieval with enrichment, filtering, and the detailed view pattern — common interaction patterns reused across the guest, room, and staff management modules.

This diagram covers three interaction patterns: listing all reservations (with a **`loop` fragment** enriching each reservation with guest name and room number via `GuestService` and `RoomService` calls), filtering reservations by status (e.g., `CONFIRMED`), and viewing a single reservation's full details including associated guest, room, and payment data. The **`alt` fragment** handles the case where a reservation ID is not found, returning an HTTP 404 error page, versus the success path where all related entities are loaded and forwarded to the detail view with a CSRF token for subsequent operations.

#### Interaction Analysis

All messages in the diagrams are **synchronous** (solid arrowheads) reflecting the servlet-based architecture where each request is processed sequentially on a single thread. Email dispatch is the sole asynchronous operation, executed on a separate `Thread` via `EmailUtil.sendAsync()` and modelled using `opt` fragments to indicate conditional execution. The **`alt` fragments** model branching logic corresponding directly to conditional statements in the implementation (e.g., `if (result.isSuccess())`). The **`opt` fragments** model optional behaviours such as email dispatch when the guest has an email address. The **`loop` fragment** in the search diagram models the enrichment iteration in `ReservationController.handleList()`.

**Assumptions:** All controller-to-service communications are synchronous Java method calls within the same JVM. Database operations complete within acceptable time limits and do not require explicit timeout handling. The email service is fire-and-forget; delivery failures do not affect the success of the business transaction.

---

## 2.3 Design Evaluation

### 2.3.1 Justification of Object-Oriented Concepts (Encapsulation, Aggregation, Composition)

The system design consistently applies the four pillars of object-oriented programming, with each principle visibly manifest across the UML diagrams and the implemented codebase.

**Encapsulation** is rigorously enforced throughout the system. All model class attributes (`User`, `Guest`, `Room`, `Reservation`, `Payment`) are declared `private`, accessible only through `public` getter and setter methods — this is visible in the class diagram where every attribute carries the `-` access modifier and every accessor carries `+`. The `DBConnection` class takes encapsulation further by declaring its constructor `private`, preventing external instantiation and enforcing the Singleton pattern. Similarly, `ServiceResult` uses a `private` constructor with `public static` factory methods (`success()`, `failure()`), controlling object creation semantics and ensuring that result objects are always constructed in a valid state. Within controllers, internal helper methods (`handleLogin()`, `handleCreate()`, `handleBill()`, etc.) are `private`, exposing only the `doGet()` and `doPost()` entry points via `protected` access inherited from `HttpServlet`. This ensures that external callers (the servlet container) interact only through the defined public interface.

**Aggregation** (hollow diamond ◇) is applied where components have independent lifecycles and may be shared. Service implementations aggregate DAO interfaces: for example, `ReservationServiceImpl` aggregates `ReservationDAO`, `RoomDAO`, and `GuestDAO`. These DAO instances exist independently and are shared across multiple services — `ReservationDAO` is aggregated by both `ReservationServiceImpl` and `PaymentServiceImpl`, and `GuestDAO` is aggregated by both `GuestServiceImpl` and `PaymentServiceImpl`. Constructor injection (e.g., `ReservationServiceImpl(ReservationDAO, RoomDAO, GuestDAO)`) enables this sharing and supports unit testing with mock objects. The `PaymentContext` aggregates a `PaymentStrategy` reference — the strategy can be swapped at runtime without affecting the context's lifecycle, and strategies exist independently of any particular context instance.

**Composition** (filled diamond ◆) is applied where a part cannot exist without its whole. Each DAO implementation composes its corresponding Mapper instance: `UserDAOImpl` composes `UserMapper`, `GuestDAOImpl` composes `GuestMapper`, and so forth. Mappers are instantiated within the DAO constructor (`private mapper : UserMapper`) and have no purpose or lifecycle outside their parent DAO. If the DAO object is garbage-collected, the mapper is destroyed with it. This is the correct relationship because mappers are implementation details of their DAOs — they are never referenced externally and have no independent reason to exist. Composition was deliberately chosen over aggregation here because the tighter coupling accurately reflects the implementation reality and communicates design intent more clearly to developers maintaining the code.

**Inheritance and Polymorphism** are demonstrated through multiple mechanisms. The actor hierarchy in the use case diagram (Staff ← Manager ← Administrator) models role-based privilege escalation through generalisation. Interface realisation — where `UserDAOImpl` implements `UserDAO`, `UserServiceImpl` implements `UserService`, and both payment strategies implement `PaymentStrategy` — represents the primary form of inheritance in the system, favouring interface inheritance over class inheritance as recommended by Gamma *et al.* (1994). **Polymorphism** is most prominently exhibited by the Strategy pattern: `PaymentContext.executePayment()` invokes different validation logic at runtime depending on whether a `CashPaymentStrategy` or `CardPaymentStrategy` is currently set — the calling code in `PaymentServiceImpl` is entirely agnostic to which concrete strategy is active. Additionally, all service calls from controllers are made through interface references (e.g., `ReservationService` rather than `ReservationServiceImpl`), enabling polymorphic substitution of implementations without modifying controller code.

**Abstraction** is achieved through the layered interface architecture visible in the class diagram: controllers interact only with service interfaces, services interact only with DAO interfaces, and DAOs interact only with the generic `Mapper<T>` interface. Each layer abstracts away its implementation details from the layer above, creating clear boundaries that reduce cognitive load and limit the ripple effect of changes.

### 2.3.2 Critical Reflection on Design Fluency and Assumptions

#### Diagram Integration and Traceability

The three diagram types form a cohesive design narrative with full traceability. The use case diagram identifies *what* the system does (functional scope); the class diagram specifies *how* the system is structured (static architecture); and the sequence diagrams reveal *when* and *in what order* interactions occur (dynamic behaviour). Each use case can be traced to specific controller methods in the class diagram and to sequence diagrams detailing the implementation flow. For example, the "Process Payment" use case maps to `PaymentController.handleProcess()` in the class diagram, which is fully detailed in the bill calculation sequence diagram (Figure 5). Similarly, the `<<include>>` relationship between "Process Payment" and "Calculate Bill" in the use case diagram directly corresponds to the `totalAmount` retrieval in the bill sequence diagram. This multi-view consistency validates the coherence of the design, as recommended by Fowler (2004) for effective UML documentation.

#### Design Trade-offs

Several deliberate trade-offs were made during the design process, each with clear justification:

**Composition over inheritance for Room types:** Rather than creating a class hierarchy (e.g., `StandardRoom extends Room`, `DeluxeRoom extends Room`), room types are modelled as an enumerated `roomType` attribute. This was chosen because room types differ primarily in pricing and amenities (data) rather than behaviour (methods). A class hierarchy would introduce unnecessary complexity for minimal behavioural variation (Bloch, 2018). However, if room types were to acquire distinct behaviours in the future (e.g., different cancellation policies per room type), a class hierarchy or Strategy pattern for room-type-specific logic would be more appropriate.

**DAO pattern vs. ORM framework:** The explicit DAO implementation with JDBC was chosen over an ORM framework (e.g., Hibernate) to maintain educational clarity and fine-grained control over SQL queries, including stored procedure calls and trigger interactions. The trade-off is more boilerplate code but greater transparency and learning value. In a production environment, an ORM would reduce repetitive code.

**Singleton DBConnection vs. Connection Pooling:** The current Singleton maintains a single database connection, which is simpler to implement but represents a scalability bottleneck under concurrent access. In a production environment, a connection pool (e.g., HikariCP) would be essential to handle multiple simultaneous requests. This is the most significant identified improvement area.

**Strategy pattern for payment vs. simple conditional:** The Strategy pattern introduces additional classes (`PaymentStrategy`, `PaymentContext`, two concrete strategies) but provides superior extensibility. A simple `if-else` conditional would have been simpler for two payment methods but would violate the Open/Closed Principle when new methods (e.g., bank transfer, mobile payment) are introduced.

#### Strengths and Weaknesses

**Strengths:** The interface-based design supports the Dependency Inversion Principle (Martin, 2018), enabling implementation substitution without modifying dependent classes. Constructor injection in service implementations facilitates unit testing with mock objects. The three-tier architecture inherently supports horizontal scaling, and the Strategy pattern provides a clean extension point for new payment methods. The uniform `ServiceResult` return type eliminates exception-based flow control and provides consistent error handling across the application.

**Weaknesses:** The Singleton database connection is a single point of failure and concurrency bottleneck. The absence of a transaction management mechanism means that multi-step operations (e.g., creating a reservation and sending email) are not atomically bound — though the asynchronous email design mitigates this by treating email as non-critical. Loyalty points are tracked on the `Guest` entity but not utilised in pricing, representing an incomplete feature. The `Mapper` layer, while beneficial for separation of concerns, adds a layer of indirection that may be unnecessary for a system of this scale.

#### Documented Assumptions

The following assumptions were made during the design phase:

**Business Logic Assumptions:**
1. All system interactions are mediated by staff members; guests do not have direct system access.
2. Each reservation is associated with exactly one guest and one room.
3. Each reservation can have at most one payment record (no partial payments or split billing).
4. Room pricing is based solely on a flat nightly rate; seasonal pricing or discounts are not modelled.
5. The reservation lifecycle follows: PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT, with CANCELLED possible from PENDING or CONFIRMED states.
6. Managers have all Staff privileges plus reporting and room management access; Administrators have all Manager privileges plus staff management and deletion capabilities.
7. Loyalty points are tracked but not utilised in pricing calculations in the current version.

**Technical Assumptions:**
8. The application runs on a single application server with a single MySQL database instance.
9. Database connection management uses a Singleton pattern; connection pooling is not implemented.
10. Passwords are hashed using BCrypt with a work factor of 12, considered secure for the current threat model.
11. CSRF protection is implemented via session-stored tokens for all POST operations.
12. Email notification is asynchronous and fire-and-forget; delivery failures do not affect business transactions.
13. Database triggers handle reservation number generation, total amount calculation, and double-booking prevention.
14. All entity identifiers use UUID (Version 4) strings for global uniqueness.

**Data and Interaction Assumptions:**
15. Room types are constrained to: STANDARD, DELUXE, SUITE, PENTHOUSE.
16. User roles are constrained to: ADMIN, MANAGER, STAFF.
17. Payment methods are constrained to CASH and CARD; online gateways are not supported.
18. All user inputs are validated on the server side; client-side validation is supplementary.
19. The system is accessed via modern web browsers; print functionality relies on the browser's native print dialogue.

### 2.3.3 Alignment with Industry Standards

The system design aligns with established industry standards and best practices across multiple dimensions:

**UML Compliance:** All diagrams adhere to the UML 2.5.1 specification (OMG, 2017), employing correct notation for actors, use cases, stereotypes (`<<include>>`, `<<extend>>`), classes, interfaces, access modifiers, relationship types (association, aggregation, composition, dependency, realisation), multiplicity, interaction fragments (`alt`, `opt`, `loop`), and activation bars. The diagrams were authored in PlantUML, a widely adopted textual UML tool that ensures notation consistency and version-controllable diagram sources.

**Design Pattern Catalogue:** The six design patterns employed — Singleton, DAO, Strategy, Façade, Row Data Gateway, and Front Controller — are catalogued in authoritative pattern literature. The Singleton, Strategy, and Façade patterns originate from Gamma *et al.* (1994), the foundational "Gang of Four" catalogue. The DAO pattern is recommended by Oracle's Java EE design pattern catalogue for separating business logic from persistence concerns (Oracle, 2023). The Front Controller pattern is a standard Java EE web application pattern. The Row Data Gateway pattern is documented by Fowler (2004) as an appropriate approach for mapping database rows to domain objects.

**SOLID Principles:** The architecture demonstrates adherence to the SOLID principles articulated by Martin (2018). The **Single Responsibility Principle** is evident in the separation of controllers (request handling), services (business logic), DAOs (persistence), and mappers (data translation). The **Open/Closed Principle** is demonstrated by the Strategy pattern — new payment methods can be added without modifying existing code. The **Liskov Substitution Principle** is supported by the interface-based design where any `PaymentStrategy` implementation can substitute for another. The **Interface Segregation Principle** is reflected in the separate, focused DAO and service interfaces (e.g., `GuestDAO` is not forced to implement payment-related methods). The **Dependency Inversion Principle** is manifest in controllers depending on service interfaces rather than implementations, and services depending on DAO interfaces rather than implementations.

**Three-Tier Architecture:** The system's package structure maps directly to the three-tier architectural model: the controller and view packages form the presentation tier, the service package forms the business logic tier, and the DAO, mapper, and utility packages form the data access tier. This separation is an industry-standard approach for enterprise web applications, enabling independent development, testing, and scaling of each tier (Richardson, 2019).

**Scalability Pathway:** While the current implementation is designed for a single-server deployment appropriate to its educational scope, the architecture provides clear pathways for production scaling. The interface-based DAO layer would allow migration to a NoSQL database, a REST-based microservice data source, or a connection-pooled JDBC configuration. The Strategy pattern accommodates additional payment gateways (e.g., PayPal, Stripe). Session management could be migrated from server-side `HttpSession` to stateless JWT tokens for horizontal load balancing. The reporting module could be extended with additional report types without modifying existing controllers.

---

## References

Bloch, J. (2018) *Effective Java*. 3rd edn. Boston: Addison-Wesley.

Fowler, M. (2004) *UML Distilled: A Brief Guide to the Standard Object Modeling Language*. 3rd edn. Boston: Addison-Wesley.

Gamma, E., Helm, R., Johnson, R. and Vlissides, J. (1994) *Design Patterns: Elements of Reusable Object-Oriented Software*. Boston: Addison-Wesley.

Martin, R.C. (2018) *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Boston: Prentice Hall.

OMG (2017) *OMG Unified Modeling Language (OMG UML) Version 2.5.1*. Available at: https://www.omg.org/spec/UML/2.5.1/ (Accessed: 5 March 2026).

Oracle (2023) *Java EE Design Patterns and Best Practices*. Available at: https://docs.oracle.com/en/java/ (Accessed: 5 March 2026).

Richardson, C. (2019) *Microservices Patterns: With Examples in Java*. Shelter Island: Manning Publications.

