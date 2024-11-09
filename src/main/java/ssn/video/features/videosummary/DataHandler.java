package ssn.video.features.videosummary;

import java.io.File;

public class DataHandler {
    public void prepareDataset(String datasetPath) {
        File datasetDir = new File(datasetPath);
        if (!datasetDir.exists()) {
            datasetDir.mkdirs();
        }

        // Here you would implement the logic to convert videos to the required format
        System.out.println("Dataset prepared at: " + datasetPath);
    }
}