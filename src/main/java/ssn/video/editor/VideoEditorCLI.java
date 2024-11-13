package ssn.video.editor;

import java.io.IOException;
import java.util.Scanner;

import ssn.video.filters.GaussianBlur;
import ssn.video.features.VideoTrimmer;
import ssn.video.filters.DenoisingFilter;
import ssn.video.filters.FadeFilter;
import ssn.video.filters.Filter;

public class VideoEditorCLI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Filter filter;

        while (true) {
            // Display menu
            System.out.println("Select an option:");
            System.out.println("1. Apply Gaussian Blur");
            System.out.println("2. Trim Video");
            System.out.println("3. Apply Grayscale");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            if (choice == 0) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("Enter the input file name: ");
            String inputFileName = scanner.nextLine();

            try {
                switch (choice) {
                    case 1 -> {
                            System.out.print("Enter start time for blur in seconds: ");
                            int startSeconds = scanner.nextInt();
                            System.out.print("Enter end time for blur in seconds: ");
                            int endSeconds = scanner.nextInt();
                            scanner.nextLine(); // Consume newline character
                            System.out.println("Applying Gaussian Blur...");
                            filter = new GaussianBlur(27, startSeconds, endSeconds);  // Assuming GaussianBlur has a constructor with these arguments
                            filter.applyFilter(inputFileName);
                            System.out.println("Gaussian Blur applied successfully.");
                        }
                    case 2 -> {
                            System.out.print("Enter start time for trimming in seconds: ");
                            int startSeconds = scanner.nextInt();
                            System.out.print("Enter end time for trimming in seconds: ");
                            int endSeconds = scanner.nextInt();
                            System.out.println("Trimming video...");
                            VideoTrimmer.trimVideo(inputFileName, startSeconds, endSeconds);
                        }
                    case 3 -> {
                        System.out.println("Applying Brightness Filter...");
                        // filter = new DenoisingFilter(7, 21);
                        // filter.applyFilter(inputFileName);
                        System.out.println("Brigtness filter applied successfully.");
                    }
                    case 4 -> {
                        System.out.println("Applying Fade Filter...");
                        filter = new FadeFilter(0, 3, "in");
                        filter.applyFilter(inputFileName);
                    }
                    default -> System.out.println("Invalid choice, please try again.");
                }
            } catch (Exception e) {
                System.err.println("An error occurred while processing the file: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
