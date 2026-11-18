public class Login {
    private String username;
    private String password;
    private String cellNumber;
    private String firstName;
    private String lastName;

    // Username: must contain an underscore and be no more than 5 characters
    public boolean checkUserName(String username) {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    // Password: at least 8 chars, 1 capital, 1 number, 1 special character
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    // Cell number: international code (+27 etc.) followed by 9-10 digits
    public boolean checkCellPhoneNumber(String cell) {
        return cell != null && cell.matches("^\\+\\d{1,3}\\d{9,10}$");
    }

    public String registerUser(String first, String last, String username,
                               String password, String cell) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted, please ensure that your username "
                 + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted, please ensure that the password "
                 + "contains at least eight characters, a capital letter, a number, "
                 + "and a special character.";
        }
        if (!checkCellPhoneNumber(cell)) {
            return "Cell phone number incorrectly formatted or does not contain "
                 + "an international code, please correct the number and try again.";
        }
        this.firstName = first;
        this.lastName = last;
        this.username = username;
        this.password = password;
        this.cellNumber = cell;
        return "Username successfully captured.\nPassword successfully captured.\n"
             + "Cell phone number successfully added.\nUser registered successfully.";
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username != null
            && username.equals(enteredUsername)
            && password.equals(enteredPassword);
    }

    // Getters used by Part 3 so that each message can record who sent it
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String returnLoginStatus(boolean loggedIn) {
        if (loggedIn) {
            return "Welcome " + firstName + " " + lastName + ", it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }
}
