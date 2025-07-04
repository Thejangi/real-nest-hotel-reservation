# Real Nest Hotel Reservation System

This is a full-stack hotel reservation management system developed as part of the ASE coursework project. The system supports role-based access and covers customer bookings, check-in/check-out, room management, billing, reporting, and travel agency features.

---

## Features

### Roles
- **Admin**: Manage users, view system dashboards.
- **Clerk**: Handle reservations, check-in, check-out, and customer charges.
- **Manager**: View reports, bookings, and revenue projections.
- **Travel Agency**: Make block bookings (3+ rooms) with mandatory pre-payment.
- **Customer**: Book rooms, view confirmations, and access portal.

### Modules
- Customer & travel agency bookings
- Room filtering and availability
- Check-in and Check-out workflows
- Payment processing and invoice generation
- Admin & Manager dashboards
- Reports with occupancy and revenue data
- JWT Authentication with OTP login
- Residential suite monthly/weekly reservations
- Scheduled no-show billing logic (7 PM)

---

## Tech Stack

### Frontend
- HTML5, CSS3, Bootstrap 5
- JavaScript, jQuery, SweetAlert
- AJAX calls to REST API

### Backend
- Java 21, Spring Boot 3.x, Maven
- Spring Security, JWT, Lombok, ModelMapper
- MySQL 8, JDBC

