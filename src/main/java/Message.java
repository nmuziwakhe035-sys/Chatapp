import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Message {

    private static final int MAX_LENGTH = 250;

    // Shared state: everything sent / stored while the program is running
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static int messageCounter = 0;
    private static String storagePath = "messages.json";

    private final String messageId;
    private final int numMessagesSent;
    private final String recipient;
    private final String message;

    // Used by the app: ID is auto-generated and the message number is auto-incremented
    public Message(String recipient, String message) {
        this(generateMessageId(), messageCounter++, recipient, message);
    }

    // Used by the unit tests so that known test data can be supplied
    public Message(String messageId, int numMessagesSent, String recipient, String message) {
        this.messageId = messageId;
        this.numMessagesSent = numMessagesSent;
        this.recipient = recipient;
        this.message = message;
    }

    // ID 

    // Randomly generated ten-digit number
    public static String generateMessageId() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    // The message ID must not be more than ten characters
    public boolean checkMessageID() {
        return messageId != null && !messageId.isEmpty() && messageId.length() <= 10;
    }

    public String messageIdCreated() {
        return "Message ID generated: " + messageId;
    }

    // Recipient 

    // Reuses the cell number check from Part 1 (international code + digits)
    public static boolean isValidRecipient(String cell) {
        return new Login().checkCellPhoneNumber(cell);
    }

    public static String validateRecipient(String cell) {
        if (isValidRecipient(cell)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain "
             + "an international code. Please correct the number and try again.";
    }

    public String checkRecipientCell() {
        return validateRecipient(recipient);
    }

    // Message length 

    public static boolean isValidLength(String text) {
        return text != null && !text.isBlank() && text.length() <= MAX_LENGTH;
    }

    public static String validateLength(String text) {
        if (text == null || text.isBlank()) {
            return "Please enter a message of less than 250 characters.";
        }
        if (text.length() > MAX_LENGTH) {
            return "Message exceeds 250 characters by " + (text.length() - MAX_LENGTH)
                 + ", please reduce the size.";
        }
        return "Message ready to send.";
    }

    // Hash 

    // First two digits of ID : message number : FIRSTWORDLASTWORD (all caps)
    public String createMessageHash() {
        String prefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;
        String[] words = message.trim().split("\\s+");
        String first = stripPunctuation(words[0]);
        String last = stripPunctuation(words[words.length - 1]);
        return (prefix + ":" + numMessagesSent + ":" + first + last).toUpperCase();
    }

    private static String stripPunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // Send / discard / store 

    // 1 = send, 2 = disregard, 3 = store for later
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                if (!sentMessages.contains(this)) {
                    sentMessages.add(this);
                }
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                return storeMessage() ? "Message successfully stored."
                                      : "Message could not be stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    // Reporting 

    public String getDetails() {
        return "Message ID: " + messageId
             + "\nMessage Hash: " + createMessageHash()
             + "\nRecipient: " + recipient
             + "\nMessage: " + message;
    }

    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(sentMessages.get(i).getDetails());
        }
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    //  JSON storage 

    // Adds this message to the stored list and rewrites the JSON file
    public boolean storeMessage() {
        storedMessages.add(this);
        try {
            writeStoredMessages();
            return true;
        } catch (IOException e) {
            storedMessages.remove(this);
            return false;
        }
    }

    private static void writeStoredMessages() throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            sb.append(storedMessages.get(i).toJson());
            if (i < storedMessages.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.writeString(Path.of(storagePath), sb.toString(), StandardCharsets.UTF_8);
    }

    private String toJson() {
        return "  {\n"
             + "    \"messageId\": \"" + escape(messageId) + "\",\n"
             + "    \"messageNumber\": " + numMessagesSent + ",\n"
             + "    \"messageHash\": \"" + escape(createMessageHash()) + "\",\n"
             + "    \"recipient\": \"" + escape(recipient) + "\",\n"
             + "    \"message\": \"" + escape(message) + "\"\n"
             + "  }";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    // Getters / test helpers 

    public String getMessageId()      { return messageId; }
    public int getNumMessagesSent()   { return numMessagesSent; }
    public String getRecipient()      { return recipient; }
    public String getMessageText()    { return message; }

    public static void setStoragePath(String path) { storagePath = path; }

    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
        messageCounter = 0;
    }
}
