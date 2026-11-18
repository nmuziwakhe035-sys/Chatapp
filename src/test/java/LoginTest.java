import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginTest {
    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login();
    }

    // ---- Username ----
    @Test
    void usernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    void usernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));   // no underscore, too long
        assertFalse(login.checkUserName("kyle"));          // no underscore
        assertFalse(login.checkUserName("ky_le1"));        // longer than 5
    }

    // ---- Password ----
    @Test
    void passwordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    void passwordFailsComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));    // no capital/number/special
        assertFalse(login.checkPasswordComplexity("Pass1!"));      // too short
        assertFalse(login.checkPasswordComplexity("Password1"));   // no special
        assertFalse(login.checkPasswordComplexity("password1!"));  // no capital
        assertFalse(login.checkPasswordComplexity("Password!!"));  // no number
    }

    // ---- Cell number ----
    @Test
    void cellNumberCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27831234567"));
    }

    @Test
    void cellNumberIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("0831234567"));      // no international code
        assertFalse(login.checkCellPhoneNumber("08966553"));        // no code, too short
        assertFalse(login.checkCellPhoneNumber("+27abc1234567"));   // letters
    }

    // ---- Registration messages ----
    @Test
    void registrationSucceedsWithValidDetails() {
        String msg = login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27831234567");
        assertTrue(msg.contains("User registered successfully"));
    }

    @Test
    void registrationFailsWithBadUsername() {
        String msg = login.registerUser("Kyle", "Smith", "kyle!!!!!!!", "Ch&&sec@ke99!", "+27831234567");
        assertTrue(msg.startsWith("Username is not correctly formatted"));
    }

    @Test
    void registrationFailsWithBadPassword() {
        String msg = login.registerUser("Kyle", "Smith", "kyl_1", "password", "+27831234567");
        assertTrue(msg.startsWith("Password is not correctly formatted"));
    }

    @Test
    void registrationFailsWithBadCellNumber() {
        String msg = login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "08966553");
        assertTrue(msg.startsWith("Cell phone number incorrectly formatted"));
    }

    // ---- Login ----
    @Test
    void loginSucceedsWithCorrectDetails() {
        login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27831234567");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void loginFailsWithWrongPassword() {
        login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27831234567");
        assertFalse(login.loginUser("kyl_1", "wrongPass1!"));
    }

    @Test
    void loginFailsWithWrongUsername() {
        login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27831234567");
        assertFalse(login.loginUser("abc_9", "Ch&&sec@ke99!"));
    }

    @Test
    void loginFailsBeforeAnyoneRegistered() {
        assertFalse(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void loginStatusMessages() {
        login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27831234567");
        assertEquals("Welcome Kyle Smith, it is great to see you again.",
                     login.returnLoginStatus(true));
        assertEquals("Username or password incorrect, please try again.",
                     login.returnLoginStatus(false));
    }
}
