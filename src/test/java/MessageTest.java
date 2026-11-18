import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTest {

    private Path storageFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        Message.resetAll();
        storageFile = tempDir.resolve("messages.json");
        Message.setStoragePath(storageFile.toString());
    }

    // Test Data - Message 1 (Send)
    private Message testMessage1() {
        return new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
    }

    // Test Data - Message 2 (Discard)
    private Message testMessage2() {
        return new Message("0987654321", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
    }

    // ---------- Message length ----------

    @Test
    void messageLengthSuccess() {
        assertEquals("Message ready to send.", Message.validateLength("Hi Mike, can you join us for dinner tonight?"));
        assertEquals("Message ready to send.", Message.validateLength("a".repeat(250)));
    }

    @Test
    void messageLengthFailure() {
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.",
                Message.validateLength("a".repeat(260)));
        assertEquals("Message exceeds 250 characters by 1, please reduce the size.",
                Message.validateLength("a".repeat(251)));
    }

    // ---------- Recipient number ----------

    @Test
    void recipientNumberCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", testMessage1().checkRecipientCell());
    }

    @Test
    void recipientNumberIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain "
                   + "an international code. Please correct the number and try again.",
                testMessage2().checkRecipientCell());
    }

    // ---------- Message hash ----------

    @Test
    void messageHashCorrectForTestCase1() {
        assertEquals("00:0:HITONIGHT", testMessage1().createMessageHash());
    }

    @Test
    void remainingMessageHashesInLoop() {
        // { messageId, messageNumber, message, expectedHash }
        String[][] cases = {
            {"0012345678", "0", "Hi Mike, can you join us for dinner tonight?", "00:0:HITONIGHT"},
            {"0987654321", "1", "Hi Keegan, did you receive the payment?",      "09:1:HIPAYMENT"},
            {"0011223344", "0", "Hi, thanks",                                    "00:0:HITHANKS"},
            {"4455667788", "2", "Meeting moved to Friday.",                      "44:2:MEETINGFRIDAY"}
        };
        for (String[] c : cases) {
            Message m = new Message(c[0], Integer.parseInt(c[1]), "+27718693002", c[2]);
            assertEquals(c[3], m.createMessageHash());
        }
    }

    // ---------- Message ID ----------

    @Test
    void messageIdIsCreated() {
        Message m = new Message("+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals(10, m.getMessageId().length());
        assertTrue(m.getMessageId().matches("\\d{10}"));
        assertTrue(m.checkMessageID());
        assertEquals("Message ID generated: " + m.getMessageId(), m.messageIdCreated());
        assertTrue(m.createMessageHash().endsWith(":0:HITONIGHT"));
    }

    @Test
    void messageNumberAutoIncrements() {
        Message first = new Message("+27718693002", "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("+27718693002", "Hi Keegan, did you receive the payment?");
        assertEquals(0, first.getNumMessagesSent());
        assertEquals(1, second.getNumMessagesSent());
    }

    @Test
    void messageIdLongerThanTenCharactersFails() {
        Message m = new Message("12345678901", 0, "+27718693002", "Hello there");
        assertFalse(m.checkMessageID());
    }

    // ---------- Message sent ----------

    @Test
    void userSelectsSendMessage() {
        assertEquals("Message successfully sent.", testMessage1().sentMessage(1));
    }

    @Test
    void userSelectsDisregardMessage() {
        assertEquals("Press 0 to delete the message.", testMessage2().sentMessage(2));
    }

    @Test
    void userSelectsStoreMessage() {
        assertEquals("Message successfully stored.", testMessage1().sentMessage(3));
    }

    @Test
    void invalidOptionIsRejected() {
        assertEquals("Invalid option. Please choose 1, 2 or 3.", testMessage1().sentMessage(9));
    }

    // ---------- Totals and printing ----------

    @Test
    void totalMessagesCountsOnlySentMessages() {
        testMessage1().sentMessage(1);   // sent
        testMessage2().sentMessage(2);   // discarded
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void printMessagesShowsSentMessagesOnly() {
        testMessage1().sentMessage(1);
        testMessage2().sentMessage(2);

        String output = Message.printMessages();
        assertTrue(output.contains("Message ID: 0012345678"));
        assertTrue(output.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(output.contains("Recipient: +27718693002"));
        assertTrue(output.contains("Message: Hi Mike, can you join us for dinner tonight?"));
        assertFalse(output.contains("Keegan"));
    }

    @Test
    void printMessagesWhenNothingSent() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }

    // ---------- JSON storage ----------

    @Test
    void storedMessageIsWrittenToJsonFile() throws IOException {
        testMessage1().sentMessage(3);

        assertTrue(Files.exists(storageFile));
        String json = Files.readString(storageFile);
        assertTrue(json.contains("\"messageId\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
        assertTrue(json.contains("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    void jsonEscapesQuotes() throws IOException {
        new Message("0011223344", 0, "+27718693002", "She said \"hi\"").sentMessage(3);
        String json = Files.readString(storageFile);
        assertTrue(json.contains("She said \\\"hi\\\""));
    }

    @Test
    void multipleStoredMessagesAreAllInFile() throws IOException {
        testMessage1().sentMessage(3);
        new Message("0987654321", 1, "+27718693002", "See you soon").sentMessage(3);
        String json = Files.readString(storageFile);
        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("0987654321"));
    }
}
