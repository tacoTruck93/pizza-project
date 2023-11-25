package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";














	
	private static boolean connect_to_db() throws SQLException, IOException {

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}

	
	public static void addOrder(Order o) throws SQLException, IOException 
	{
		connect_to_db();
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, and pickup tables
		 * 
		 */

		// add data into order table
		PreparedStatement os;
		String query;
		query = "INSERT INTO customerorder (CustomerOrderOrderType, CustomerOrderCustomerID, CustomerOrderCustomerPrice, " +
				"CustomerOrderCompanyCost, CustomerOrderTimestamp, CustomerOrderComplete) VALUES " +
				"(?, ?, ?, ?, ?, ?)";

		os = conn.prepareStatement(query);
		os.setString(1, o.getOrderType());
		os.setInt(2, o.getCustID());
		os.setDouble(3, o.getCustPrice());
		os.setDouble(4, o.getBusPrice());
		os.setString(5, o.getDate());
		os.setDouble(6, o.getIsComplete());

		os.executeUpdate();

		for (Discount discount: o.getDiscountList()) {
			useOrderDiscount(o, discount);
		}

		//add data into appropriate subtype
		String orderType = o.getOrderType();
		String query2;
		switch (orderType) {
			case "dinein" -> {
				query2 = "INSERT INTO dinein (DineInOrderID, DineInTableID) VALUES (?, ?);";
				os = conn.prepareStatement(query2);
				os.setInt(1, o.getOrderID());
				os.setInt(2, 12);
				os.executeUpdate();
			}
			case "delivery" -> {
				query2 = "INSERT INTO delivery (DeliveryOrderID, DeliveryAddress) VALUES (?, ?);";
				os = conn.prepareStatement(query2);
				os.setInt(1, o.getOrderID());
				os.setString(2, "temp address for testing");
				os.executeUpdate();
			}
			case "pickup" -> {
				query2 = "INSERT INTO pickup (PickupOrderID) VALUES (?);";
				os = conn.prepareStatement(query2);
				os.setInt(1, o.getOrderID());
				os.executeUpdate();
			}
		}

		//create pizza array
		ArrayList<Pizza> pizzas = o.getPizzaList();


		//add data to pizzacustomerorder table, need to loop through pizzas

		for (Pizza pizza: pizzas) {
			boolean doubleTopping = false;
			boolean[] isDoubleArray = pizza.getIsDoubleArray();

			addPizza(pizza);
			int lastOrder = getLastPizzaID();
			String query3;
			query3 = "INSERT INTO pizzacustomerorder (PizzaCustomerOrderPizzaID, PizzaCustomerOrderCustomerOrderID, PizzaCustomerOrderStatus) VALUES " +
					"(?, ?, ?);";
			os = conn.prepareStatement(query3);
			os.setInt(1, lastOrder);
			os.setInt(2, o.getOrderID());
			os.setString(3, "open");

			for (Discount discount: pizza.getDiscounts()) {
				usePizzaDiscount(pizza, discount);
			}

			os.executeUpdate();
			ArrayList<Topping> toppings = pizza.getToppings();
			for (Topping topping: toppings) {
				/*for (int i = 0; i < isDoubleArray.length; i++) {
					System.out.println("Topping " + topping.getTopName() + " " + i + " is: " + isDoubleArray[i]);

					if (i == topping.getTopID() && isDoubleArray[i]) {
						System.out.println("Entered true zone on index: " + i);
						doubleTopping = true;
					}
				}*/

				if (isDoubleArray[topping.getTopID()-1]) {
					doubleTopping = true;
				}
				//System.out.println("It is: " + isDoubleArray[topping.getTopID()] + ". Therefore doubleTopping = " + doubleTopping);
				useTopping(pizza, topping, doubleTopping);
			}

		}






		//make sure to call add order on menu, AKA this


		conn.close();
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void addPizza(Pizza p) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind adding pizza discounts and toppings associated with the pizza,
		 * there are other methods below that may help with that process.
		 * 
		 */

		//insert into pizza, add toppings and discounts to pizza. may need to update pizzatopping table

		//Need to get the cost for customer and company, topping price + baseprice - pizza discount
		double companyCost = p.getBusPrice();
		double customerPrice = p.getCustPrice();

		PreparedStatement os;
		String query;
		query = "INSERT INTO pizza (PizzaPizzaBaseID, PizzaCustomerPrice, PizzaCompanyCost) " +
				"VALUES ((SELECT PizzaBaseID FROM pizzabase " +
				"LEFT JOIN pizzacrust ON PizzaCrustID = PizzaBaseCrustPizzaCrustID " +
				"LEFT JOIN pizzasize ON PizzaSizeID = PizzaBasePizzaSizeID " +
				"WHERE PizzaSizeName =  ? AND PizzaCrustName = ?), ?, ?);";

		os = conn.prepareStatement(query);
		os.setString(1, p.getSize());
		os.setString(2, p.getCrustType());
		os.setDouble(3, p.getCustPrice());
		os.setDouble(4, p.getBusPrice());

		os.executeUpdate();
		//conn.close();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	
	public static void useTopping(Pizza p, Topping t, boolean isDoubled) throws SQLException, IOException //this method will update toppings inventory in SQL and add entities to the Pizzatops table. Pass in the p pizza that is using t topping
	{
		connect_to_db();
		/*
		 * This method should do 2 two things.
		 * - update the topping inventory every time we use t topping (accounting for extra toppings as well)
		 * - connect the topping to the pizza
		 *   What that means will be specific to your implementatinon.
		 * 
		 * Ideally, you should't let toppings go negative....but this should be dealt with BEFORE calling this method.
		 * 
		 */

		// get the pizza size, look at topping quantity and retrieve qty for that pizza size.
		String pizzaSize = p.getSize();
		int toppingINVT = t.getCurINVT();
		double amount = 0;

		switch (pizzaSize){
			case "Small":
				amount = t.getPerAMT();
				break;
			case "Medium":
				amount = t.getMedAMT();
				break;
			case "Large":
				amount = t.getLgAMT();
				break;
			case "XLarge":
				amount = t.getXLAMT();
				break;
		}
		if (isDoubled) {
			amount *= 2;
		}

		//subtract amount used from existing inventory
		toppingINVT -= amount;

		// inserting
		PreparedStatement os;
		String query;
		String query2;
		query = "INSERT INTO pizzatopping (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingExtraTopping) VALUES (?, ?, ?);";
		os = conn.prepareStatement(query);
		os.setInt(1, getLastPizzaID());
		os.setInt(2, t.getTopID());
		os.setBoolean(3, isDoubled);
		os.executeUpdate();



		query2 = "UPDATE topping SET ToppingCurInventory = ? WHERE toppingID = ?;";
		os = conn.prepareStatement(query2);
		os.setInt(1, toppingINVT);
		os.setInt(2, t.getTopID());
		os.executeUpdate();
		//conn.close();
		
		

		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	
	public static void usePizzaDiscount(Pizza p, Discount d) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * This method connects a discount with a Pizza in the database.
		 * 
		 * What that means will be specific to your implementatinon.
		 */
		PreparedStatement os;
		String query;
		query = "INSERT INTO pizzadiscount (PizzaDiscountDiscountID, PizzaDiscountPizzaID) VALUES (?, ?);";
		os = conn.prepareStatement(query);
		os.setInt(1, d.getDiscountID());
		os.setInt(2, p.getPizzaID());

		// Execute the update
		os.executeUpdate();
		conn.close();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * This method connects a discount with an order in the database
		 * 
		 * You might use this, you might not depending on where / how to want to update
		 * this information in the dabast
		 */

		PreparedStatement os;
		String query;
		query = "INSERT INTO orderdiscount (OrderDiscountDiscountID, OrderDiscountOrderID) VALUES (?, ?);";
		os = conn.prepareStatement(query);
		os.setInt(1, d.getDiscountID());
		os.setInt(2, o.getOrderID());

		// Execute the update
		os.executeUpdate();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method adds a new customer to the database.
		 * 
		 */

		PreparedStatement os;
		String query;
		query = "INSERT INTO customer (CustomerFirstName, CustomerLastName, CustomerPhone) VALUES (?, ?, ?);";
		os = conn.prepareStatement(query);
		os.setString(1, c.getFName());
		os.setString(2, c.getLName());
		os.setString(3, c.getPhone());

		// Execute the update
		os.executeUpdate();
		conn.close();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void completeOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Find the specifed order in the database and mark that order as complete in the database.
		 * 
		 */
		int OrderID = o.getOrderID();

		PreparedStatement os;
		String query;
		query = "UPDATE customerorder SET CustomerOrderComplete = 1 WHERE CustomerOrderID = ?;";
		os = conn.prepareStatement(query);
		os.setInt(1, OrderID);

		os.executeUpdate();
		conn.close();









		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}


	public static ArrayList<Order> getOrders(boolean openOnly) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Return an arraylist of all of the orders.
		 * 	openOnly == true => only return a list of open (ie orders that have not been marked as completed)
		 *           == false => return a list of all the orders in the database
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */
		ArrayList<Order> orders = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;

		if (openOnly) {


			query = "SELECT * " +
					"FROM customerorder " +
					"LEFT JOIN delivery ON CustomerOrderID = DeliveryOrderID " +
					"LEFT JOIN dinein ON CustomerOrderID = DineInOrderID " +
					"LEFT JOIN pickup ON CustomerOrderID = PickupOrderID " +
					"WHERE CustomerOrderComplete = 0;";
		}
		else {
			query = "SELECT * " +
					"FROM customerorder " +
					"LEFT JOIN delivery ON CustomerOrderID = DeliveryOrderID " +
					"LEFT JOIN dinein ON CustomerOrderID = DineInOrderID " +
					"LEFT JOIN pickup ON CustomerOrderID = PickupOrderID;";
		}
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
		while (rset.next()) {
			int orderID = rset.getInt("CustomerOrderID");
			int custID = rset.getInt("CustomerOrderCustomerID");
			String orderType = rset.getString("CustomerOrderOrderType");
			String date = rset.getString("CustomerOrderTimestamp");
			double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
			double busPrice = rset.getDouble("CustomerOrderCompanyCost");
			int iscomplete = rset.getInt("CustomerOrderComplete");
			int tableNum = rset.getInt("DineInTableID");
			String address = rset.getString("DeliveryAddress");
			int isPickedUp = rset.getInt("PickupIsPickedUp");

			switch (orderType) {
				case "dinein":
					orders.add(new DineinOrder(orderID, custID, date,custPrice,busPrice,iscomplete, tableNum));
					break;
				case "pickup":
					//need to change is pickedup to real value
					orders.add(new PickupOrder(orderID,custID,date,custPrice,busPrice,isPickedUp,iscomplete));
					break;
				case "delivery":
					orders.add(new DeliveryOrder(orderID,custID,date,custPrice,busPrice,iscomplete,address));
					break;
			}
		}

		ArrayList<Pizza> pizzas = new ArrayList<>();
		String query3;

		for (Order order : orders) {
			int sqlOrderID = order.getOrderID();
			query3 = "SELECT * FROM pizza LEFT JOIN pizzacustomerorder ON PizzaID = PizzaCustomerOrderPizzaID " +
					"LEFT JOIN customerorder ON CustomerOrderID = PizzaCustomerOrderCustomerOrderID " +
					"LEFT JOIN pizzabase ON PizzaBaseID = PizzaPizzaBaseID " +
					"LEFT JOIN pizzacrust ON PizzaCrustID = PizzaBaseCrustPizzaCrustID " +
					"LEFT JOIN pizzasize ON PizzaSizeID = PizzaBasePizzaSizeID " +
					"WHERE CustomerOrderID = ?;";
			os = conn.prepareStatement(query3);
			os.setInt(1, sqlOrderID);
			rset = os.executeQuery();

			while (rset.next()) {
				int pizzaID = rset.getInt("PizzaID");
				String size = rset.getString("PizzaSizeName");
				String crustType = rset.getString("PizzaCrustName");
				int orderID = rset.getInt("CustomerOrderID");
				String pizzaState = rset.getString("CustomerOrderComplete");
				String pizzaDate = rset.getString("CustomerOrderTimestamp");
				double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
				double busPrice = rset.getDouble("CustomerOrderCompanyCost");
				Pizza pizza = new Pizza(pizzaID, size, crustType, orderID, pizzaState, pizzaDate, custPrice, busPrice);
				order.addPizza(pizza);

			}
		}


		conn.close();
		return orders;
	}

	public static Order getLastOrder() {
		try {
			connect_to_db();

			/*
			 * Query the database for the LAST order added
			 * then return an Order object for that order.
			 * NOTE...there should ALWAYS be a "last order"!
			 */


			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT * FROM customerorder ORDER BY CustomerOrderID DESC LIMIT 1;";
			os = conn.prepareStatement(query);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int orderID = rset.getInt("CustomerOrderID");
				int custID = rset.getInt("CustomerOrderCustomerID");
				String orderType = rset.getString("CustomerOrderOrderType");
				String date = rset.getString("CustomerOrderTimestamp");
				double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
				double busPrice = rset.getDouble("CustomerOrderCompanyCost");
				int isComplete = rset.getInt("CustomerOrderComplete");
				Order order = new Order(orderID, custID, orderType, date, custPrice, busPrice, isComplete);
				conn.close();
				return order;
			}


			//System.err.println("No orders in the System!");
			conn.close();
			return null;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public static int getLastPizzaID() {
		try {
			connect_to_db();

			/*
			 * Query the database for the LAST order added
			 * then return an Order object for that order.
			 * NOTE...there should ALWAYS be a "last order"!
			 */


			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT PizzaID FROM pizza ORDER BY PizzaID DESC LIMIT 1;";
			os = conn.prepareStatement(query);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int PizzaID = rset.getInt("PizzaID");
				//conn.close();
				return PizzaID;
			}


			//System.err.println("No orders in the System!");
			//conn.close();
			return 0;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}

	}

	public static Order getOrderByID(int ID) {
		try {
			connect_to_db();

			/*
			 * Query the database for the LAST order added
			 * then return an Order object for that order.
			 * NOTE...there should ALWAYS be a "last order"!
			 */


			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT * FROM customerorder WHERE CustomerOrderID = ?;";
			os = conn.prepareStatement(query);
			os.setInt(1, ID);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int orderID = rset.getInt("CustomerOrderID");
				int custID = rset.getInt("CustomerOrderCustomerID");
				String orderType = rset.getString("CustomerOrderOrderType");
				String date = rset.getString("CustomerOrderTimestamp");
				double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
				double busPrice = rset.getDouble("CustomerOrderCompanyCost");
				int isComplete = rset.getInt("CustomerOrderComplete");
				Order order = new Order(orderID, custID, orderType, date, custPrice, busPrice, isComplete);
				conn.close();
				return order;
			}


			//System.err.println("No orders in the System!");
			conn.close();
			return null;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<Order> getOrdersByDate(String date) {
		ArrayList<Order> orders = new ArrayList<>();
		try {
			connect_to_db();


			String query = "SELECT * FROM customerorder WHERE DATE(CustomerOrderTimestamp) = ?;";
			PreparedStatement os = conn.prepareStatement(query);
			os.setString(1, date); // set the date parameter
			ResultSet rset = os.executeQuery();

			while (rset.next()) {
				int orderID = rset.getInt("CustomerOrderID");
				int custID = rset.getInt("CustomerOrderCustomerID");
				String orderType = rset.getString("CustomerOrderOrderType");
				String orderDate = rset.getString("CustomerOrderTimestamp");
				double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
				double busPrice = rset.getDouble("CustomerOrderCompanyCost");
				int isComplete = rset.getInt("CustomerOrderComplete");

				orders.add(new Order(orderID, custID, orderType, orderDate, custPrice, busPrice, isComplete));
			}

			conn.close();

		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return orders;
	}

	public static ArrayList<Order> getOrdersAfterDate(String date) {
		ArrayList<Order> orders = new ArrayList<>();
		try {
			connect_to_db();


			String query = "SELECT * FROM customerorder WHERE DATE(CustomerOrderTimestamp) >= DATE(?);";
			PreparedStatement os = conn.prepareStatement(query);
			os.setString(1, date);
			ResultSet rset = os.executeQuery();

			while (rset.next()) {
				int orderID = rset.getInt("CustomerOrderID");
				int custID = rset.getInt("CustomerOrderCustomerID");
				String orderType = rset.getString("CustomerOrderOrderType");
				String orderDate = rset.getString("CustomerOrderTimestamp");
				double custPrice = rset.getDouble("CustomerOrderCustomerPrice");
				double busPrice = rset.getDouble("CustomerOrderCompanyCost");
				int isComplete = rset.getInt("CustomerOrderComplete");

				orders.add(new Order(orderID, custID, orderType, orderDate, custPrice, busPrice, isComplete));
			}

			conn.close();

		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return orders;
	}

	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database for all the available discounts and 
		 * return them in an arrayList of discounts.
		 * 
		*/
		ArrayList<Discount> discounts = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT " +
				"DiscountID, " +
				"DiscountName, " +
				"COALESCE(DiscountDollarAmount, DiscountPercentageAmount) AS amount, " +
				"CASE " +
				"WHEN DiscountDollarAmount IS NOT NULL THEN 0 " +
				"WHEN DiscountPercentageAmount IS NOT NULL THEN 1 " +
				"ELSE NULL " +
				"END AS isPercent " +
				"FROM discount;";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		while (rset.next()) {
			int discountID = rset.getInt("DiscountID");
			String discountName = rset.getString("DiscountName");
			double amount = rset.getDouble("amount");
			boolean isPercent = rset.getBoolean("isPercent");
			discounts.add(new Discount(discountID, discountName, amount, isPercent));
		}

		conn.close();
		return discounts;
		
		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static Discount findDiscountByName(String name) {
		/*
		 * Query the database for a discount using it's name.
		 * If found, then return an OrderDiscount object for the discount.
		 * If it's not found....then return null
		 *
		 */
		Discount discount = null;
		try {
			connect_to_db();

			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT " +
					"  DiscountID," +
					"  DiscountName," +
					"  COALESCE(DiscountDollarAmount, DiscountPercentageAmount) AS amount," +
					"  CASE " +
					"    WHEN DiscountDollarAmount IS NOT NULL THEN 0 " +
					"    WHEN DiscountPercentageAmount IS NOT NULL THEN 1 " +
					"    ELSE NULL " +
					"  END AS isPercent " +
					"FROM discount WHERE DiscountName = ?;";
			os = conn.prepareStatement(query);
			os.setString(1, name);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int discountID = rset.getInt("DiscountID");
				String discountName = rset.getString("DiscountName");
				double amount = rset.getDouble("amount");
				boolean isPercent = rset.getBoolean("isPercent");
				discount = new Discount(discountID, discountName, amount, isPercent);
			}

			conn.close();

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}


		return discount;

	}


	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the data for all the customers and return an arrayList of all the customers. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		*/
		ArrayList<Customer> customers = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "Select CustomerID, CustomerFirstName, CustomerLastName, CustomerPhone " +
				"From customer " +
				"ORDER BY CustomerID;";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		while (rset.next()) {
			int custID = rset.getInt("CustomerID");
			String fName = rset.getString("CustomerFirstName");
			String lName = rset.getString("CustomerLastName");
			String phone = rset.getString("CustomerPhone");
			customers.add(new Customer(custID, fName, lName, phone)); // Using existing Customer class
		}

		conn.close();
		return customers;

		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static Customer findCustomerByPhone(String phoneNumber){
		/*
		 * Query the database for a customer using a phone number.
		 * If found, then return a Customer object for the customer.
		 * If it's not found....then return null
		 *  
		 */
		try {
			connect_to_db();

			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT * FROM customer WHERE CustomerPhone = ?;";
			os = conn.prepareStatement(query);
			os.setString(1, phoneNumber);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int custID = rset.getInt("CustomerID");
				String fName = rset.getString("CustomerFirstName");
				String lName = rset.getString("CustomerLastName");
				String phone = rset.getString("CustomerPhone");
				Customer customer = new Customer(custID, fName, lName, phone);
				conn.close();
				return customer;
			}

			// Added this section to handle the case where there's no result
			conn.close();
			return null;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public static Customer getLastCustomer() {
		try {
			connect_to_db();

			/*
			 * Query the database for the LAST customre added
			 * then return an Order object for that order.
			 * NOTE...there should ALWAYS be a "last order"!
			 */


			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT * FROM customer ORDER BY CustomerID DESC LIMIT 1;";
			os = conn.prepareStatement(query);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int custID = rset.getInt("CustomerID");
				String fName = rset.getString("CustomerFirstName");
				String lname = rset.getString("CustomerLastName");
				String phone = rset.getString("CustomerPhone");
				Customer customer = new Customer(custID, fName, lname, phone);
				conn.close();
				return customer;
			}


			//System.err.println("No orders in the System!");
			conn.close();
			return null;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}
	public static ArrayList<Topping> getToppingList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for the aviable toppings and 
		 * return an arrayList of all the available toppings. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */

		ArrayList<Topping> toppings = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT \n" +
				"    t.ToppingID AS ToppingID,\n" +
				"    t.ToppingName AS ToppingName,\n" +
				"    t.ToppingCustomerPrice AS ToppingCustomerPrice,\n" +
				"    t.ToppingCompanyCost AS ToppingCompanyCost,\n" +
				"    t.ToppingMinInventory AS ToppingMinInventory,\n" +
				"    t.ToppingCurInventory AS ToppingCurInventory,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'small' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS small,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'medium' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS medium,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'large' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS large,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'xlarge' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS xlarge\n" +
				"FROM\n" +
				"    topping AS t\n" +
				"LEFT JOIN\n" +
				"    toppingamount AS ta ON t.ToppingID = ta.ToppingAmountToppingID\n" +
				"LEFT JOIN\n" +
				"    pizzasize AS ps ON ps.PizzaSizeID = ta.ToppingAmountPizzaSizeID\n" +
				"GROUP BY\n" +
				"    t.ToppingID, t.ToppingName, t.ToppingCustomerPrice, t.ToppingCompanyCost, t.ToppingMinInventory, t.ToppingCurInventory\n" +
				"ORDER BY\n" +
				"    t.ToppingID;\n";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		while (rset.next()) {
			int TopID = rset.getInt("ToppingID");
			String TopName = rset.getString("ToppingName");
			double PerAMT = rset.getDouble("small");
			double MedAMT = rset.getDouble("medium");
			double LgAMT = rset.getDouble("large");
			double XLAMT = rset.getDouble("xlarge");
			double CustPrice = rset.getDouble("ToppingCustomerPrice");
			double BusPrice = rset.getDouble("ToppingCompanyCost");
			int MinINVT = rset.getInt("ToppingMinInventory");
			int CurINVT = rset.getInt("ToppingCurInventory");

			toppings.add(new Topping(TopID, TopName, PerAMT, MedAMT, LgAMT, XLAMT, CustPrice, BusPrice, MinINVT, CurINVT));

		}


		conn.close();
		return toppings;

	}

	public static Topping findToppingByName(String name){
		/*
		 * Query the database for the topping using it's name.
		 * If found, then return a Topping object for the topping.
		 * If it's not found....then return null
		 *  
		 */
		try {
			connect_to_db();

			PreparedStatement os;
			ResultSet rset;
			String query;
			query = "SELECT\n" +
					"    t.ToppingID AS ToppingID,\n" +
					"    t.ToppingName AS ToppingName,\n" +
					"    t.ToppingCustomerPrice AS ToppingCustomerPrice,\n" +
					"    t.ToppingCompanyCost AS ToppingCompanyCost,\n" +
					"    t.ToppingMinInventory AS ToppingMinInventory,\n" +
					"    t.ToppingCurInventory AS ToppingCurInventory,\n" +
					"    SUM(CASE WHEN ps.PizzaSizeName = 'small' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS small,\n" +
					"    SUM(CASE WHEN ps.PizzaSizeName = 'medium' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS medium,\n" +
					"    SUM(CASE WHEN ps.PizzaSizeName = 'large' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS large,\n" +
					"    SUM(CASE WHEN ps.PizzaSizeName = 'xlarge' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS xlarge\n" +
					"FROM\n" +
					"    topping AS t\n" +
					"LEFT JOIN\n" +
					"    toppingamount AS ta ON t.ToppingID = ta.ToppingAmountToppingID\n" +
					"LEFT JOIN\n" +
					"    pizzasize AS ps ON ps.PizzaSizeID = ta.ToppingAmountPizzaSizeID\n" +
					"WHERE\n" +
					"	ToppingName = ?\n" +
					"GROUP BY\n" +
					"    t.ToppingID, t.ToppingName, t.ToppingCustomerPrice, t.ToppingCompanyCost, t.ToppingMinInventory, t.ToppingCurInventory;";
			os = conn.prepareStatement(query);
			os.setString(1, name);
			rset = os.executeQuery();

			if (rset.next()) { // Changed this line to call the next() method on the ResultSet
				int TopID = rset.getInt("ToppingID");
				String TopName = rset.getString("ToppingName");
				double PerAMT = rset.getDouble("small");
				double MedAMT = rset.getDouble("medium");
				double LgAMT = rset.getDouble("large");
				double XLAMT = rset.getDouble("xlarge");
				double CustPrice = rset.getDouble("ToppingCustomerPrice");
				double BusPrice = rset.getDouble("ToppingCompanyCost");
				int MinINVT = rset.getInt("ToppingMinInventory");
				int CurINVT = rset.getInt("ToppingCurInventory");

				Topping topping =new Topping(TopID, TopName, PerAMT, MedAMT, LgAMT, XLAMT, CustPrice, BusPrice, MinINVT, CurINVT);

				conn.close();
				return topping;
			}

			// Added this section to handle the case where there's no result
			conn.close();
			return null;

		} catch (SQLException e) { // Caught the SQLException
			// Handling exception without modifying method signature
			System.err.println("SQL Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}


	public static void addToInventory(Topping t, double quantity) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Updates the quantity of the topping in the database by the amount specified.
		 * 
		 * */

		/*
		ArrayList<Topping> toppings = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT \n" +
				"    t.ToppingID AS ToppingID,\n" +
				"    t.ToppingName AS ToppingName,\n" +
				"    t.ToppingCustomerPrice AS ToppingCustomerPrice,\n" +
				"    t.ToppingCompanyCost AS ToppingCompanyCost,\n" +
				"    t.ToppingMinInventory AS ToppingMinInventory,\n" +
				"    t.ToppingCurInventory AS ToppingCurInventory,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'small' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS small,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'medium' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS medium,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'large' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS large,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'xlarge' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS xlarge\n" +
				"FROM\n" +
				"    topping AS t\n" +
				"LEFT JOIN\n" +
				"    toppingamount AS ta ON t.ToppingID = ta.ToppingAmountToppingID\n" +
				"LEFT JOIN\n" +
				"    pizzasize AS ps ON ps.PizzaSizeID = ta.ToppingAmountPizzaSizeID\n" +
				"GROUP BY\n" +
				"    t.ToppingID, t.ToppingName, t.ToppingCustomerPrice, t.ToppingCompanyCost, t.ToppingMinInventory, t.ToppingCurInventory\n" +
				"ORDER BY\n" +
				"    t.ToppingName;\n";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		while (rset.next()) {
			int TopID = rset.getInt("ToppingID");
			String TopName = rset.getString("ToppingName");
			double PerAMT = rset.getDouble("small");
			double MedAMT = rset.getDouble("medium");
			double LgAMT = rset.getDouble("large");
			double XLAMT = rset.getDouble("xlarge");
			double CustPrice = rset.getDouble("ToppingCustomerPrice");
			double BusPrice = rset.getDouble("ToppingCompanyCost");
			int MinINVT = rset.getInt("ToppingMinInventory");
			int CurINVT = rset.getInt("ToppingCurInventory");

			toppings.add(new Topping(TopID, TopName, PerAMT, MedAMT, LgAMT, XLAMT, CustPrice, BusPrice, MinINVT, CurINVT));

		}

		// Find the topping that matches the input
		for (Topping top : toppings) {
			if (top.getTopID() == t.getTopID()) {
				// Calculate the new inventory
				double newInventory = top.getCurINVT() + quantity;

				// Prepare the query to update the topping's inventory
				query = "UPDATE topping SET ToppingCurInventory = ? WHERE ToppingID = ?";
				os = conn.prepareStatement(query);
				os.setDouble(1, newInventory);
				os.setInt(2, top.getTopID());

				// Execute the update
				os.executeUpdate();


				break; // Exit the loop once the topping has been found and updated
			}
		}

		conn.close();

		 */

		double newInventory = t.getCurINVT() + quantity;

		// Prepare the query to update the topping's inventory
		PreparedStatement os;
		String query = "UPDATE topping SET ToppingCurInventory = ? WHERE ToppingID = ?";
		os = conn.prepareStatement(query);
		os.setDouble(1, newInventory);
		os.setInt(2, t.getTopID());

		// Execute the update
		os.executeUpdate();
	}
	
	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database fro the base customer price for that size and crust pizza.
		 * 
		*/
		double price = 0.0;
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT \n" +
				"    PizzaBaseCustomerPrice\n" +
				"FROM\n" +
				"    pizzabase\n" +
				"        LEFT JOIN\n" +
				"    pizzasize ON PizzaBasePizzaSizeID = PizzaSizeID\n" +
				"        LEFT JOIN\n" +
				"    pizzacrust ON PizzaCrustID = PizzaBaseCrustPizzaCrustID\n" +
				"WHERE\n" +
				"    PizzaCrustName =?\n" +
				"        AND PizzaSizeName =?";

		os = conn.prepareStatement(query);
		os.setString(1, crust);
		os.setString(2, size);
		rset = os.executeQuery();

		while(rset.next()) {
			price = rset.getDouble("PizzaBaseCustomerPrice");
		}
		conn.close();
		return price;
		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database fro the base business price for that size and crust pizza.
		 * 
		*/
		double price = 0.0;
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT \n" +
				"    PizzaBaseCompanyCost\n" +
				"FROM\n" +
				"    pizzabase\n" +
				"        LEFT JOIN\n" +
				"    pizzasize ON PizzaBasePizzaSizeID = PizzaSizeID\n" +
				"        LEFT JOIN\n" +
				"    pizzacrust ON PizzaCrustID = PizzaBaseCrustPizzaCrustID\n" +
				"WHERE\n" +
				"    PizzaCrustName =?\n" +
				"        AND PizzaSizeName =?";

		os = conn.prepareStatement(query);
		os.setString(1, crust);
		os.setString(2, size);
		rset = os.executeQuery();

		while(rset.next()) {
			price = rset.getDouble("PizzaBaseCompanyCost");
		}
		conn.close();
		return price;
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Queries the database and prints the current topping list with quantities.
		 *  
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */
		ArrayList<Topping> toppings = new ArrayList<>();
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "SELECT \n" +
				"    t.ToppingID AS ToppingID,\n" +
				"    t.ToppingName AS ToppingName,\n" +
				"    t.ToppingCustomerPrice AS ToppingCustomerPrice,\n" +
				"    t.ToppingCompanyCost AS ToppingCompanyCost,\n" +
				"    t.ToppingMinInventory AS ToppingMinInventory,\n" +
				"    t.ToppingCurInventory AS ToppingCurInventory,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'small' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS small,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'medium' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS medium,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'large' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS large,\n" +
				"    SUM(CASE WHEN ps.PizzaSizeName = 'xlarge' THEN ta.ToppingAmountUnitQty ELSE 0 END) AS xlarge\n" +
				"FROM\n" +
				"    topping AS t\n" +
				"LEFT JOIN\n" +
				"    toppingamount AS ta ON t.ToppingID = ta.ToppingAmountToppingID\n" +
				"LEFT JOIN\n" +
				"    pizzasize AS ps ON ps.PizzaSizeID = ta.ToppingAmountPizzaSizeID\n" +
				"GROUP BY\n" +
				"    t.ToppingID, t.ToppingName, t.ToppingCustomerPrice, t.ToppingCompanyCost, t.ToppingMinInventory, t.ToppingCurInventory\n" +
				"ORDER BY\n" +
				"    t.ToppingID;\n";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		while (rset.next()) {
			int TopID = rset.getInt("ToppingID");
			String TopName = rset.getString("ToppingName");
			double PerAMT = rset.getDouble("small");
			double MedAMT = rset.getDouble("medium");
			double LgAMT = rset.getDouble("large");
			double XLAMT = rset.getDouble("xlarge");
			double CustPrice = rset.getDouble("ToppingCustomerPrice");
			double BusPrice = rset.getDouble("ToppingCompanyCost");
			int MinINVT = rset.getInt("ToppingMinInventory");
			int CurINVT = rset.getInt("ToppingCurInventory");

			toppings.add(new Topping(TopID, TopName, PerAMT, MedAMT, LgAMT, XLAMT, CustPrice, BusPrice, MinINVT, CurINVT));

		}
		System.out.println(String.format("%-5s %-20s %s", "ID", "Name", "CurINVT"));
		for (Topping topping : getToppingList()) {
			System.out.println(String.format("%-5d %-20s %d", topping.getTopID(), topping.getTopName(), topping.getCurINVT()));
		}
		conn.close();

		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION


	}
	
	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 *
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 */
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "Select * From ToppingPopularity;";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		System.out.println(String.format("%-20s %-15s", "Topping", "ToppingCount"));
		while (rset.next()) {
			System.out.println(String.format("%-20s %-15s", rset.getString(1), rset.getString(2)));
		}
		conn.close();
	}

	public static void printProfitByPizzaReport() throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "Select * From ProfitByPizza;";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		System.out.println(String.format("%-20s %-15s %-15s %-18s", "PizzaCrust",
				"PizzaSize", "Profit", "LastOrderDate"));
		while (rset.next()) {
			System.out.println(String.format("%-20s %-15s %-15s %-18s", rset.getString(1),
					rset.getString(2), rset.getString(3), rset.getString(4)));
		}

		conn.close();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void printProfitByOrderType() throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */
		PreparedStatement os;
		ResultSet rset;
		String query;
		query = "Select * From ProfitByOrderType;";
		os = conn.prepareStatement(query);
		rset = os.executeQuery();

		System.out.println(String.format("%-20s %-15s %-15s %-18s %-18s", "CustomerType",
				"OrderMonth", "TotalOrderPrice", "TotalOrderCost", "Profit"));
		while (rset.next()) {
			System.out.println(String.format("%-20s %-15s %-15s %-18s %-18s", rset.getString(1),
					rset.getString(2), rset.getString(3), rset.getString(4),
					rset.getString(5)));
		}

		conn.close();
		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION	
	}
	
	public static String getCustomerName(int CustID) throws SQLException, IOException
	{
	/*
		 * This is a helper method to fetch and format the name of a customer
		 * based on a customer ID. This is an example of how to interact with 
		 * your database from Java.  It's used in the model solution for this project...so the code works!
		 * 
		 * OF COURSE....this code would only work in your application if the table & field names match!
		 *
		 */

		 connect_to_db();

		/* 
		 * an example query using a constructed string...
		 * remember, this style of query construction could be subject to sql injection attacks!
		 * 
		 */
		/*
		String cname1 = "";
		String query = "Select FName, LName From customer WHERE CustID=" + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		
		while(rset.next())
		{
			cname1 = rset.getString(1) + " " + rset.getString(2);
		}
*/
		/* 
		* an example of the same query using a prepared statement...
		* 
		*/
		String cname2 = "";
		PreparedStatement os;
		ResultSet rset2;
		String query2;
		query2 = "Select CustomerFirstName, CustomerLastName From customer WHERE CustomerID=?;";
		os = conn.prepareStatement(query2);
		os.setInt(1, CustID);
		rset2 = os.executeQuery();
		while(rset2.next())
		{
			cname2 = rset2.getString("CustomerFirstName") + " " + rset2.getString("CustomerLastName"); // note the use of field names in the getSting methods
		}

		conn.close();
		return cname2; // OR cname2
	}

	/*
	 * The next 3 private methods help get the individual components of a SQL datetime object. 
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0,4));
	}
	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}
	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder)
	{
		if(getYear(dateOfOrder) > year)
			return true;
		else if(getYear(dateOfOrder) < year)
			return false;
		else
		{
			if(getMonth(dateOfOrder) > month)
				return true;
			else if(getMonth(dateOfOrder) < month)
				return false;
			else
			{
				if(getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
		}
	}


}