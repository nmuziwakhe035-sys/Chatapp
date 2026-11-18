import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Login login = new Login();

        register(sc, login);
        logIn(sc, login);          // only returns once the user has logged in successfully
        Message.setCurrentSender(login.getFirstName() + " " + login.getLastName());
        runQuickChat(sc);          // so messages can only be sent after a successful login

        sc.close();
    }

    // ---------------- Part 1: registration and login ----------------

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

        // Read any messages stored on a previous run from messages.json into the stored array
        int loaded = Message.loadStoredMessages();
        if (loaded > 0) {
            System.out.println("Loaded " + loaded + " stored message(s) from messages.json.");
        } else if (loaded < 0) {
            System.out.println("messages.json could not be read, so there are no stored messages loaded.");
        }

        int limit = readInt(sc, "How many messages do you wish to enter? ", 1, 1000);
        int entered = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            int choice = readInt(sc, "Choose an option: ", 1, 4);

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
                case 4:
                    storedMessagesMenu(sc);
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

    // ---------------- Part 3: Stored Messages menu ----------------

    private static void storedMessagesMenu(Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== STORED MESSAGES ===");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a particular recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to the main menu");
            int choice = readInt(sc, "Choose an option: ", 1, 7);

            switch (choice) {
                case 1:
                    System.out.println(Message.displaySenderAndRecipient());
                    break;
                case 2:
                    System.out.println(Message.longestStoredMessage());
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.describeByMessageId(sc.nextLine()));
                    break;
                case 4:
                    System.out.print("Enter the recipient's cell number: ");
                    java.util.List<String> found = Message.searchByRecipient(sc.nextLine());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    } else {
                        for (String text : found) {
                            System.out.println("- " + text);
                        }
                    }
                    break;
                case 5:
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByHash(sc.nextLine()));
                    break;
                case 6:
                    System.out.println(Message.storedMessagesReport());
                    break;
                default:
                    back = true;
            }
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
