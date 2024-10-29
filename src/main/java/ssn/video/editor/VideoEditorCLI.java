package ssn.video.editor;

import java.io.IOException;
import java.util.Scanner;

public class VideoEditorCLI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GaussianBlurExample gaussianBlurExample = new GaussianBlurExample();

        while (true) {
            // Display menu
            System.out.println("Select an option:");
            System.out.println("1. Apply Gaussian Blur (with time range)");
            System.out.println("2. Trim Video");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            if (choice == 0) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("Enter the input file name (video): ");
            String inputFileName = scanner.nextLine();

            System.out.print("Enter the output file name: ");
            String outputFileName = scanner.nextLine();

            try {
                if (choice == 1) {
                    System.out.print("Enter start time for blur in seconds: ");
                    int startSeconds = scanner.nextInt();
                    System.out.print("Enter end time for blur in seconds: ");
                    int endSeconds = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character

                    System.out.println("Applying Gaussian Blur...");
                    gaussianBlurExample.applyBlurToVideoWithTime(inputFileName, outputFileName, startSeconds, endSeconds);
                } else if (choice == 2) {
                    System.out.print("Enter start time for trimming in seconds: ");
                    int startSeconds = scanner.nextInt();
                    System.out.print("Enter end time for trimming in seconds: ");
                    int endSeconds = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character

                    System.out.println("Trimming video...");
                    VideoTrimmer.trimVideo(inputFileName, outputFileName, startSeconds, endSeconds);
                    System.out.println("Video trimmed successfully and saved as " + outputFileName);
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
