package chatapp3;
import java.util.Scanner;

public class Chatapp3Main {
    
    //Runs registration
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Login login = registerFlow(scanner);
            loginFlow(scanner, login);
        }
    }

    //Handels registration and asks the user for their details
    private static Login registerFlow(Scanner scanner) {
        System.out.println("=== Registration ===");

        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter South African cell phone number (e.g. +27831234567): ");
        String cell = scanner.nextLine();

        Login login = new Login(username, password, cell, firstName, lastName);

        // registerUser() checks username + password internally 
        // (returns the message to show the user)
        System.out.println(login.registerUser());

        // Checks cell phone number and resports it separatley
        if (login.checkCellPhoneNumber(cell)) {
            System.out.println("Cell number successfully captured.");
        } else {
            System.out.println("Cell number is incorrectly formatted or does not "
                    + "contain an international code; please correct the number and try again.");
        }

        return login;
    }
    //Handles login step
    private static void loginFlow(Scanner scanner, Login login) {
        System.out.println("\n=== Login ===");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        boolean success = login.loginUser(username, password);
        System.out.println(login.returnLoginStatus(success));
    }
}