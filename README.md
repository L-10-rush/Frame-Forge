<p align="center">
  <img src="./src/main/resources/Frame_Forge_logo.png" width="192px" />
</p>

<h1 style="text-align:center;">Frame Forge</h1>

Frame Forge is a light-weight video editing app developed in java. It uses the FFmpeg library to work with videos as frames, and then processes them using openCV ibrary.

---

Below are some comparisions that we made with the existing video editing applications

| Feature                     | **Frame Forge**          | **Clipchamp**         | **OpenShot**        |
|-----------------------------|---------------------------|------------------------|----------------------|
| **Startup Time**            | 4.38 seconds              | 29.2 seconds          | 13 seconds          |
| **Open Source**             | ✅                        | ❌                    | ✅                  |
| **Unique Filters**          | Periodic Noise Removal    | None                  | None                |
| **Basic Filters**           | Fade In, Fade Out, Gaussian Blur, BnW | Fade, Blur, Saturate and more | Fade, Crop, Color Adjustment and more|
| **Resource Efficiency**     | Frame-by-frame processing, low buffer storage usage | High memory consumption | Moderate CPU and memory usage |
| **Video Summarization**     | GPT-based API integration | ❌                    | ❌                  |
| **User Interface**          | Simple, minimalistic      | Feature-rich          | Feature-rich        |

---

- [Installation](#installation)
    - [1. Installing Maven](#installing-maven)
    - [2. Cloning this repo](#cloning-this-repo)
    - [3. Try it out](#try-it-out)
- [Examples](#examples)
    - [Video Summarization](#video-summarization)
    - [Basic Filters](#basic-filters)
    - [Advanced Filters](#advanced-filters)
- [TODO](#todo)

## Installation

We are using the Maven Project Manager for our project

### Installing Maven

Firstly, [install Maven](https://maven.apache.org/install.html) and follow the steps given there.

Check if it has been installed with `mvn -v`

### Cloning this repo

To clone this repo-
```bash
git clone https://github.com/L-10-rush/Frame-Forge
cd Frame-Forge
```

Alternatively, you can download the source code as a zip file from the github page and unzip it.
Here's a link to it: [Download zip](https://github.com/L-10-rush/Frame-Forge/archive/refs/heads/main.zip)

### Try it out

Run these maven commands to compile it.
```shell
mvn clean compile
```

Once all the dependencies are downloaded and the source codes compiled, you can execute the application by:
```shell
mvn exec:java
```

## Examples

### Video Summarization

images demonstrating video summarization  

### Basic Filters

Applying a basic Black and White Filter
Before-<br>
![image1](src/main/resources/image.png)


After-<br>
![image2](src/main/resources/image2.png)


### Advanced Filters

As of now only 1 Advanced filter is available. That is a periodic noise removal filter.
Applying it onto a frame like the one below<br>
![noisy image](src/main/resources/period_input.jpg)


we get a much smoother frame<br>
![smoother image](src/main/resources/denoised_result.jpg)



## TODO

- &#x2611; Video Summarization
- &#x2611; Denoising
- &#x2610; Motion Blur
- &#x2610; Better GUI