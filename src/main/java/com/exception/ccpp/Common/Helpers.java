package com.exception.ccpp.Common;

import java.nio.charset.StandardCharsets;

public class Helpers {
    public static String[] concatStringArrays(String[]... arrays) {
        // compute total length
        int total = 0;
        for (String[] arr : arrays) {
            total += arr.length;
        }

        // create result
        String[] result = new String[total];

        // copy arrays in order
        int pos = 0;
        for (String[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }

        return result;
    }

    public static String joinStringArrays(String[] a, String[] b, String elemSep, String arraySep) {
        String joinedA = String.join(elemSep, a);
        String joinedB = String.join(elemSep, b);
        return joinedA + arraySep + joinedB;
    }

    public static String stripAnsi(String input) {
        String regex = "\\x1b\\[[\\d;?]*[a-zA-Z]";
        return input.replaceAll(regex, "");
    }
    public static String stripCRLines(String input) {
        String regex = "([^\\n\\r]*\\r(?!\\n))|(\\r)";
        return input.replaceAll(regex, "");
    }
    public static String stripAnsiCRLines(String input) {
        return stripCRLines(stripAnsi(input));
    }

    public static void main(String[] args) {
        // 1. Your specific byte array
        byte[] bytes = {
            27, 91, 48, 109, 27, 91, 48, 75, 69, 110,
            116, 101, 114, 32, 97, 32, 110, 117, 109, 98,
            101, 114, 58, 27, 91, 48, 75, 27, 91, 50,
            54, 71, 49, 48, 48, 27, 91, 63, 50, 53,
            108, 13, 69, 110, 116, 101, 114, 32, 97, 32,
            110, 117, 109, 98, 101, 114, 58, 32, 32, 32,
            32, 32, 32, 32, 32, 32, 32, 49, 48, 48,
            27, 91, 48, 75, 13, 10, 69, 110, 116, 101,
            114, 32, 97, 32, 110, 117, 109, 98, 101, 114,
            32, 121, 58, 27, 91, 48, 75, 27, 91, 49,
            57, 71, 27, 91, 63, 50, 53, 104, 50, 48,
            48, 27, 91, 63, 50, 53, 108, 13, 69, 110,
            116, 101, 114, 32, 97, 32, 110, 117, 109, 98,
            101, 114, 32, 121, 58, 32, 50, 48, 48, 27,
            91, 48, 75, 13, 10, 83, 117, 109, 58, 32,
            51, 48, 48, 27, 91, 48, 75, 13, 10, 68,
            73, 68, 68, 89, 33, 33, 33, 27, 91, 48,
            75, 13, 10, 27, 91, 48, 75, 27, 91, 63,
            50, 53, 104, 10
        };

        // 2. Convert bytes to String
        String dirtyString = new String(bytes);

        String regex = "\\x1b\\[[\\d;?]*[a-zA-Z]";


        System.out.println("Dirty String:");
        for (char c : dirtyString.toCharArray()) {
            if (c == '\r') System.out.print("\\r");
            else if (c == '\n') System.out.print("\\n\n");
            else System.out.print(c);
        }
        System.out.println();
        // 4. Clean the string
        String cleanString = stripAnsi(dirtyString);
        String stripedString = stripCRLines(cleanString.trim());

        // 5. Output (using replace to visualize the \r for this demo)
        System.out.println("Cleaned Output:");
        System.out.println(cleanString.trim());

        System.out.println("\nDebug View (showing \\r):");
        for (char c : cleanString.trim().toCharArray()) {
            if (c == '\r') System.out.print("\\r");
            else if (c == '\n') System.out.print("\\n\n");
            else System.out.print(c);
        }

        System.out.println("\n\nCleaned Striped Output:");
        System.out.println(stripedString);
        System.out.println("\nDebug View (showing \\r):");
        for (char c : stripedString.toCharArray()) {
            if (c == '\r') System.out.print("\\r");
            else if (c == '\n') System.out.print("\\n\n");
            else System.out.print(c);
        }
    }


}
