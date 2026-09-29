-- create the database
CREATE DATABASE sales_db; 
USE sales_db; -- active database

CREATE TABLE users ( -- table 1: system users
    user_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    username VARCHAR(30) NOT NULL UNIQUE, -- login name (no duplicates)
    password_hash CHAR(64) NOT NULL, -- SHA-256 hash, never the plain password
    role ENUM('ADMIN','CASHIER') NOT NULL DEFAULT 'CASHIER' -- role access control
); 

CREATE TABLE categories ( -- table 2: product groups
    category_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    category_name VARCHAR(50) NOT NULL UNIQUE -- category name (no duplicates)
); 

CREATE TABLE products ( -- table 3: items that are sold
    product_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    product_code VARCHAR(20) NOT NULL UNIQUE, -- business code
    product_name VARCHAR(80) NOT NULL, -- name
    category_id INT NOT NULL, -- link to categories
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0), -- selling price must be positive cannot be negative values
    cost_price DECIMAL(10,2) NOT NULL CHECK (cost_price > 0), -- purchase cost, used for profit
    stock_qty INT NOT NULL DEFAULT 0 CHECK (stock_qty >= 0), -- stock can never be negative
    reorder_level INT NOT NULL DEFAULT 10 CHECK (reorder_level >= 0), -- restock alert
    FOREIGN KEY (category_id) REFERENCES categories(category_id) -- category must exist
); 

CREATE TABLE customers ( -- table 4: buyers
    customer_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    customer_name VARCHAR(80) NOT NULL, -- business name
    phone VARCHAR(15) NOT NULL UNIQUE, -- contact number (no duplicates)
    email VARCHAR(80) NULL, -- email
    address VARCHAR(150) NULL, -- address
    customer_type ENUM('RETAIL','WHOLESALE') NOT NULL DEFAULT 'RETAIL' -- drives the discount strategy
); 

CREATE TABLE sales ( -- table 5: invoice 
    sale_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    invoice_no VARCHAR(30) NOT NULL UNIQUE, -- printed invoice number
    sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- when the sale happened
    customer_id INT NOT NULL, -- buyer
    user_id INT NOT NULL, --  cashier 
    sub_total DECIMAL(12,2) NOT NULL DEFAULT 0, -- total before discount
    discount DECIMAL(12,2) NOT NULL DEFAULT 0, -- discount amount
    grand_total DECIMAL(12,2) NOT NULL DEFAULT 0, -- amount payable
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id), -- customer must exist
    FOREIGN KEY (user_id) REFERENCES users(user_id), -- cashier must exist
    INDEX idx_sale_date (sale_date) -- speeds up date range reports
); 

CREATE TABLE sale_items ( -- table 6: invoice lines
    item_id INT AUTO_INCREMENT PRIMARY KEY, -- id
    sale_id INT NOT NULL, -- invoice
    product_id INT NOT NULL, -- product sold
    quantity INT NOT NULL CHECK (quantity > 0), -- quantity
    unit_price DECIMAL(10,2) NOT NULL, -- price at time of sale
    line_total DECIMAL(12,2) NOT NULL, -- quantity x unit price
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE, -- lines die with the invoice
    FOREIGN KEY (product_id) REFERENCES products(product_id) -- product must exist
); 

-- ---------------- sample data adding ---------------- 
INSERT INTO users (username, password_hash, role) VALUES -- two demo users
 ('admin', SHA2('admin123',256), 'ADMIN'), -- admin / admin123
 ('cashier1', SHA2('cashier123',256), 'CASHIER'); -- cashier1 / cashier123

INSERT INTO categories (category_name) VALUES ('Electronics'),('Groceries'),('Stationery'),('Household'); -- 4 categories

INSERT INTO products (product_code, product_name, category_id, unit_price, cost_price, stock_qty, reorder_level) VALUES -- 8 products
 ('P001','Wireless Mouse',1,2500,1700,40,10), 
 ('P002','USB Keyboard',1,3200,2300,8,10),
 ('P003','Rice 5kg',2,1450,1200,120,30),
 ('P004','Sugar 1kg',2,260,210,15,25),
 ('P005','A4 Paper Ream',3,1250,950,60,20),
 ('P006','Ball Pen Box',3,480,320,200,50),
 ('P007','Detergent 1kg',4,890,640,35,15),
 ('P008','LED Bulb 9W',4,550,380,5,20);

INSERT INTO customers (customer_name, phone, email, address, customer_type) VALUES -- 5 customers
 ('Nimal Perera','0771234567','nimal@mail.com','Negombo','RETAIL'),
 ('Sunrise Traders','0112345678','sunrise@mail.com','Colombo','WHOLESALE'),
 ('Kamal Silva','0779876543',NULL,'Gampaha','RETAIL'),
 ('City Mart','0113456789','citymart@mail.com','Kandy','WHOLESALE'),
 ('Anoma Fernando','0762223344',NULL,'Negombo','RETAIL');

INSERT INTO sales (sale_id, invoice_no, sale_date, customer_id, user_id) VALUES -- 8 invoices
 (1,'INV-1001',DATE_SUB(NOW(),INTERVAL 1 DAY),1,2),
 (2,'INV-1002',DATE_SUB(NOW(),INTERVAL 3 DAY),3,2),
 (3,'INV-1003',DATE_SUB(NOW(),INTERVAL 5 DAY),2,1),
 (4,'INV-1004',DATE_SUB(NOW(),INTERVAL 8 DAY),5,2),
 (5,'INV-1005',DATE_SUB(NOW(),INTERVAL 12 DAY),4,1),
 (6,'INV-1006',DATE_SUB(NOW(),INTERVAL 18 DAY),1,2),
 (7,'INV-1007',DATE_SUB(NOW(),INTERVAL 25 DAY),3,2),
 (8,'INV-1008',DATE_SUB(NOW(),INTERVAL 40 DAY),5,2);

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total) VALUES -- lines (prices fixed below)
 (1,1,2,0,0),(1,3,5,0,0),(2,2,3,0,0),(2,5,10,0,0),(3,4,20,0,0),(3,3,10,0,0),(4,7,4,0,0),(4,8,15,0,0), -- invoices 1-4
 (5,1,5,0,0),(5,5,20,0,0),(6,3,8,0,0),(6,4,10,0,0),(7,2,2,0,0),(7,8,10,0,0),(8,7,6,0,0),(8,1,3,0,0); -- invoices 5-8

UPDATE sale_items si JOIN products p ON p.product_id = si.product_id -- copy current prices into the lines
   SET si.unit_price = p.unit_price, si.line_total = si.quantity * p.unit_price; -- and compute line totals
UPDATE sales s SET s.sub_total = (SELECT SUM(si.line_total) FROM sale_items si WHERE si.sale_id = s.sale_id); -- header sub totals
UPDATE sales s JOIN customers c ON c.customer_id = s.customer_id -- wholesale customers get 5 percent
   SET s.discount = ROUND(s.sub_total * 0.05, 2) WHERE c.customer_type = 'WHOLESALE'; -- only wholesale rows
UPDATE sales SET grand_total = sub_total - discount; -- final payable amount