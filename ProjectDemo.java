import java.util.*;
import java.sql.*;

// class called User to handle user informations
class User {
    // private data fields for username and password
    private String username;
    private String password;

    // static data field to access username of logged user
    static String getCurrentUser;

    // default constructor
    User() {
    }

    // set method for username
    public void setUsername(String username) {
        this.username = username;
        getCurrentUser = username;
    }

    // set method for password
    public void setPassword(String password) {
        this.password = password;
    }

    // get method for username
    public String getUsername() {
        return username;
    }
}

// class called Meals to handle meal information
class Meals {
    // default constructor
    Meals(){}

    // parametrized constructor
    Meals(String name, int count, double pricePerServing){
        this.count = count;
        this.name = name;
        this.pricePerServing = pricePerServing;
    }

    String name;
    double pricePerServing;
    int count;

    // static method fetchData to get meal info that corresponds to parameter called 'name'
    static Meals fetchData(String name){
        Meals meal = new Meals();
        try {
            // JDBC and MySQL Database connection API
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            // statement object that creates new session with database
            Statement statement = connenction.createStatement();

            // query to fetch info of meal that is name is equal to 'name'
            String query = "select count, name, pricePerServing from meals where name='"+name+"'";

            // ResultSet object to store result data that is recieved from database in that object
            ResultSet result = statement.executeQuery(query);

            // iterates until result reaches the end
            while (result.next()) {

                int count = result.getInt("count"); // stores count column value to 'count' variable
                String mealName = result.getString("name"); // stores name column value to 'name' variable
                double pricePerServing = result.getDouble("pricePerServing"); // stores pricePerServing column value to 'pricePerServing' variable

                meal = new Meals(mealName, count, pricePerServing); // assigns meal new object with parametrized constructor
            }
            connenction.close(); // closes connection with database
        } catch (SQLException e) {
            System.out.println(e); // expception handling that is related to SQL queries
        }
        return meal; // returns object of Meals class
    }

    // static method to show all meals that is currently available
    static List<Meals> showAvailableMeals() {
        List<Meals> meals = new ArrayList<Meals>();
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            Statement statement = connenction.createStatement();

            String query = "select count, name, pricePerServing from meals";

            ResultSet result = statement.executeQuery(query);

            System.out.println("\nAvaialable Meals:");

            // counter variable to count length of rows
            int rowCount = 0;

            while (result.next()) {

                int count = result.getInt("count");
                String name = result.getString("name");
                double pricePerServing = result.getDouble("pricePerServing");

                // prints all info of meals
                System.out.println("\n" + (rowCount + 1) + ". Meal: " + name + ";\n   Number of Servings: " + count
                        + ";\n   Price per Serving: " + pricePerServing + " UZS.\n");

                meals.add(new Meals(name, count, pricePerServing)); // creates list of meals based on results

                ++rowCount;
            }

            connenction.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return meals;
    }

