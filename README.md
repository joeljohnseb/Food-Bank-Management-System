# 🍱 Community Food Bank Management System

A Java-based desktop application developed using **Java Swing** and **MySQL** for managing food bank operations efficiently.

The system allows users to request food supplies while administrators can manage inventory and approve pending requests through a user-friendly graphical interface.

---

# ✨ Features

## 👤 User Module

* Secure login system
* View available food inventory
* Submit food requests
* Track request status
* Simple and clean GUI

## 🛠️ Admin Module

* Admin login access
* View pending requests
* Approve or reject requests
* Manage food inventory
* Monitor user activities

---

# 🧰 Tech Stack

* **Language:** Java
* **GUI Framework:** Java Swing
* **Database:** MySQL
* **IDE:** Eclipse
* **Connectivity:** JDBC

---

# 📂 Project Structure

```bash
CommunityFoodBank/
│
├── src/
│   └── foodpack/
│       ├── LoginFrame.java
│       ├── AdminFrame.java
│       ├── UserFrame.java
│       └── DBConnection.java
│
├── bin/
├── .classpath
├── .project
└── README.md
```

---

# ⚙️ Requirements

Before running the project, make sure you have:

* Java JDK 8 or higher
* Eclipse IDE
* MySQL Server
* MySQL JDBC Connector

---

# 🗄️ Database Setup

## 1. Create Database

```sql
CREATE DATABASE foodbank;
```

## 2. Update Database Credentials

Open:

```java
DBConnection.java
```

Update:

```java
private static final String URL     = "jdbc:mysql://localhost:3306/foodbank";
private static final String DB_USER = "root";
private static final String DB_PASS = "your_password";
```

---

# ▶️ How to Run

## Using Eclipse

1. Import the project into Eclipse
2. Add MySQL JDBC Driver to Build Path
3. Configure MySQL database
4. Run:

```bash
LoginFrame.java
```

---

# 🔐 Login System

The application supports:

* **Admin Login**
* **User Login**

Role-based access controls are implemented using Swing interfaces and database authentication.

---

# 🖥️ Screenshots

Add screenshots here for better GitHub presentation.

Example:

```md
![Login Page](screenshots/login.png)
![Admin Dashboard](screenshots/admin.png)
![User Dashboard](screenshots/user.png)
```

---

# 📌 Key Concepts Used

* Object-Oriented Programming (OOP)
* Java Swing GUI Development
* JDBC Database Connectivity
* Event Handling
* Role-Based Access Control
* CRUD Operations
* SQL Queries

---

# 🚀 Future Improvements

* Password encryption
* Email notifications
* Food donation module
* Analytics dashboard
* PDF report generation
* Cloud database integration
* Better UI/UX design

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Push to your branch
5. Create a Pull Request

