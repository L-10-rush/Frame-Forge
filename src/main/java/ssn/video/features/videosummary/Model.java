package ssn.video.features.videosummary;

import ai.onnxruntime.*;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private OrtEnvironment env;
    private OrtSession session;

    public Model(String modelPath) {
        try {
            // Initialize ONNX environment and load the ONNX model
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            session = env.createSession(modelPath, options);
            System.out.println("ONNX model loaded successfully.");
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    public float[] predict(float[] inputData) {
        try {
            // Define input tensor shape (adjust as per your model's requirements)
            long[] shape = {1, 3, 224, 224};

            // Create a FloatBuffer from the input data
            FloatBuffer floatBuffer = FloatBuffer.wrap(inputData);

            // Create input tensor using the FloatBuffer
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, floatBuffer, shape);

            // Prepare input map
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input", inputTensor); // Ensure 'input' matches the name in model.py

            // Run inference
            OrtSession.Result result = session.run(inputs);
            OnnxTensor outputTensor = (OnnxTensor) result.get(0);

            // Retrieve and return the output data as a float array
            float[] outputData = (float[]) outputTensor.getValue();

            // Cleanup resources
            inputTensor.close();
            outputTensor.close();

            return outputData;
        } catch (OrtException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            session.close();
            env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}