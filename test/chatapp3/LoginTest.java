package chatapp3;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginTest {

    // CheckUserName()

    @Test
    public void testUserName_correctlyFormatted() {
        // "kyl_1" has an underscore and is exactly 5 characters -> valid
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void testUserName_incorrectlyFormatted() {
        // No underscore at all -> invalid
        Login login = new Login("kyle!!!!!!", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertFalse(login.checkUserName("kyle!!!!!!"));
    }

    // CheckPasswordComplexity()

    @Test
    public void testPassword_meetsComplexity() {
        // Has a capital, a number, and special characters, and is long enough
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void testPassword_doesNotMeetComplexity() {
        // All lowercase, no number, no special character -> invalid
        Login login = new Login("kyl_1", "password", "+27838968976", "Kyle", "Test");
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // CheckCellPhoneNumber()

    @Test
    public void testCellPhone_correctlyFormatted() {
        // Starts with +27, followed by digits, within the length limit
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void testCellPhone_incorrectlyFormatted() {
        // No international code -> invalid
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "08966553", "Kyle", "Test");
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // RegisterUser() 

    @Test
    public void testRegisterUser_usernameIncorrect() {
        // Username check fails first, so we expect the username error message
        Login login = new Login("kyle!!!!!!", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertEquals("Username is not correctly formatted; please ensure that your "
                + "username contains an underscore and is no more than five "
                + "characters in length.", login.registerUser());
    }

    @Test
    public void testRegisterUser_passwordIncorrect() {
        // Username is valid but password fails, so we expect the password error message
        Login login = new Login("kyl_1", "password", "+27838968976", "Kyle", "Test");
        assertEquals("Password is not correctly formatted; please ensure that the "
                + "password contains at least eight characters, a capital "
                + "letter, a number, and a special character.", login.registerUser());
    }

    @Test
    public void testRegisterUser_success() {
        // Both username and password are valid, so registration should succeed
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertEquals("Username successfully captured. Password successfully captured. "
                + "You have been registered successfully.", login.registerUser());
    }

    // loginUser() / returnLoginStatus() ---

    @Test
    public void testLogin_successful() {
        // Entered details match what was registered -> true
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void testLogin_failed() {
        // Wrong password entered -> false
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertFalse(login.loginUser("kyl_1", "wrongPassword"));
    }

    @Test
    public void testReturnLoginStatus_success() {
        // Successful login should return the personalised welcome message
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertEquals("Welcome Kyle, Test it is great to see you again.",
                login.returnLoginStatus(true));
    }

    @Test
    public void testReturnLoginStatus_failure() {
        // Failed login should return the generic error message
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Test");
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus(false));
    }
}