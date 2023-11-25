package cpsc4620;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static cpsc4620.DBNinja.*;

/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the methods for each of the menu options.
 * 
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 * 
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {

		System.out.println("Welcome to Pizzas-R-Us!");
		
		int menu_option = 0;

		// present a menu of options and take their selection
		
		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		//Autograder.checkSeed("eec2d78a0957459cb5e841a8760b4e99");
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
			case 1:// enter order
				EnterOrder();
				break;
			case 2:// view customers
				viewCustomers();
				break;
			case 3:// enter customer
				EnterCustomer();
				break;
			case 4:// view order
				// open/closed/date
				ViewOrders();
				break;
			case 5:// mark order as complete
				MarkOrderAsComplete();
				break;
			case 6:// view inventory levels
				ViewInventoryLevels();
				break;
			case 7:// add to inventory
				AddInventory();
				break;
			case 8:// view reports
				PrintReports();
				break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException 
	{

		/*
		 * EnterOrder should do the following:
		 * 
		 * Ask if the order is delivery, pickup, or dinein
		 *   if dine in....ask for table number
		 *   if pickup...
		 *   if delivery...
		 * 
		 * Then, build the pizza(s) for the order (there's a method for this)
		 *  until there are no more pizzas for the order
		 *  add the pizzas to the order
		 *
		 * Apply order discounts as needed (including to the DB)
		 * 
		 * return to menu
		 * 
		 * make sure you use the prompts below in the correct order!
		 */
         String address = "temp";
		 Customer customer = getLastCustomer();
		 int customerID = customer.getCustID();
		 int tableID = 0;
		 Order lastOrder = getLastOrder();
		 int lastOrderID = lastOrder.getOrderID();
		 ArrayList<Discount> discounts = getDiscountList();
		ArrayList<Customer> customers = getCustomerList();
		double custPrice = 0.0;
		double busPrice = 0.0;
		Date date = new Date();
		String orderDiscount = "n";

		 // User Input Prompts...
		System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		String orderType = reader.readLine();
		switch (orderType) {
			case "1":
				customerID = 1;
				System.out.println("What is the table number for this order?");
				String tableIDString = reader.readLine();
				tableID = Integer.parseInt(tableIDString);
				break;
			case "2":
				System.out.println("Is this order for an existing customer? Answer y/n: ");
				String pickUpCustomer = reader.readLine();
				switch (pickUpCustomer) {
					case "y":
						System.out.println("Here's a list of the current customers: ");
						for (Customer customer1: customers) {
							System.out.println(customer1);
						}
						System.out.println("Which customer is this order for? Enter ID Number:");
						customerID = Integer.parseInt(reader.readLine());
						break;
					case "n":
						EnterCustomer();
						customerID += 1;
						break;
				}
				break;
			case "3":
				System.out.println("Is this order for an existing customer? Answer y/n: ");
				String DeliveryCustomer = reader.readLine();
				switch (DeliveryCustomer) {
					case "y":
						System.out.println("Here's a list of the current customers: ");
						for (Customer customer1: customers) {
							System.out.println(customer1);
						}
						System.out.println("Which customer is this order for? Enter ID Number:");
						customerID = Integer.parseInt(reader.readLine());
						break;
					case "n":
						EnterCustomer();
						customerID += 1;
						System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
						String house = reader.readLine();
						System.out.println("What is the Street for this order? (e.g., Smile Street)");
						String street  = reader.readLine();
						System.out.println("What is the City for this order? (e.g., Greenville)");
						String city = reader.readLine();
						System.out.println("What is the State for this order? (e.g., SC)");
						String state = reader.readLine();
						System.out.println("What is the Zip Code for this order? (e.g., 20605)");
						String zip = reader.readLine();
						address = house + " " + street + " " + city + " " + state + " " + zip;
						break;
					default:
						System.out.println("ERROR: I don't understand your input for: Is this order an existing customer?");
				}
				break;
		}

		switch (orderType){
			case "1":
				DineinOrder dineinOrder = new DineinOrder(lastOrderID + 1, 1, "2023-8-02", 0, 0, 0, tableID);

				System.out.println("Let's build a pizza!");
				dineinOrder.addPizza(buildPizza(lastOrderID + 1));
				System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
				String pizzaBuilderString = reader.readLine();
				int pizzaBuilder = Integer.parseInt(pizzaBuilderString);
				while (pizzaBuilder != -1) {
					dineinOrder.addPizza(buildPizza(lastOrderID + 1));
					System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
					pizzaBuilderString = reader.readLine();
					pizzaBuilder = Integer.parseInt(pizzaBuilderString);

				}


				System.out.println("Do you want to add discounts to this order? Enter y/n?");
				orderDiscount = reader.readLine();
				while (Objects.equals(orderDiscount, "y")) {
					for (Discount discount: discounts) {
						System.out.println(discount);
					}
					System.out.println("Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
					String orderDiscountIDString = reader.readLine();
					int orderDiscountID = Integer.parseInt(orderDiscountIDString);
					if (orderDiscountID != -1) {
						for (Discount discount: discounts) {
							if (discount.getDiscountID() == orderDiscountID) {
								dineinOrder.addDiscount(discount);
								//useOrderDiscount(dineinOrder, discount);
							}
						}
					}
					else {
						break;
					}

				}
				System.out.println("Finished adding order...Returning to menu...");

				// Updating the dollar value for the order
				custPrice = 0.0;
				busPrice = 0.0;

				for (Pizza pizza: dineinOrder.getPizzaList()) {
					custPrice += pizza.getCustPrice();
					busPrice += pizza.getBusPrice();
				}
				dineinOrder.setCustPrice(custPrice);
				dineinOrder.setBusPrice(busPrice);
				addOrder(dineinOrder);
				break;
			case "2":
				PickupOrder pickupOrder = new PickupOrder(lastOrderID + 1, customerID, "2022-08-02", 0, 0, 0, 0);

				System.out.println("Let's build a pizza!");
				pickupOrder.addPizza(buildPizza(lastOrderID + 1));
				System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
				pizzaBuilderString = reader.readLine();
				pizzaBuilder = Integer.parseInt(pizzaBuilderString);
				while (pizzaBuilder != -1) {
					pickupOrder.addPizza(buildPizza(lastOrderID + 1));
					System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
					pizzaBuilderString = reader.readLine();
					pizzaBuilder = Integer.parseInt(pizzaBuilderString);
					//System.out.println(pizzaBuilder);
				}


				System.out.println("Do you want to add discounts to this order? Enter y/n?");
				orderDiscount = reader.readLine();
				while (Objects.equals(orderDiscount, "y")) {
					for (Discount discount: discounts) {
						System.out.println(discount);
					}
					System.out.println("Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
					String orderDiscountIDString = reader.readLine();
					int orderDiscountID = Integer.parseInt(orderDiscountIDString);
					if (orderDiscountID != -1) {
						for (Discount discount: discounts) {
							if (discount.getDiscountID() == orderDiscountID) {
								pickupOrder.addDiscount(discount);
								//useOrderDiscount(pickupOrder, discount);
							}
						}
					}
					else {
						break;
					}

				}
				System.out.println("Finished adding order...Returning to menu...");
				custPrice = 0.0;
				busPrice = 0.0;

				for (Pizza pizza: pickupOrder.getPizzaList()) {
					custPrice += pizza.getCustPrice();
					busPrice += pizza.getBusPrice();
				}
				pickupOrder.setCustPrice(custPrice);
				pickupOrder.setBusPrice(busPrice);
				addOrder(pickupOrder);
				break;
			case "3":
				DeliveryOrder deliveryOrder = new DeliveryOrder(lastOrderID + 1, customerID, "2022-08-02", 0, 0, 0, address);

				System.out.println("Let's build a pizza!");
				deliveryOrder.addPizza(buildPizza(lastOrderID + 1));
				System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
				pizzaBuilderString = reader.readLine();
				pizzaBuilder = Integer.parseInt(pizzaBuilderString);
				while (pizzaBuilder != -1) {
					deliveryOrder.addPizza(buildPizza(lastOrderID + 1));
					System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
					pizzaBuilderString = reader.readLine();
					pizzaBuilder = Integer.parseInt(pizzaBuilderString);

				}


				System.out.println("Do you want to add discounts to this order? Enter y/n?");
				orderDiscount = reader.readLine();
				while (Objects.equals(orderDiscount, "y")) {
					for (Discount discount: discounts) {
						System.out.println(discount);
					}
					System.out.println("Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
					String orderDiscountIDString = reader.readLine();
					int orderDiscountID = Integer.parseInt(orderDiscountIDString);
					if (orderDiscountID != -1) {
						for (Discount discount: discounts) {
							if (discount.getDiscountID() == orderDiscountID) {
								deliveryOrder.addDiscount(discount);
								//useOrderDiscount(deliveryOrder, discount);
							}
						}
					}
					else {
						break;
					}

				}
				System.out.println("Finished adding order...Returning to menu...");
				custPrice = 0.0;
				busPrice = 0.0;

				for (Pizza pizza: deliveryOrder.getPizzaList()) {
					custPrice += pizza.getCustPrice();
					busPrice += pizza.getBusPrice();
				}
				deliveryOrder.setCustPrice(custPrice);
				deliveryOrder.setBusPrice(busPrice);
				addOrder(deliveryOrder);
				break;
		}


	}
	
	
	public static void viewCustomers() throws SQLException, IOException 
	{
		/*
		 * Simply print out all of the customers from the database. 
		 */

		for (Customer customer : getCustomerList()) {
			System.out.println(customer);
		}
		Date date = new Date();
		Date date1 = new Date();
		System.out.println(date);
	}
	

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException 
	{
		/*
		 * Ask for the name of the customer:
		 *   First Name <space> Last Name
		 * 
		 * Ask for the  phone number.
		 *   (##########) (No dash/space)
		 * 
		 * Once you get the name and phone number, add it to the DB
		 */
		
		// User Input Prompts...
		System.out.println("What is this customer's name (first <space> last)");
		String name = reader.readLine();
		String[] parts = name.split(" ");
		String firstName = parts[0];
		String lastName = parts[1];

		System.out.println("What is this customer's phone number (##########) (No dash/space)");
		String phone = reader.readLine();

		addCustomer(new Customer (0, firstName, lastName, phone));

	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException 
	{
		/*  
		* This method allows the user to select between three different views of the Order history:
		* The program must display:
		* a.	all open orders
		* b.	all completed orders 
		* c.	all the orders (open and completed) since a specific date (inclusive)
		* 
		* After displaying the list of orders (in a condensed format) must allow the user to select a specific order for viewing its details.  
		* The details include the full order type information, the pizza information (including pizza discounts), and the order discounts.
		* 
		*/


			ArrayList<Order> orders = new ArrayList<>();
		
		// User Input Prompts...
		System.out.println("Would you like to:\n(a)display all orders [open or closed]\n(b) display all open orders\n(c) display all completed orders\n(d) display orders since a specific date");
		String choice = reader.readLine();

		switch (choice) {
			case "a":
				orders = getOrders(false);
				break;
			case "b":
				orders = getOrders(true);
				break;
			case "c":
				ArrayList<Order> tempOrders = getOrders(false);
				for (Order orderTemp: tempOrders) {
					if (orderTemp.getIsComplete() == 1) {
						orders.add(orderTemp);
					}
				}
				break;
			case "d":
				System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
				String date = reader.readLine();
				orders = getOrdersAfterDate(date);
				break;
			default:
				System.out.println("I don't understand that input, returning to menu");
				break;
		}
		if (orders.isEmpty()) {
			System.out.println("No orders to display, returning to menu.");
		}
		else {

			for (Order order: orders) {
				System.out.println(order.toSimplePrint());
			}

			System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
			String orderIDString = reader.readLine();
			int orderID = Integer.parseInt(orderIDString);

			if (orderID == -1) {
				System.out.println("Exiting, returning to menu.");
				return;
			}

			boolean orderFound = false;
			for (Order order : orders) {
				if (order.getOrderID() == orderID) {
					System.out.println(order);
					if (order.getDiscountList().isEmpty()) {
						System.out.println("NO ORDER DISCOUNTS");
					}
					else {
						for (Discount discount: order.getDiscountList()) {
							System.out.println("ORDER DISCOUNTS: " + discount);
						}
					}
					ArrayList<Pizza> pizzas = order.getPizzaList();
					for (Pizza pizza: pizzas) {
						if (Objects.equals(pizza.getPizzaState(), "0")) {
							pizza.setPizzaState("open");
						}
						else {
							pizza.setPizzaState("completed");
						}
						System.out.println(pizza);
						if (pizza.getDiscounts().isEmpty()) {
							System.out.println("NO PIZZA DISCOUNTS");
						}
						else {
							for (Discount discount: pizza.getDiscounts()) {
								System.out.println("PIZZA DISCOUNTS: " + discount);
							}
						}
					}
					orderFound = true;
					break;
				}
			}

			if (!orderFound) {
				System.out.println("Incorrect entry, returning to menu.");
			}
		}
	}

	
	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException 
	{
		/*
		 * All orders that are created through java (part 3, not the orders from part 2) should start as incomplete
		 * 
		 * When this method is called, you should print all of the "opoen" orders marked
		 * and allow the user to choose which of the incomplete orders they wish to mark as complete
		 * 
		 */
		ArrayList<Order> orders = getOrders(true);
		if (orders.isEmpty()) {
			System.out.println("There are no open orders currently... returning to menu...");

		}
		else {
			for (Order order: orders) {
				System.out.println(order.toSimplePrint());
			}
			System.out.println("Which order would you like mark as complete? Enter the OrderID: ");
			String orderIDString = reader.readLine();
			try {
				int orderID = Integer.parseInt(orderIDString);

				// Search for the order in the list
				Order selectedOrder = null;
				for (Order order : orders) {
					if (order.getOrderID() == orderID) {
						selectedOrder = order;
						break;
					}

				}

				if (selectedOrder != null) {
					completeOrder(selectedOrder);
				} else {
					System.out.println("Incorrect entry, not an option");
				}
			} catch (NumberFormatException e) {
				//System.out.println("Invalid order ID entered. Please enter a valid integer.");
			}


		}
		// User Input Prompts...
	}

	public static void ViewInventoryLevels() throws SQLException, IOException 
	{
		/*
		 * Print the inventory. Display the topping ID, name, and current inventory
		*/
		printInventory();



	}


	public static void AddInventory() throws SQLException, IOException
	{
		/*
		 * This should print the current inventory and then ask the user which topping (by ID) they want to add more to and how much to add
		 */

		Topping topping = null;
		// User Input Prompts...
		printInventory();
		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		String toppingID = reader.readLine();
		System.out.println("How many units would you like to add? ");
		String toppingQTYStr = reader.readLine();
		double toppingQTY = Double.parseDouble(toppingQTYStr);

		switch (toppingID) {
			case "1":
				topping = findToppingByName("Pepperoni");
				break;
			case "2":
				topping = findToppingByName("Sausage");
				break;
			case "3":
				topping = findToppingByName("Ham");
				break;
			case "4":
				topping = findToppingByName("Chicken");
				break;
			case "5":
				topping = findToppingByName("Green Pepper");
				break;
			case "6":
				topping = findToppingByName("Onion");
				break;
			case "7":
				topping = findToppingByName("Roma Tomato");
				break;
			case "8":
				topping = findToppingByName("Mushrooms");
				break;
			case "9":
				topping = findToppingByName("Black Olives");
				break;
			case "10":
				topping = findToppingByName("Pineapple");
				break;
			case "11":
				topping = findToppingByName("Jalapenos");
				break;
			case "12":
				topping = findToppingByName("Banana Peppers");
				break;
			case "13":
				topping = findToppingByName("Regular Cheese");
				break;
			case "14":
				topping = findToppingByName("Four Cheese Blend");
				break;
			case "15":
				topping = findToppingByName("Feta Cheese");
				break;
			case "16":
				topping = findToppingByName("Goat Cheese");
				break;
			case "17":
				topping = findToppingByName("Bacon");
				break;
			default:
				System.out.println("Invalid topping choice.");
				topping = null; // You can assign null or handle this case as needed
				break;
		}

		if (topping != null) {
			addToInventory(topping, toppingQTY);
		}


	}

	// A method that builds a pizza. Used in our add new order method
	public static Pizza buildPizza(int orderID) throws SQLException, IOException 
	{
		
		/*
		 * This is a helper method for first menu option.
		 * 
		 * It should ask which size pizza the user wants and the crustType.
		 * 
		 * Once the pizza is created, it should be added to the DB.
		 * 
		 * We also need to add toppings to the pizza. (Which means we not only need to add toppings here, but also our bridge table)
		 * 
		 * We then need to add pizza discounts (again, to here and to the database)
		 * 
		 * Once the discounts are added, we can return the pizza
		 */
		ArrayList<Topping> toppings = getToppingList();
		ArrayList<Discount> discounts = getDiscountList();
		String sizeID;
		String sizeName = null;
		String crustID;
		String crustName = null;
		String currDate = "2022-08-02";
		String pizzaDiscount;
		int pizzaDiscountID;
		int toppingID;
		String toppingIDString = "0";




		 Pizza ret = null;
		
		// Pizza size
		System.out.println("What size is the pizza?");
		System.out.println("1."+DBNinja.size_s);
		System.out.println("2."+DBNinja.size_m);
		System.out.println("3."+DBNinja.size_l);
		System.out.println("4."+DBNinja.size_xl);
		System.out.println("Enter the corresponding number: ");
		sizeID = reader.readLine();
		switch (sizeID){
			case "1":
				sizeName = size_s;
				break;
			case "2":
				sizeName = size_m;
				break;
			case "3":
				sizeName = size_l;
				break;
			case "4":
				sizeName = size_xl;
				break;
		}

		System.out.println("What crust for this pizza?");
		System.out.println("1."+DBNinja.crust_thin);
		System.out.println("2."+DBNinja.crust_orig);
		System.out.println("3."+DBNinja.crust_pan);
		System.out.println("4."+DBNinja.crust_gf);
		System.out.println("Enter the corresponding number: ");
		crustID = reader.readLine();
		switch (crustID){
			case "1":
				crustName = crust_thin;
				break;
			case "2":
				crustName = crust_orig;
				break;
			case "3":
				crustName = crust_pan;
				break;
			case "4":
				crustName = crust_gf;
				break;
		}

		//get the base price for customer and business
		double cusPrice = getBaseCustPrice(sizeName, crustName);
		double busPrice = getBaseBusPrice(sizeName, crustName);

		//create the pizza
		Pizza pizza = new Pizza(17, sizeName, crustName, orderID, "open", currDate, cusPrice, busPrice);

		//add toppings to pizza
		while (!toppingIDString.equals("-1")) {
			System.out.println("Available Toppings:");
			printInventory();
			System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			toppingIDString = reader.readLine();
			toppingID = Integer.parseInt(toppingIDString);
			if (toppingID == -1) {
				break;
			}
			System.out.println("Do you want to add extra topping? Enter y/n");
			String extraToppingString = reader.readLine();
			boolean extraTopping;
			if (Objects.equals(extraToppingString, "y")) {
				extraTopping = true;
			}
			else {
				extraTopping = false;
			}

			for (Topping topping: toppings) {
				if (topping.getTopID() == toppingID) {
					int currINVT = topping.getCurINVT();
					double desiredAmount = 0;
					switch (sizeID) {
						case "1":
							desiredAmount = topping.getPerAMT();
							break;
						case "2":
							desiredAmount = topping.getMedAMT();
							break;
						case "3":
							desiredAmount = topping.getLgAMT();
							break;
						case "4":
							desiredAmount = topping.getXLAMT();
							break;
					}
					if (extraTopping) {
						desiredAmount *= 2;
					}
					if (currINVT - desiredAmount > topping.getMinINVT()) {
						pizza.addToppings(topping, extraTopping);
						if (extraTopping) {
							pizza.modifyDoubledArray(topping.getTopID()-1, true);
						}
						//useTopping(pizza, topping, extraTopping);
					}
					else {
						System.out.println("We don't have enough of that topping to add it...");
					}
				}
			}
		};

		System.out.println("Do you want to add discounts to this pizza? Enter y/n?");
		pizzaDiscount = reader.readLine();
		while (Objects.equals(pizzaDiscount, "y")) {
			for (Discount discount: discounts) {
				System.out.println(discount);
			}
			System.out.println("Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
			String pizzaDiscountIDString = reader.readLine();
			pizzaDiscountID = Integer.parseInt(pizzaDiscountIDString);
			if (pizzaDiscountID != -1) {
				for (Discount discount: discounts) {
					if (discount.getDiscountID() == pizzaDiscountID) {
						pizza.addDiscounts(discount);
						//usePizzaDiscount(pizza, discount);
					}
				}
			}
			else {
				break;
			}
		}

		//System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
		//System.out.println("Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
		//pizzaDiscountID = reader.readLine();
		//System.out.println("Do you want to add more discounts to this Pizza? Enter y/n?");
		//addDiscount = reader.readLine();
		return pizza;
	}
	
	
	public static void PrintReports() throws SQLException, NumberFormatException, IOException
	{
		/*
		 * This method asks the use which report they want to see and calls the DBNinja method to print the appropriate report.
		 * 
		 */
		String menu_option;

		// User Input Prompts...
		System.out.println("Which report do you wish to print? Enter\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType:");
		String input = reader.readLine();

		switch (input) {
			case "a":
				// Handle the case where "a" is entered
				printToppingPopReport();
				break;
			case "b":
				// Handle the case where "b" is entered
				printProfitByPizzaReport();
				break;
			case "c":
				// Handle the case where "c" is entered
				printProfitByOrderType();
				break;
			default:
				// Handle any other input
				System.out.println("I don't understand that input... returning to menu...");
				break;
		}

	}

	//Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order"); //needs work
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders"); //needs work
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "eec2d78a0957459cb5e841a8760b4e99";

}


