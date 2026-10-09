package com.example.assignment3.analysis;

import java.util.regex.Pattern;

final class NumericValues {
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");

    private NumericValues() { }

    static Double finiteDecimal(String value) {
        if (!DECIMAL.matcher(value).matches()) {
            return null;
        }
        try {
            double number = Double.parseDouble(value);
            return Double.isFinite(number) ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
