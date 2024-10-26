package ssn.video.editor;

import java.io.IOException;
import java.util.Scanner;

public class VideoEditorCLI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GaussianBlurExample gaussianBlurExample = new GaussianBlurExample();

        while (true) {
            // Display menu
            System.out.println("Select a filter to apply:");
            System.out.println("1. Gaussian Blur");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            // Exit option
            if (choice == 0) {
                System.out.println("Exiting...");
                break;
            }

            // Check chosen filter
            String filterName = null;
            switch (choice) {
                case 1:
                    filterName = "Gaussian Blur";
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    continue;
            }

            // Ask for input file name
            System.out.print("Enter the input file name (image or video): ");
            String inputFileName = scanner.nextLine();

            try {
                // Apply selected filter
                if ("Gaussian Blur".equals(filterName)) {
                    System.out.println("Applying Gaussian Blur...");
                    gaussianBlurExample.GBlur(inputFileName);
                }
            } catch (IOException e) {
                System.err.println("An error occurred while processing the file: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
