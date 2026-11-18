import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Message {

    private static final int MAX_LENGTH = 250;
    private static final String UNKNOWN_SENDER = "Unknown sender";

    // ---------- Arrays (populated while the program runs, nothing is hard-coded) ----------
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIds = new ArrayList<>();

    private static int messageCounter = 0;
    private static String storagePath = "messages.json";
    private static String currentSender = UNKNOWN_SENDER;

    private final String messageId;
    private final int numMessagesSent;
    private final String sender;
    private final String recipient;
    private final String message;

    // Used by the app: ID is auto-generated and the message number is auto-incremented
    public Message(String recipient, String message) {
        this(generateMessageId(), messageCounter++, recipient, message);
    }

    // Used by the unit tests so that known test data can be supplied
    public Message(String messageId, int numMessagesSent, String recipient, String message) {
        this(messageId, numMessagesSent, currentSender, recipient, message);
    }

    // Used when a message is read back from the JSON file (the sender is stored too)
    public Message(String messageId, int numMessagesSent, String sender,
                   String recipient, String message) {
        this.messageId = messageId;
        this.numMessagesSent = numMessagesSent;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    // ---------- ID ----------

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

    // ---------- Recipient ----------

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

    // ---------- Message length ----------

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

    // ---------- Hash ----------

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

    // ---------- Send / disregard / store ----------

    // 1 = send, 2 = disregard, 3 = store for later
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                addOnce(sentMessages, this);
                track();
                return "Message successfully sent.";
            case 2:
                addOnce(disregardedMessages, this);
                track();
                return "Press 0 to delete the message.";
            case 3:
                return storeMessage() ? "Message successfully stored."
                                      : "Message could not be stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    private static void addOnce(List<Message> list, Message m) {
        if (!list.contains(m)) {
            list.add(m);
        }
    }

    // Keeps the Message ID and Message Hash arrays in step with each other
    private void track() {
        if (!messageIds.contains(messageId)) {
            messageIds.add(messageId);
            messageHashes.add(createMessageHash());
        }
    }

    private void untrack() {
        int index = messageIds.indexOf(messageId);
        if (index >= 0) {
            messageIds.remove(index);
            messageHashes.remove(index);
        }
    }

    // ---------- Reporting (Part 2) ----------

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

    // ---------- Part 3: working with the arrays ----------

    // a) Sender and recipient of every stored message
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            if (i > 0) sb.append("\n");
            Message m = storedMessages.get(i);
            sb.append("Sender: ").append(m.sender).append(" | Recipient: ").append(m.recipient);
        }
        return sb.toString();
    }

    // b) The longest message in any list of messages
    public static String findLongestMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No messages found.";
        }
        Message longest = messages.get(0);
        for (Message m : messages) {
            if (m.message.length() > longest.message.length()) {
                longest = m;
            }
        }
        return longest.message;
    }

    public static String longestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        return findLongestMessage(storedMessages);
    }

    // c) Find a message by its ID (looks through the sent, disregarded and stored arrays)
    public static Message findByMessageId(String id) {
        if (id == null) return null;
        String wanted = id.trim();
        for (List<Message> list : List.of(sentMessages, disregardedMessages, storedMessages)) {
            for (Message m : list) {
                if (m.messageId.equals(wanted)) {
                    return m;
                }
            }
        }
        return null;
    }

    public static String describeByMessageId(String id) {
        Message m = findByMessageId(id);
        if (m == null) {
            return "Message ID not found.";
        }
        return "Recipient: " + m.recipient + "\nMessage: " + m.message;
    }

    // d) All messages sent or stored for one recipient
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        if (recipient == null) return found;
        String wanted = recipient.trim();
        for (Message m : sentMessages) {
            if (m.recipient.equals(wanted)) found.add(m.message);
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(wanted)) found.add(m.message);
        }
        return found;
    }

    // e) Delete a stored message using its message hash
    public static String deleteByHash(String hash) {
        if (hash == null) return "Message hash not found.";
        String wanted = hash.trim();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.createMessageHash().equalsIgnoreCase(wanted)) {
                storedMessages.remove(i);
                try {
                    writeStoredMessages();
                } catch (IOException e) {
                    storedMessages.add(i, m);
                    return "Message could not be deleted.";
                }
                m.untrack();
                return "Message \"" + m.message + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    // f) Report of all stored messages
    public static String storedMessagesReport() {
        return buildReport("STORED MESSAGES REPORT", storedMessages);
    }

    // Report of all sent messages
    public static String displayReport() {
        return buildReport("SENT MESSAGES REPORT", sentMessages);
    }

    private static String buildReport(String title, List<Message> messages) {
        StringBuilder sb = new StringBuilder("===== " + title + " =====");
        if (messages.isEmpty()) {
            return sb.append("\nNo messages to display.").toString();
        }
        for (int i = 0; i < messages.size(); i++) {
            sb.append("\n\nMessage ").append(i + 1).append("\n")
              .append(messages.get(i).getFullDetails());
        }
        sb.append("\n\nTotal messages: ").append(messages.size());
        return sb.toString();
    }

    public String getFullDetails() {
        return "Message ID: " + messageId
             + "\nMessage Hash: " + createMessageHash()
             + "\nSender: " + sender
             + "\nRecipient: " + recipient
             + "\nMessage: " + message;
    }

    // ---------- JSON storage ----------

    // Adds this message to the stored array and rewrites the JSON file
    public boolean storeMessage() {
        boolean added = false;
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
            added = true;
        }
        try {
            writeStoredMessages();
            track();
            return true;
        } catch (IOException e) {
            if (added) {
                storedMessages.remove(this);
            }
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
             + "    \"sender\": \"" + escape(sender) + "\",\n"
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

    // Reads messages.json into the stored array.
    // Returns how many messages were loaded, 0 if there is no file yet, or -1 if the file could not be read.
    public static int loadStoredMessages() {
        Path path = Path.of(storagePath);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<Map<String, String>> records = new JsonReader(json).readArray();

            storedMessages.clear();
            int highestNumber = -1;
            for (Map<String, String> record : records) {
                int number = Integer.parseInt(record.getOrDefault("messageNumber", "0").trim());
                Message m = new Message(
                        record.getOrDefault("messageId", ""),
                        number,
                        record.getOrDefault("sender", UNKNOWN_SENDER),
                        record.getOrDefault("recipient", ""),
                        record.getOrDefault("message", ""));
                storedMessages.add(m);
                m.track();
                highestNumber = Math.max(highestNumber, number);
            }
            // Carry on numbering after the messages that were already stored
            messageCounter = Math.max(messageCounter, highestNumber + 1);
            return records.size();
        } catch (IOException | RuntimeException e) {
            return -1;
        }
    }

    // A small JSON reader for the file this program writes (an array of flat objects)
    private static class JsonReader {
        private final String s;
        private int i = 0;

        JsonReader(String s) {
            this.s = s;
        }

        List<Map<String, String>> readArray() throws IOException {
            List<Map<String, String>> list = new ArrayList<>();
            skipWhitespace();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                i++;
            } else {
                while (true) {
                    list.add(readObject());
                    skipWhitespace();
                    char c = next();
                    if (c == ']') break;
                    if (c != ',') throw new IOException("Expected ',' or ']'");
                }
            }
            skipWhitespace();
            if (i < s.length()) throw new IOException("Unexpected content after the array");
            return list;
        }

        private Map<String, String> readObject() throws IOException {
            Map<String, String> map = new HashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                i++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value = (peek() == '"') ? readString() : readToken();
                map.put(key, value);
                skipWhitespace();
                char c = next();
                if (c == '}') break;
                if (c != ',') throw new IOException("Expected ',' or '}'");
            }
            return map;
        }

        private String readString() throws IOException {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') break;
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = next();
                switch (e) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw new IOException("Bad unicode escape");
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default:
                        throw new IOException("Bad escape sequence");
                }
            }
            return sb.toString();
        }

        // Numbers (and anything else that is not a string)
        private String readToken() throws IOException {
            int start = i;
            while (i < s.length() && ",}] \n\r\t".indexOf(s.charAt(i)) < 0) {
                i++;
            }
            if (start == i) throw new IOException("Missing value");
            return s.substring(start, i);
        }

        private char peek() throws IOException {
            if (i >= s.length()) throw new IOException("Unexpected end of file");
            return s.charAt(i);
        }

        private char next() throws IOException {
            char c = peek();
            i++;
            return c;
        }

        private void expect(char c) throws IOException {
            if (next() != c) throw new IOException("Expected '" + c + "'");
        }

        private void skipWhitespace() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }
    }

    // ---------- Getters / helpers ----------

    public String getMessageId()      { return messageId; }
    public int getNumMessagesSent()   { return numMessagesSent; }
    public String getSender()         { return sender; }
    public String getRecipient()      { return recipient; }
    public String getMessageText()    { return message; }

    public static List<Message> getSentMessages()        { return Collections.unmodifiableList(sentMessages); }
    public static List<Message> getDisregardedMessages() { return Collections.unmodifiableList(disregardedMessages); }
    public static List<Message> getStoredMessages()      { return Collections.unmodifiableList(storedMessages); }
    public static List<String> getMessageHashes()        { return Collections.unmodifiableList(messageHashes); }
    public static List<String> getMessageIds()           { return Collections.unmodifiableList(messageIds); }

    public static void setCurrentSender(String name) { currentSender = name; }
    public static void setStoragePath(String path)   { storagePath = path; }

    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIds.clear();
        messageCounter = 0;
        currentSender = UNKNOWN_SENDER;
    }
}
