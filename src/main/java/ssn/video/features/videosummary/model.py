import torch
import torchvision.models as models

# Load your PyTorch model
model = torch.load('model/best_model_epoch60.pth.tar', map_location=torch.device('cpu'))
model.eval()  # Set the model to evaluation mode

# Create a dummy input tensor with the shape your model expects
dummy_input = torch.randn(1, 3, 224, 224)  # Adjust the shape as necessary for your model

# Export the model to ONNX format
torch.onnx.export(
    model, 
    dummy_input, 
    "model/best_model.onnx", 
    export_params=True, 
    opset_version=10, 
    do_constant_folding=True, 
    input_names=['input'], 
    output_names=['output']
)

print("Model exported to ONNX format as 'model/best_model.onnx'")
