package CCJudge;

import java.nio.file.Paths;
import java.util.Arrays;

// idk gpt said suggested this shit might as well use it
record SubmissionFile(String filename, String content) {}

public class ExecutionConfig {

    // returns test files (not for compiling)
    public static SubmissionFile[] getTestFiles(String language) {
        return switch (language) {
            case "java" -> new SubmissionFile[]{
                    new SubmissionFile("Main.java",
                            """
                            import java.util.Scanner;
                            public class Main {
                                public static void main(String[] args) {
                                    Scanner sc = new Scanner(System.in);
                                    System.out.print("Name 1: ");
                                    String name1 = sc.nextLine();
                                    System.out.println("Java Main got: " + name1);
        
                                    SomeClass helper = new SomeClass();
                                    System.out.print("Name 2: ");
                                    String name2 = sc.nextLine();
                                    helper.printName(name2);
                                }
                            }
                            """),
                    new SubmissionFile("SomeClass.java",
                            """
                            public class SomeClass {
                                public void printName(String name) {
                                    System.out.println("Java Helper got: " + name);
                                }
                            }
                            """)
            };
            case "cpp" -> new SubmissionFile[]{
                    new SubmissionFile("main.cpp",
                            """
                            #include <iostream>
                            #include <string>
                            #include "helper.h"
                            using namespace std;
                            int main() {
                                string name1, name2;
                                cout << "Name 1: " << flush;
                                getline(cin, name1);
                                cout << "C++ Main got: " << name1 << endl;
        
                                cout << "Name 2: " << flush;
                                getline(cin, name2);
                                print_name(name2);
                                return 0;
                            }
                            """),
                    new SubmissionFile("helper.cpp",
                            """
                            #include <iostream>
                            #include "helper.h"
                            void print_name(const std::string& name) {
                                std::cout << "C++ Helper got: " << name << std::endl;
                            }
                            """),
                    new SubmissionFile("helper.h",
                            """
                            #include <string>
                            void print_name(const std::string& name);
                            """)
            };
            case "c" ->
                new SubmissionFile[]{
                    new SubmissionFile("main.c",
                            """
                            #include <stdio.h>
                            #include "helper.h"
                            int main() {
                                char name1[255], name2[255];
                                printf("Enter name1: ");
                                scanf(" %s", name1);
                                printf("Main C name: %s\\n", name1);
                                printf("Enter name2: ");
                                scanf(" %s", name2);
        
                                print_name(name2);
                                return 0;
                            }
                            """),
                    new SubmissionFile("helper.c",
                            """
                            #include <stdio.h>
                            #include "helper.h"
                            void print_name(char *name) {
                                printf("Helper C name: %s\\n", name);
                            }
                            """),
                    new SubmissionFile("helper.h",
                            """
                            void print_name(char *name);
                            """)
                };
            case "python" -> new SubmissionFile[]{
                    new SubmissionFile("main.py",
                            """
                            import sys
                            from helper import print_name
                            
                            sys.stdout.write("Name 1: ")
                            sys.stdout.flush()
                            name1 = sys.stdin.readline().strip()
                            print("Python Main got:", name1)
        
                            sys.stdout.write("Name 2: ")
                            sys.stdout.flush()
                            name2 = sys.stdin.readline().strip()
                            print_name(name2)
                            """),
                    new SubmissionFile("helper.py",
                            """
                            def print_name(name):
                                print("Python Helper got:", name)
                            """)
            };
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    // return compile command for our terminal, shits ass but its aihgt
    public static String[] getCompileCommand(String language, SubmissionFile[] files) {
        String[] sourceFilenames = Arrays.stream(files).map(SubmissionFile::filename).toArray(String[]::new);

        String[] command;
        switch (language) {
            case "java":
                command = new String[1 + sourceFilenames.length];
                command[0] = "javac";
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                return command;

            case "cpp":
                String[] cppSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".cpp"))
                        .toArray(String[]::new);

                command = new String[3 + cppSourceFiles.length];
                command[0] = "g++";
                System.arraycopy(cppSourceFiles, 0, command, 1, cppSourceFiles.length);
                command[1 + cppSourceFiles.length] = "-o";
                command[2 + cppSourceFiles.length] = "Submission";
                return command;
            case "c":
                String[] cSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".c"))
                        .toArray(String[]::new);

                command = new String[3 + cSourceFiles.length];
                command[0] = "gcc";
                System.arraycopy(cSourceFiles, 0, command, 1, cSourceFiles.length);
                command[1 + cSourceFiles.length] = "-o";
                command[2 + cSourceFiles.length] = "Submission";
                return command;

            case "python":
                return null;

            default:
                return null;
        }
    }

    public static String[] getExecuteCommand(String language) {
        return switch (language) {
            case "java" -> new String[]{"java", "Main"}; // idk if Main is in all program
            case "cpp", "c" -> new String[]{Paths.get(".").toAbsolutePath().normalize().toString() + "/Submission"};
            case "python" -> new String[]{"python3", "main.py"};
            default -> throw new IllegalArgumentException("Unsupported language.");
        };
    }
}