// package ssn.video.features;


// import com.google.cloud.vertexai.VertexAI;
// import com.google.cloud.vertexai.api.GenerateContentResponse;
// import com.google.cloud.vertexai.generativeai.ContentMaker;
// import com.google.cloud.vertexai.generativeai.GenerativeModel;
// import com.google.cloud.vertexai.generativeai.PartMaker;
// import com.google.cloud.vertexai.generativeai.ResponseHandler;
// import java.io.IOException;

// public class VideoInputWithAudio {

//   public static void main(String[] args) throws IOException {
//     // TODO(developer): Replace these variables before running the sample.
//     String projectId = "your-google-cloud-project-id";
//     String location = "us-central1";
//     String modelName = "gemini-1.5-flash-001";

//     videoAudioInput(projectId, location, modelName);
//   }

//   // Analyzes the given video input, including its audio track.
//   public static String videoAudioInput(String projectId, String location, String modelName)
//       throws IOException {
//     // Initialize client that will be used to send requests. This client only needs
//     // to be created once, and can be reused for multiple requests.
//     try (VertexAI vertexAI = new VertexAI(projectId, location)) {
//       String videoUri = "gs://cloud-samples-data/generative-ai/video/pixel8.mp4";

//       GenerativeModel model = new GenerativeModel(modelName, vertexAI);
//       GenerateContentResponse response = model.generateContent(
//           ContentMaker.fromMultiModalData(
//               "Provide a description of the video.\n The description should also "
//                   + "contain anything important which people say in the video.",
//               PartMaker.fromMimeTypeAndData("video/mp4", videoUri)
//           ));

//       String output = ResponseHandler.getText(response);
//       System.out.println(output);

//       return output;
//     }
//   }
