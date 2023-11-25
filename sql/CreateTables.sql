CREATE SCHEMA Pizzeria;
USE Pizzeria;

CREATE TABLE pizzasize (
    PizzaSizeID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaSizeName VARCHAR(255) NOT NULL
);

CREATE TABLE pizzacrust (
    PizzaCrustID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaCrustName VARCHAR(255) NOT NULL
);

CREATE TABLE address (
    AddressID INT AUTO_INCREMENT PRIMARY KEY,
    AddressHouseNum VARCHAR(255) NOT NULL,
    AddressStreet VARCHAR(255) NOT NULL,
    AddressCity VARCHAR(255) NOT NULL,
    AddressState VARCHAR(255) NOT NULL,
    AddressZipcode VARCHAR(255) NOT NULL
);

CREATE TABLE customer (
    CustomerID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerFirstName VARCHAR(255),
    CustomerLastName VARCHAR(255),
    CustomerPhone VARCHAR(10),
    CustomerAddressAddressID INT,
    FOREIGN KEY (CustomerAddressAddressID) REFERENCES address(AddressID)
);

CREATE TABLE pizzabase (
    PizzaBaseID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaBaseCustomerPrice DECIMAL(8,2) NOT NULL,
    PizzaBaseCompanyCost DECIMAL(8,2) NOT NULL,
    PizzaBaseCrustPizzaCrustID INT NOT NULL,
    PizzaBasePizzaSizeID INT NOT NULL,
    FOREIGN KEY (PizzaBaseCrustPizzaCrustID) REFERENCES pizzacrust(PizzaCrustID),
    FOREIGN KEY (PizzaBasePizzaSizeID) REFERENCES pizzasize(PizzaSizeID)
);

CREATE TABLE topping (
    ToppingID INT AUTO_INCREMENT PRIMARY KEY,
    ToppingName VARCHAR(255) NOT NULL,
    ToppingCustomerPrice DECIMAL(8,2) NOT NULL,
    ToppingCompanyCost DECIMAL(8,2) NOT NULL,
    ToppingCurInventory INT DEFAULT 0,
    ToppingMinInventory INT DEFAULT 0
);

CREATE TABLE pizza (
    PizzaID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaPizzaBaseID INT NOT NULL,
    PizzaCustomerPrice DECIMAL(8,2) NOT NULL,
    PizzaCompanyCost DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (PizzaPizzaBaseID) REFERENCES pizzabase(PizzaBaseID)
);

CREATE TABLE toppingamount (
    ToppingAmountToppingID INT,
    ToppingAmountPizzaSizeID INT,
    ToppingAmountUnitQty INT NOT NULL,
    PRIMARY KEY (ToppingAmountToppingID, ToppingAmountPizzaSizeID),
    FOREIGN KEY (ToppingAmountToppingID) REFERENCES topping(ToppingID),
    FOREIGN KEY (ToppingAmountPizzaSizeID) REFERENCES pizzasize(PizzaSizeID)
);

CREATE TABLE pizzatopping (
    PizzaToppingPizzaID INT,
    PizzaToppingToppingID INT,
    PizzaToppingExtraTopping BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (PizzaToppingPizzaID, PizzaToppingToppingID),
    FOREIGN KEY (PizzaToppingPizzaID) REFERENCES pizza(PizzaID),
    FOREIGN KEY (PizzaToppingToppingID) REFERENCES topping(ToppingID)
);

CREATE TABLE discount (
    DiscountID INT AUTO_INCREMENT PRIMARY KEY,
    DiscountName VARCHAR(255) NOT NULL,
    DiscountDollarAmount DECIMAL(8,2),
    DiscountPercentageAmount DECIMAL(5,2)
);

CREATE TABLE customerorder (
    CustomerOrderID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerOrderOrderType VARCHAR(255) NOT NULL,
    CustomerOrderCustomerID INT,
    CustomerOrderCustomerPrice DECIMAL(8,2) NOT NULL,
    CustomerOrderCompanyCost DECIMAL(8,2) NOT NULL,
    CustomerOrderTimestamp TIMESTAMP,
    CustomerOrderComplete boolean,
    FOREIGN KEY (CustomerOrderCustomerID) REFERENCES customer(CustomerID)
);

CREATE TABLE pizzadiscount (
    PizzaDiscountDiscountID INT,
    PizzaDiscountPizzaID INT,
    PRIMARY KEY (PizzaDiscountDiscountID, PizzaDiscountPizzaID),
    FOREIGN KEY (PizzaDiscountDiscountID) REFERENCES discount(DiscountID),
    FOREIGN KEY (PizzaDiscountPizzaID) REFERENCES pizza(PizzaID)
);

CREATE TABLE pizzacustomerorder (
    PizzaCustomerOrderPizzaID INT,
    PizzaCustomerOrderCustomerOrderID INT,
    PizzaCustomerOrderStatus VARCHAR(255) NOT NULL,
    PRIMARY KEY (PizzaCustomerOrderPizzaID, PizzaCustomerOrderCustomerOrderID),
    FOREIGN KEY (PizzaCustomerOrderPizzaID) REFERENCES pizza(PizzaID),
    FOREIGN KEY (PizzaCustomerOrderCustomerOrderID) REFERENCES customerorder(CustomerOrderID)
);

CREATE TABLE orderdiscount (
    OrderDiscountDiscountID INT,
    OrderDiscountOrderID INT,
    PRIMARY KEY (OrderDiscountDiscountID, OrderDiscountOrderID),
    FOREIGN KEY (OrderDiscountDiscountID) REFERENCES discount(DiscountID),
    FOREIGN KEY (OrderDiscountOrderID) REFERENCES customerorder(CustomerOrderID)
);

CREATE TABLE dinein (
    DineInOrderID INT PRIMARY KEY,
    DineInTableID INT NOT NULL,
    FOREIGN KEY (DineInOrderID) REFERENCES customerorder(CustomerOrderID)
);

CREATE TABLE pickup (
    PickupOrderID INT PRIMARY KEY,
    PickupIsPickedUp boolean,
    FOREIGN KEY (PickupOrderID) REFERENCES customerorder(CustomerOrderID)
);

CREATE TABLE delivery (
    DeliveryOrderID INT PRIMARY KEY,
    DeliveryAddress VARCHAR(255) NOT NULL,
    FOREIGN KEY (DeliveryOrderID) REFERENCES customerorder(CustomerOrderID)
);
