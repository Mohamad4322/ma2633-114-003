package Project;
// TextFX.java - Utility class for text effects (optional enhancement for user experience)
public class TextFX {

    // Method to add color to text (for console output)
    public static String colorText(String text, String colorCode) {
        return colorCode + text + "\u001B[0m";
    }

    // Predefined color codes
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Method to print a success message
    public static void printSuccess(String message) {
        System.out.println(colorText(message, GREEN));
    }

    // Method to print an error message
    public static void printError(String message) {
        System.out.println(colorText(message, RED));
    }

    // Method to print an informational message
    public static void printInfo(String message) {
        System.out.println(colorText(message, CYAN));
    }
}

