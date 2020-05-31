package com.example.wallet;

public class FrequencyStringConverter {

    public static int convertFrequencyStringToInt(String frequencyString) {

        if (frequencyString.equals("Monthly")) {
            return 12;
        } else if (frequencyString.equals("Quarterly")) {
            return 4;
        } else if (frequencyString.equals("Biannually")){
            return 2;
        } else {
            // annually
            return 1;
        }
    }

    public static String convertFrequencyIntToString(int frequency) {

        if (frequency == 12) {
            return "Monthly";
        } else if (frequency == 4) {
            return "Quarterly";
        } else if (frequency == 2){
            return "Biannually";
        } else {
            // annually
            return "Annually";
        }
    }
}
