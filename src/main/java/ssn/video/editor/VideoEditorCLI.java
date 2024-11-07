package ssn.video.editor;

import java.io.IOException;
import java.util.Scanner;

import ssn.video.filters.GaussianBlur;
import ssn.video.filters.GrayscaleFilter;
import ssn.video.features.VideoTrimmer;
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
                if (choice == 1) {
                    System.out.print("Enter start time for blur in seconds: ");
                    int startSeconds = scanner.nextInt();
                    System.out.print("Enter end time for blur in seconds: ");
                    int endSeconds = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character

                    System.out.println("Applying Gaussian Blur...");
                    filter = new GaussianBlur(27, startSeconds, endSeconds);  // Assuming GaussianBlur has a constructor with these arguments
                    filter.applyFilter(inputFileName);
                    System.out.println("Gaussian Blur applied successfully.");
                } else if (choice == 2) {
                    System.out.print("Enter start time for trimming in seconds: ");
                    int startSeconds = scanner.nextInt();
                    System.out.print("Enter end time for trimming in seconds: ");
                    int endSeconds = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character

                    System.out.print("Enter the output file name: ");
                    String outputFileName = scanner.nextLine();

                    System.out.println("Trimming video...");
                    VideoTrimmer.trimVideo(inputFileName, outputFileName, startSeconds, endSeconds);
                    System.out.println("Video trimmed successfully and saved as " + outputFileName);
                } else if (choice == 3) {
                    System.out.println("Applying Grayscale Filter...");
                    filter = new GrayscaleFilter();
                    filter.applyFilter(inputFileName);
                    System.out.println("Grayscale filter applied successfully.");
                } else {
                    System.out.println("Invalid choice, please try again.");
                }
            } catch (Exception e) {
                System.err.println("An error occurred while processing the file: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
