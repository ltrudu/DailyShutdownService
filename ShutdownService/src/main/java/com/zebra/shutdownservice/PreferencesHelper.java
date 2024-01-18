package com.zebra.shutdownservice;

import android.widget.CheckBox;

import java.util.ArrayList;

public class PreferencesHelper {
    // This method takes an array of boolean as a parameter and returns a string with boolean values comma separated

    public static String checkboxArrayListToString(ArrayList<CheckBox> array, char separator) {
        // If the array is null or empty, return an empty string
        if (array == null || array.size() == 0) {
            return "";
        }
        // Use a StringBuilder to append the boolean values and commas
        StringBuilder sb = new StringBuilder();
        // Loop through the array
        for (int i = 0; i < array.size(); i++) {
            // Append the boolean value
            sb.append(array.get(i).isChecked());
            // If it is not the last element, append a comma and a space
            if (i < array.size() - 1) {
                sb.append(separator);
            }
        }
        // Return the string representation of the StringBuilder
        return sb.toString();
    }

    public static void applyBooleanArrayToCheckBoxes(boolean[] array, ArrayList<CheckBox> checkBoxes) {
        for (int i = 0; i < array.length; i++) {
            checkBoxes.get(i).setChecked(array[i]);
        }
    }

    public static boolean[] getBooleanArrayFromCheckBoxes(ArrayList<CheckBox> checkBoxes)
    {
        boolean[] array = new boolean[checkBoxes.size()];
        for(int i = 0; i < array.length; i++)
        {
            array[i] = checkBoxes.get(i).isChecked();
        }
        return array;
    }

    public static String booleanArrayToString(boolean[] array, char separator) {
        // If the array is null or empty, return an empty string
        if (array == null || array.length == 0) {
            return "";
        }
        // Use a StringBuilder to append the boolean values and commas
        StringBuilder sb = new StringBuilder();
        // Loop through the array
        for (int i = 0; i < array.length; i++) {
            // Append the boolean value
            sb.append(array[i]);
            // If it is not the last element, append a comma and a space
            if (i < array.length - 1) {
                sb.append(separator);
            }
        }
        // Return the string representation of the StringBuilder
        return sb.toString();
    }


    // This method takes a string of boolean values comma separated as a parameter and returns an array of boolean
    public static boolean[] stringToBooleanArray(String str, char separator) {
        // If the string is null or empty, return an empty array
        if (str == null || str.isEmpty()) {
            return new boolean[0];
        }
        // Split the string by comma and trim the spaces
        String[] parts = str.split(Character.toString(separator));
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        // Create a boolean array with the same length as the parts array
        boolean[] array = new boolean[parts.length];
        // Loop through the parts array and parse each part as a boolean value
        for (int i = 0; i < parts.length; i++) {
            // Use the Boolean.parseBoolean method to convert the string to a boolean value
            // This method returns true for any string that equals "true" ignoring case, and false for any other string
            // For example, "True", "TRUE", and "true" will return true, while "False", "FALSE", "false", or any other string will return false
            // You can read more about this method here: [Boolean.parseBoolean](https://www.geeksforgeeks.org/boolean-tostring-method-in-java-with-examples/)
            array[i] = Boolean.parseBoolean(parts[i]);
        }
        // Return the boolean array
        return array;
    }

}
