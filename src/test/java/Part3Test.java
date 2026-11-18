import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Part3Test {

    private Path storageFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        Message.resetAll();
        storageFile = tempDir.resolve("messages.json");
        Message.setStoragePath(storageFile.toString());
    }

    // ---------- Test data from the brief ----------

    private Message msg1() { // Sent
        return new Message("0011111111", 0, "Test Sender", "+27834557896", "Did you get the cake?");
    }

    private Message msg2() { // Stored
        return new Message("0922222222", 1, "Test Sender", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
    }

    private Message msg3() { // Disregard
        return new Message("1033333333", 2, "Test Sender", "+27834484567", "Yohoooo, I am at your gate.");
    }

    private Message msg4() { // Sent (message ID 0838884567 entered by the developer)
        return new Message("0838884567", 3, "Test Sender", "+27831112222", "It is dinner time!");
    }

    private Message msg5() { // Stored
        return new Message("1255555555", 4, "Test Sender", "+27838884567", "Ok, I am leaving without you.");
    }

    // Fills the arrays through the same method the app uses: 1 = Sent, 2 = Disregard, 3 = Stored
    private void populateArrays() {
        msg1().sentMessage(1);
        msg2().sentMessage(3);
        msg3().sentMessage(2);
        msg4().sentMessage(1);
        msg5().sentMessage(3);
    }

    private List<String> texts(List<Message> messages) {
        return messages.stream().map(Message::getMessageText).collect(Collectors.toList());
    }

    // ---------- Sent Messages array correctly populated ----------

    @Test
    void sentMessagesArrayContainsExpectedTestData() {
        populateArrays();
        assertEquals(List.of("Did you get the cake?", "It is dinner time!"),
                texts(Message.getSentMessages()));
    }

    @Test
    void disregardedAndStoredArraysAreAlsoPopulated() {
        populateArrays();
        assertEquals(List.of("Yohoooo, I am at your gate."), texts(Message.getDisregardedMessages()));
        assertEquals(List.of("Where are you? You are late! I have asked you to be on time.",
                             "Ok, I am leaving without you."),
                texts(Message.getStoredMessages()));
    }

    @Test
    void messageIdAndMessageHashArraysArePopulated() {
        populateArrays();
        assertEquals(List.of("0011111111", "0922222222", "1033333333", "0838884567", "1255555555"),
                Message.getMessageIds());
        assertEquals(List.of("00:0:DIDCAKE", "09:1:WHERETIME", "10:2:YOHOOOOGATE",
                             "08:3:ITTIME", "12:4:OKYOU"),
                Message.getMessageHashes());
    }

    // ---------- Display the longest message ----------

    @Test
    void longestMessageOfTestMessages1To4() {
        List<Message> messages = List.of(msg1(), msg2(), msg3(), msg4());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.findLongestMessage(messages));
    }

    @Test
    void longestStoredMessage() {
        populateArrays();
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    @Test
    void longestStoredMessageWhenNothingIsStored() {
        assertEquals("No stored messages.", Message.longestStoredMessage());
    }

    // ---------- Search for message ID ----------

    @Test
    void searchForMessageId() {
        populateArrays();
        assertEquals("It is dinner time!", Message.findByMessageId("0838884567").getMessageText());
        assertEquals("Recipient: +27831112222\nMessage: It is dinner time!",
                Message.describeByMessageId("0838884567"));
    }

    @Test
    void searchForMessageIdThatDoesNotExist() {
        populateArrays();
        assertNull(Message.findByMessageId("9999999999"));
        assertEquals("Message ID not found.", Message.describeByMessageId("9999999999"));
    }

    // ---------- Search all messages sent or stored for a recipient ----------

    @Test
    void searchMessagesForRecipient() {
        populateArrays();
        assertEquals(List.of("Where are you? You are late! I have asked you to be on time.",
                             "Ok, I am leaving without you."),
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    void searchFindsSentMessagesToo() {
        populateArrays();
        assertEquals(List.of("Did you get the cake?"), Message.searchByRecipient("+27834557896"));
    }

    @Test
    void searchIgnoresDisregardedMessagesAndUnknownRecipients() {
        populateArrays();
        assertTrue(Message.searchByRecipient("+27834484567").isEmpty());   // disregarded only
        assertTrue(Message.searchByRecipient("+27000000000").isEmpty());
    }

    // ---------- Delete a message using the message hash ----------

    @Test
    void deleteMessageUsingMessageHash() throws IOException {
        populateArrays();
        String result = Message.deleteByHash("09:1:WHERETIME");

        assertEquals("Message \"Where are you? You are late! I have asked you to be on time.\" "
                   + "successfully deleted.", result);
        assertEquals(List.of("Ok, I am leaving without you."), texts(Message.getStoredMessages()));
        assertFalse(Message.getMessageIds().contains("0922222222"));
        assertFalse(Message.getMessageHashes().contains("09:1:WHERETIME"));
        assertFalse(Files.readString(storageFile).contains("0922222222"));
    }

    @Test
    void deleteWithUnknownHash() {
        populateArrays();
        assertEquals("Message hash not found.", Message.deleteByHash("99:9:NOPE"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    // ---------- Display report ----------

    @Test
    void sentMessagesReport() {
        populateArrays();
        String report = Message.displayReport();

        assertTrue(report.contains("SENT MESSAGES REPORT"));
        assertTrue(report.contains("Message Hash: 00:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message Hash: 08:3:ITTIME"));
        assertTrue(report.contains("Message: It is dinner time!"));
        assertTrue(report.contains("Total messages: 2"));
        assertFalse(report.contains("Yohoooo"));
    }

    @Test
    void storedMessagesReport() {
        populateArrays();
        String report = Message.storedMessagesReport();

        assertTrue(report.contains("STORED MESSAGES REPORT"));
        assertTrue(report.contains("Message ID: 0922222222"));
        assertTrue(report.contains("Message Hash: 09:1:WHERETIME"));
        assertTrue(report.contains("Message Hash: 12:4:OKYOU"));
        assertTrue(report.contains("Sender: Test Sender"));
        assertTrue(report.contains("Total messages: 2"));
        assertFalse(report.contains("cake"));
    }

    @Test
    void reportsWhenThereAreNoMessages() {
        assertTrue(Message.displayReport().contains("No messages to display."));
        assertTrue(Message.storedMessagesReport().contains("No messages to display."));
    }

    // ---------- Sender and recipient of stored messages ----------

    @Test
    void displaySenderAndRecipientOfStoredMessages() {
        populateArrays();
        assertEquals("Sender: Test Sender | Recipient: +27838884567\n"
                   + "Sender: Test Sender | Recipient: +27838884567",
                Message.displaySenderAndRecipient());
    }

    @Test
    void displaySenderAndRecipientWhenNothingIsStored() {
        assertEquals("No stored messages.", Message.displaySenderAndRecipient());
    }

    // ---------- Reading the JSON file into the stored array ----------

    @Test
    void storedMessagesAreReadBackFromTheJsonFile() {
        populateArrays();
        Message.resetAll();                                   // simulates closing and reopening the app
        Message.setStoragePath(storageFile.toString());

        assertEquals(2, Message.loadStoredMessages());
        assertEquals(List.of("Where are you? You are late! I have asked you to be on time.",
                             "Ok, I am leaving without you."),
                texts(Message.getStoredMessages()));
        assertEquals("Test Sender", Message.getStoredMessages().get(0).getSender());
        assertEquals(List.of("0922222222", "1255555555"), Message.getMessageIds());
        assertEquals(List.of("09:1:WHERETIME", "12:4:OKYOU"), Message.getMessageHashes());
    }

    @Test
    void messageNumbersContinueAfterLoadingStoredMessages() {
        populateArrays();
        Message.resetAll();
        Message.setStoragePath(storageFile.toString());
        Message.loadStoredMessages();

        Message next = new Message("+27718693002", "Hello there");
        assertEquals(5, next.getNumMessagesSent());           // highest stored number was 4
    }

    @Test
    void specialCharactersSurviveStoringAndLoading() {
        String tricky = "She said \"hi\" at C:\\temp\nnew line\tand a tab";
        new Message("0011223344", 0, "Test Sender", "+27718693002", tricky).sentMessage(3);
        Message.resetAll();
        Message.setStoragePath(storageFile.toString());

        assertEquals(1, Message.loadStoredMessages());
        assertEquals(tricky, Message.getStoredMessages().get(0).getMessageText());
    }

    @Test
    void loadingWhenThereIsNoFile() {
        assertEquals(0, Message.loadStoredMessages());
        assertTrue(Message.getStoredMessages().isEmpty());
    }

    @Test
    void loadingAFileThatIsNotValidJson() throws IOException {
        Files.writeString(storageFile, "this is not json");
        assertEquals(-1, Message.loadStoredMessages());
    }

    @Test
    void loadingHandWrittenJsonWithUnicodeEscapes() throws IOException {
        Files.writeString(storageFile,
                "[ {\"messageId\":\"0011223344\", \"messageNumber\":7, "
              + "\"recipient\":\"+27718693002\", \"message\":\"Caf\\u00e9 time\"} ]");

        assertEquals(1, Message.loadStoredMessages());
        Message loaded = Message.getStoredMessages().get(0);
        assertEquals("Café time", loaded.getMessageText());
        assertEquals("Unknown sender", loaded.getSender());
        assertEquals(7, loaded.getNumMessagesSent());
    }

    @Test
    void loadingAnEmptyArray() throws IOException {
        Files.writeString(storageFile, "[]");
        assertEquals(0, Message.loadStoredMessages());
    }
}
