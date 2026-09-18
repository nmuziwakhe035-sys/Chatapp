package chatapp3;
import java.util.regex.Pattern;

class Login{

    //Collects the users Information
    private final String username;
    private final String password;
    private final String cell;
    private final String firstName;
    private final String lastName;

 
    private static final Pattern CELL_PATTERN = Pattern.compile("^\\+27\\d{1,10}$");

    // Captures users information when they register
    Login(String username, String password, String cell, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.cell = cell;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    //Verify the username
    boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

   //Verify the Password
    boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasNumber = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();
        return hasCapital && hasNumber && hasSpecial;
    }

    //Verify the cellphonenumber
    boolean checkCellPhoneNumber(String cell) {
        if (cell == null) {
            return false;
        }
        return CELL_PATTERN.matcher(cell).matches();
    }

    //Register the user
    String registerUser() {
        if (!checkUserName(this.username)) {
            return "Username is not correctly formatted; please ensure that your "
                    + "username contains an underscore and is no more than five "
                    + "characters in length.";
        }
        if (!checkPasswordComplexity(this.password)) {
            return "Password is not correctly formatted; please ensure that the "
                    + "password contains at least eight characters, a capital "
                    + "letter, a number, and a special character.";
        }
        return "Username successfully captured. Password successfully captured. "
                + "You have been registered successfully.";
    }

    //Checks whether the given username and password match the creditials
    boolean loginUser(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    // Login status
    String returnLoginStatus(boolean success) {
        if (success) {
            return "Welcome " + this.firstName + ", " + this.lastName
                    + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    // Getters-Return the value of the private field
    String getUsername() { return username; }
    String getCell() { return cell; }
    String getFirstName() { return firstName; }
    String getLastName() { return lastName; }
}