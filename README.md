# CodeChum++
**The Fastest and Most Reliable Offline Code Judger**

CodeChum++ is a lightweight, standalone code-judging environment built with Java Swing. It provides an efficient workspace to write, run, and evaluate code instantly without an internet connection.

## The .ccpp File Format
The core of the CodeChum++ ecosystem is the `.ccpp` file. This is a custom, encrypted, and integrity-verified file format designed for secure problem sharing between teachers and students.

A `.ccpp` file acts as a collection of infinite testcase pairs (input and expected output). These files can be generated in two ways:
1. **Golden Code Execution:** Run a reference program and interact with it to automatically capture inputs and outputs.
2. **Manual Entry:** Manually define input sets and their corresponding expected outputs.

This format positions CodeChum++ as a free, open-source, and decentralized platform for offline competitive programming and classroom exercises.

## Features
* **Multi-Language Support:** Execute and judge Java, C, C++, and Python.
* **Integrated File Management:** Full file tree navigation to manage project directories.
* **Automated Judging:** Securely verify code against encrypted `.ccpp` testcases.
* **Diff Engine:** High-precision comparison between actual program output and expected results.
* **Open Source:** A transparent and community-driven alternative to online platforms.

## Dependencies & Requirements
To use the code-judging features, the following compilers and interpreters must be installed on your system and added to your Environment Path:

| Language | Required Dependency |
| :--- | :--- |
| **Java** | JDK 17 or higher |
| **C / C++** | GCC (MinGW-w64) or Clang |
| **Python** | Python 3.x |

## Application Preview

### 1. Main Editor
The landing page features a streamlined text editor designed for focus and speed.
![Main Editor](docs/imgs/main.png)

### 2. Challenge Loading
Demonstrating a `.ccpp` file imported and loaded, ready for the user to begin coding.
![Testcase Loaded](docs/imgs/main_testcase.png)

### 3. Testcase Management & Creation
The interface for editing `.ccpp` files, allowing users to generate testcases via "Golden Code" interaction or manual definitions.
![Manage Testcases](docs/imgs/manage.png)

### 4. Manual Add Dialog
A dedicated dialog for manually inputting newline-separated inputs and their expected outputs.
![Manual Add Dialog](docs/imgs/manual_add.png)

### 5. Interactive Execution
The built-in terminal allows users to run and interact with their code in real-time before submission.
![Run Code](docs/imgs/run_code.png)

### 6. Submission & Judging
The judge processes the code against the `.ccpp` testcase collection to determine correctness.
![Submit Code](docs/imgs/submit_code.png)

### 7. Output Difference (Diff)
A detailed view comparing the actual output of the user's program against the expected output of the testcase.
![Diff Tab](docs/imgs/diff.png)

## Documentation
Detailed technical specifications and usage guides are available in the [Project Wiki (TODO)](../../wiki).

## Installation
1. Download the latest release.
2. Ensure the required compilers (GCC/Python/JDK) are installed.
3. Run the application and import a `.ccpp` file to get started.

## Class Diagram
[Class Diagram](docs/imgs/class_diagram.png)