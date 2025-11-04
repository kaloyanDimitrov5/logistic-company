# Logistics Company Management System

A web application for managing the core operations of a logistics company.  
It supports handling **clients, employees, offices, and shipments** with a clean admin interface.

---

## ✨ Features

### Users
- Register and manage basic user information
- Passwords are securely stored using `PasswordEncoder`

### Client Profiles
- Create and manage clients
- Store contact and delivery information

### Employee Profiles
- Create employees and assign them to a specific office
- Store employee position (e.g., Courier, Office Worker)

### Offices
- Manage office locations (city + address)
- Each employee belongs to one office

### Shipments
- Create and track shipments
- Sender and receiver must be registered clients
- Delivery pricing depends on weight and delivery type (to address / to office)
- Courier (employee) can be assigned to handle the shipment

---

## 🏗️ Tech Stack

| Part | Technology |
|------|------------|
| Backend | Java 21, Spring Boot 3 |
| Database | H2 (in-memory by default) |
| Persistence | Spring Data JPA (Hibernate) |
| UI | Thymeleaf + Bootstrap 5 |
| Build Tool | Maven |

---

## 🗄️ Simplified Database Structure
- User (id, email, password, full_name, enabled)
- ClientProfile (id, user_id, phone, address)
- EmployeeProfile (id, user_id, office_id, position)
- Office (id, city, address_line)
- Shipment (id, sender_id, recipient_id, weight, price, delivery_type, courier_id, office_id)
