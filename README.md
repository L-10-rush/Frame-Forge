<p align="center">
  <img src="./src/main/resources/Frame_Forge_logo.png" width="192px" />
</p>

<h1 style="text-align:center;">Frame Forge</h1>

Frame Forge is a light-weight video editing app developed in java. It uses the FFmpeg library to work with videos as frames, and then processes them using openCV ibrary.

---
Research about stuff and add things to better compare our Project with existing ones and why is it better.
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

images demonstrating basic filters

### Advanced Filters

images demonstrating advanced filters


## TODO

- &#x2611; Video Summarization
- &#x2611; Denoising
- &#x2610; Motion Blur
- &#x2610; Better GUI