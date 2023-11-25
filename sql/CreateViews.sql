USE Pizzeria;

CREATE VIEW ToppingPopularity AS
SELECT ToppingName AS Topping, 
       COUNT(ToppingID) + SUM(CASE WHEN PizzaToppingExtraTopping = 1 THEN 1 ELSE 0 END) AS ToppingCount
FROM pizzatopping 
LEFT JOIN topping ON pizzatopping.PizzaToppingToppingID = topping.ToppingID
GROUP BY ToppingName
ORDER BY ToppingCount DESC;

CREATE VIEW ProfitByOrderType AS
SELECT 
    CustomerOrderOrderType AS CustomerType, 
    DATE_FORMAT(CustomerOrderTimestamp, '%Y-%M') AS OrderMonth, 
    SUM(CustomerOrderCustomerPrice) AS TotalOrderPrice, 
    SUM(CustomerOrderCompanyCost) AS TotalOrderCost, 
    SUM(CustomerOrderCustomerPrice - CustomerOrderCompanyCost) AS Profit 
FROM customerorder
GROUP BY 
    CustomerOrderOrderType, 
    DATE_FORMAT(CustomerOrderTimestamp, '%Y-%M')

UNION ALL

SELECT 
    ' ' AS CustomerType, 
    'Grand Total' AS OrderMonth, 
    SUM(CustomerOrderCustomerPrice) AS TotalOrderPrice, 
    SUM(CustomerOrderCompanyCost) AS TotalOrderCost, 
    SUM(CustomerOrderCustomerPrice - CustomerOrderCompanyCost) AS Profit 
FROM customerorder;

CREATE VIEW ProfitByPizza AS
SELECT PizzaCrustName AS 'Pizza Crust', 
       PizzaSizeName AS 'Pizza Size', 
       SUM(PizzaCustomerPrice - PizzaCompanyCost) AS Profit, 
       MAX(CustomerOrderTimestamp) AS LastOrderDate
FROM pizza
JOIN pizzacustomerorder ON PizzaCustomerOrderPizzaID = PizzaID
JOIN customerorder ON PizzaCustomerOrderCustomerOrderID = CustomerOrderID
JOIN pizzabase ON PizzaPizzaBaseID = PizzaBaseID
JOIN pizzacrust ON PizzaBaseCrustPizzaCrustID = PizzaCrustID
JOIN pizzasize ON PizzaBasePizzaSizeID = PizzaSizeID
GROUP BY PizzaCrustName, PizzaSizeName 
ORDER BY Profit DESC;

SELECT * FROM ToppingPopularity;
SELECT * from ProfitByOrderType;
SELECT * FROM ProfitByPizza;