import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Login login = new Login();

        register(sc, login);
        logIn(sc, login);          // only returns once the user has logged in successfully
        runQuickChat(sc);          // so messages can only be sent after a successful login

        sc.close();
    }

    //  Part 1: registration and login 

    private static void register(Scanner sc, Login login) {
        System.out.println("=== REGISTRATION ===");
        System.out.print("First name: ");
        String first = sc.nextLine().trim();
        System.out.print("Last name: ");
        String last = sc.nextLine().trim();

        String result;
        do {
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine();
            System.out.print("Cell number (e.g. +27831234567): ");
            String cell = sc.nextLine().trim();

            result = login.registerUser(first, last, username, password, cell);
            System.out.println(result);
        } while (!result.contains("registered successfully"));
    }

    private static void logIn(Scanner sc, Login login) {
        System.out.println("\n=== LOGIN ===");
        boolean loggedIn;
        do {
            System.out.print("Username: ");
            String u = sc.nextLine().trim();
            System.out.print("Password: ");
            String p = sc.nextLine();

            loggedIn = login.loginUser(u, p);
            System.out.println(login.returnLoginStatus(loggedIn));
        } while (!loggedIn);
    }

    // ---------------- Part 2: QuickChat ----------------

    private static void runQuickChat(Scanner sc) {
        System.out.println("\nWelcome to QuickChat.");
        int limit = readInt(sc, "How many messages do you wish to enter? ", 1, 1000);
        int entered = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            int choice = readInt(sc, "Choose an option: ", 1, 3);

            switch (choice) {
                case 1:
                    if (entered >= limit) {
                        System.out.println("You have already entered your " + limit + " message(s).");
                    } else {
                        sendMessageFlow(sc);
                        entered++;
                        if (entered == limit) {
                            System.out.println("\nAll messages entered. Total messages sent: "
                                    + Message.returnTotalMessages());
                        }
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                default:
                    running = false;
            }
        }

        if (entered < limit) {
            System.out.println("\nTotal messages sent: " + Message.returnTotalMessages());
        }
        System.out.println("Goodbye!");
    }

    private static void sendMessageFlow(Scanner sc) {
        String recipient;
        while (true) {
            System.out.print("Recipient cell number (e.g. +27718693002): ");
            recipient = sc.nextLine().trim();
            System.out.println(Message.validateRecipient(recipient));
            if (Message.isValidRecipient(recipient)) break;
        }

        String text;
        while (true) {
            System.out.print("Message (max 250 characters): ");
            text = sc.nextLine();
            System.out.println(Message.validateLength(text));
            if (Message.isValidLength(text)) break;
        }

        Message msg = new Message(recipient, text);
        System.out.println(msg.messageIdCreated());

        System.out.println("\n1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int option = readInt(sc, "Choose an option: ", 1, 3);

        System.out.println(msg.sentMessage(option));

        if (option == 1) {
            System.out.println("\n" + msg.getDetails());
        } else if (option == 2) {
            while (!sc.nextLine().trim().equals("0")) {
                // keep waiting until the user presses 0
            }
            System.out.println("Message deleted.");
        }
    }

    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // fall through to the error message
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}
