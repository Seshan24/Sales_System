# Sales System

## Enterprise Application Development 

A Java-based **Sales Management System** developed as part of the **Enterprise Application Development**

The system is designed to manage sales-related activities through a user-friendly interface while demonstrating database operations, validation, exception handling, design patterns, dashboards, and reporting.

---

## 🚀 Features

* 🔐 User Login
* 👤 Customer Management
* 📦 Product Management
* 🛒 Sales Transaction Management
* 📊 Dashboard
* 📄 Sales Reports
* ✅ Input Validation
* ⚠️ Exception Handling
* 🗄️ MySQL Database Integration
* 📈 JasperReports Integration
* 🏗️ Design Pattern Implementation

---

## 🛠️ Technologies Used

* **Java**
* **MySQL**
* **JasperReports**
* **Maven**
* **NetBeans / Java IDE**

---

## 📁 Project Structure

```
SalesSystem/
│
├── database/
│   └── sales_db.sql
│
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           ├── db.properties
│           └── reports/
│               └── SalesDecisionReport.jrxml
│
├── setup.bat
├── setup.sh
├── pom.xml
└── README.md
```

---

## 🗄️ Database Setup

### 1. Install MySQL

Install and start **MySQL Server**.

### 2. Create the Database

Use the SQL file provided in:

```
database/sales_db.sql
```

Import the SQL file into MySQL.

### 3. Configure Database Connection

Open:

```
src/main/resources/db.properties
```

Update the database connection details according to your MySQL configuration.

Example:

```
host=localhost
port=3306
database=sales_db
username=root
password=YOUR_PASSWORD
```

> Do not upload real passwords or sensitive credentials to a public GitHub repository.

---

## ▶️ How to Run

### Using NetBeans

1. Clone or download this repository.
2. Open the project in NetBeans.
3. Make sure MySQL Server is running.
4. Import `database/sales_db.sql`.
5. Configure `db.properties`.
6. Allow Maven to download the required dependencies.
7. Run the main Java application.

### Using Setup Script

For Windows:

```
setup.bat
```

For Linux/macOS:

```
setup.sh
```

---

## 📊 Reporting

The application uses **JasperReports** for generating reports.

Main report:

```
src/main/resources/reports/SalesDecisionReport.jrxml
```

The sales report is designed to provide information that can support management decision-making.

---

## 🏗️ Design Patterns

The project demonstrates the use of software design patterns, including:

* **DAO Pattern** – Database access and data operations
* **Factory Pattern** – Object creation
* **Strategy Pattern** – Flexible business logic
* **Singleton-style Database Connection** – Database connection management
* **MVC-style Structure** – Separation of application components

---

## ✅ Validation & Exception Handling

The system includes:

* Input validation
* Database validation
* Custom exceptions
* Error handling
* User-friendly validation messages

These features help prevent invalid data and improve application reliability.

---

## 📋 Coursework Requirements Covered

The project covers the main coursework deliverables:

* User Interfaces
* Transaction UI
* Input UIs
* Design Patterns
* Dashboard
* Major Report
* Exception Handling
* Validations
* Database
* Setup File

---

## 💻 System Requirements

Before running the application, make sure you have:

* Java JDK
* MySQL Server
* Maven
* NetBeans or another Java IDE

---

## 📌 Important Notes

* Start MySQL before running the application.
* Configure the database connection before starting the system.
* Import the database SQL file before using the application.
* Keep database credentials private when using a public GitHub repository.

---

## 👨‍💻 Project

**Application Area:** Sales Management
**Project Type:** Enterprise Application

--- 

## 👨‍💻 Developer

**Seshan Rodrigo**

- LinkedIn: https://www.linkedin.com/in/your-profile
- GitHub: https://github.com/yourusername
---

⭐ If you find this project useful, consider giving the repository a star!

