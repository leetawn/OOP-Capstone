package com.exception.ccpp.Common;

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
        // Regex to remove ANSI escape codes (color/control sequences)
        return input.replaceAll("\u001B\\[[;\\d?]*[A-Za-z]", "");
    }


}
