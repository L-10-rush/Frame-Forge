// // Make sure to add com.assemblyai:assemblyai-java to your dependencies

// import com.assemblyai.api.AssemblyAI;
// import com.assemblyai.api.resources.transcripts.types.*;

// public final class App {

//     public static void main(String... args) throws Exception {
//         AssemblyAI client = AssemblyAI.builder()
//                 .apiKey("e31dbba563834fdb9f20fb6f57c03cab")
//                 .build();

//         Transcript transcript = client.transcripts().transcribe("https://assembly.ai/news.mp4");

//         if (transcript.getStatus() == TranscriptStatus.ERROR) {
//             throw new Exception("Transcript failed with error: " + transcript.getError().get());
//         }

//         System.out.println("Transcript: " + transcript);
//     }
// }