    // static method updateCount to handle quantity of meals after ordering/cancelling
    static void updateCount(int counter, String name){
        // updates if and only if counter is equal or more than zero
        if(counter >= 0){
            try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            String query = "update meals set count='" + counter + "' where name='" + name + "';";

            Statement statement = connenction.createStatement();

            statement.executeUpdate(query);

            connenction.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}

class Orders {

    // helper method to avoid repitition
    private static void helper(int index, List<Meals> meals){
        if(meals.get(index).count > 0){
            System.out.println("You ordered " + meals.get(index).name + " price is "+ meals.get(index).pricePerServing);
            int newCount = meals.get(index).count - 1;
            Meals.updateCount(newCount, meals.get(index).name); // handles quantity
            addToOrders(meals.get(index).name, meals.get(index).pricePerServing); // adds to order table
        }else{
            System.out.println("\nThis meal is currently UNAVAILABLE!\n"); // case when quantity of meals reached the zero
        }
    }

    // static method to insert to orders table
    static void addToOrders(String meal, double price){
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            // query for prepared statement
            String query = "insert into orders (orderedBy, totalPrice, meal)" + " values (?, ?, ?)"; 

            PreparedStatement preparedStmt = connenction.prepareStatement(query); // prepared statement

            preparedStmt.setString(1, User.getCurrentUser); // replaces first ? with current user`s name
            preparedStmt.setDouble(2, price); // replaces 1st and 2nd ? with price, meal name
            preparedStmt.setString(3, meal);
            preparedStmt.execute(); // executes queries

            connenction.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    // making order based on available meals
    static void orderLunch() {
        Scanner sc4 = new Scanner(System.in);
        List<Meals> meals = Meals.showAvailableMeals();

        System.out.println("Please choose a meal from above list (enter corresponding index): ");
        
        int choice = sc4.nextInt();

        if(choice < 6 && choice > 0)
        {
            switch(choice){
             case 1:{
                helper(0, meals);
             }
             break;
             case 2:{
                helper(1, meals);
             }
             break;
             case 3:{
                helper(2, meals);
             }
             break;
             case 4:{
                helper(3, meals);
             }
             break;
             case 5:{
                helper(4, meals);
             }
             break;
        }
    }else{
        System.out.println("[ERROR] Value that is out of range is chosen! Try Again!\n");
        orderLunch();
    }
}

    // static method to display all orders
    static List<Meals> showOrders(){
        List<Meals> orders = new ArrayList<Meals>();
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            Statement statement = connenction.createStatement();

            String query = "select totalPrice, meal, count from orders where orderedBy='"+User.getCurrentUser+"'";

            ResultSet result = statement.executeQuery(query);

            System.out.println("\nAll Orders:");

            int rowCount = 0;
            double totalPaymentAmount = 0;

            while (result.next()) {

                String meal = result.getString("meal");
                double totalPrice = result.getDouble("totalPrice");
                int count = result.getInt("count");

                System.out.println("\n" + (rowCount+1) + ". Meal: " + meal + "\n   Price " + totalPrice + " UZS"+"\n   Order ID: "+count);

                orders.add(new Meals(meal, count, totalPrice));
                totalPaymentAmount=totalPaymentAmount+totalPrice;
                ++rowCount;
            }

            if(rowCount>0)
            {
                System.out.println("\nTotal Price: " + totalPaymentAmount + " UZS");
            }else{
                System.out.println("Order List is Empty");
            }

            System.out.println("\n" + rowCount + " order(s) found");
            connenction.close();
        } catch (SQLException e) {
            System.out.println(e); 
        }
        return orders;
    }

    //  static method to cancel existing order
    static void cancelOrder(int count){
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            Statement statement = connenction.createStatement();

            // hardcoded MySQL query for deleting
            String query = "Delete from orders where count =" + count + ";";

            statement.executeUpdate(query);

            showOrders();

            connenction.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}

class LoginSignup {
    // static method to handle user Signup
    static void signUp(String username, String password) {
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            String query = "insert into users (username, password)" + " values (?, ?)";

            PreparedStatement preparedStmt = connenction.prepareStatement(query);

            preparedStmt.setString(1, username);
            preparedStmt.setString(2, password);
            boolean isSuccess = preparedStmt.execute();

            if (!isSuccess) {
                System.out.println("\nUser added successfully!\n");
            }

            connenction.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    // static method to handle user login
    static User login(String username, String password) {
        User user = new User();
        try {
            Connection connenction = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Canteen_DB?autoReconnect=true&useSSL=false", "root", "Axsj_2206");

            Statement statement = connenction.createStatement();

            String query = "select username, password from users where username='" + username + "'";

            ResultSet result = statement.executeQuery(query);

            int rowCount = 0;

            while (result.next()) {
                String receivedUsername = result.getString("username");
                String receivedPassword = result.getString("password");

                if (password.equals(receivedPassword)) {
                    user.setUsername(receivedUsername);
                    user.setPassword(password);
                }
                ++rowCount;
            }
            System.out.println("\n" + rowCount + " user(s) found!\n");
        } catch (SQLException e) {
            System.out.println(e);
        }
        return user;
    }
}

class ProjectDemo {
    // submenu
    static void menu() {
        int choice = 0;
        Scanner sc1 = new Scanner(System.in);
        System.out.println("->  Order Page  <-\n");
        System.out.println("Press:\n");
        System.out.println("<1> Order Lunch");
        System.out.println("<2> Order History");
        System.out.println("<3> Cancel Orders");
        System.out.println("<4> Logout");
        System.out.println("\nYour choice:  ");

        try {
            choice = sc1.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("[ERROR] " + e.getMessage()); // input mismatch exception that is triggered whenever other data types are entered by user 
            driverMethod();
        }

        if(choice>0 && choice <5){
            switch (choice) {
                case 1: {
                    Orders.orderLunch();
                    System.out.println("\n");
                    menu();
                }
                break;
                case 2:{
                    Orders.showOrders();
                    System.out.println("\n");
                    menu();
                }
                break;
                case 3:{
                    Scanner sc5 = new Scanner(System.in);
        
                    List<Meals> meals = Orders.showOrders();
                    System.out.println("\nPlease choose a meal to DELETE from orders list (enter corresponding index): ");
                    int index = sc5.nextInt();
        
                    Orders.cancelOrder(meals.get(index-1).count);
                    Meals.updateCount(Meals.fetchData(meals.get(index-1).name).count+1, meals.get(index-1).name); // increments meal counter to new value, as user cancelled his/her order

                    System.out.println("\n");
                    menu();
                }
                    break;
                    case 4:
                    {
                        System.out.println("\nYou have successfully logged out!");
                        driverMethod();
                    }
                }
        }else{
            System.out.println("\n[ERROR] Please enter numbers between 1 - 4! (both 1 and 4 inclusive)\n"); // error when rangi is incorrect
            menu();
        }
    }

    // menu/ login - signup page
    static void driverMethod() {
        int choice = 0;
        Scanner sc2 = new Scanner(System.in);
        System.out.println("\n->  IUT Canteen  <-\n");
        System.out.println("Press:\n");
        System.out.println("<1> Login");
        System.out.println("<2> Sign Up");
        System.out.println("<3> EXIT");
        System.out.println("\nYour choice:    ");

        try {
            choice = sc2.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("[ERROR] " + e.getMessage());
            driverMethod();
        }

        if(choice>0 && choice<4){
            switch (choice) {
                case 1: {
                    Scanner sc3 = new Scanner(System.in);
                    System.out.println("\nEnter your USERNAME: ");
                    String username = sc3.nextLine();
                    System.out.println("\nEnter your PASSWORD: ");
                    String password = sc3.nextLine();
        
                    User user = LoginSignup.login(username, password);
        
                    if (user.getUsername() != null) {
                        System.out.println("Login Success!\n");
                        menu();
        
                    } else {
                        System.out.println("\n[ERROR] Login failed, try again!\n");
                        driverMethod();
                    }
                }
                    break;
                case 2: {
                    Scanner sc4 = new Scanner(System.in);
                    System.out.println("\nEnter your USERNAME: ");
                    String username = sc4.nextLine();
                    System.out.println("\nEnter your PASSWORD: ");
                    String password = sc4.nextLine();
        
                    LoginSignup.signUp(username, password);
        
                    System.out.println("\n");
                    driverMethod();
                }
                    break;
                    case 3:{
                        System.out.println("\nThanks for using our program");
                        System.exit(0);
                    }
                }
        }

        
    }

    public static void main(String[] args) {
        driverMethod();
    }
}