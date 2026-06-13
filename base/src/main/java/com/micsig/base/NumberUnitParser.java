package com.micsig.base;

public class NumberUnitParser {
    private String number;
    private String unit;

    public NumberUnitParser(String input) {
        parseInput(input);
    }

    private void parseInput(String input) {
        String cleanInput = input.replaceAll("\\s+", "");

        // 找到第一个非数字字符的位置（考虑小数点）
        int unitStartIndex = 0;
        boolean foundDecimal = false;
        boolean bDigit = false;
        boolean isNegative = false;

        if (cleanInput.startsWith("-")) {
            cleanInput = cleanInput.replace("-", "");
            isNegative = true;
        }

        for (int i = 0; i < cleanInput.length(); i++) {
            char c = cleanInput.charAt(i);
            if (Character.isDigit(c)) {
                bDigit = true;
                continue;
            } else if (c == '.' && !foundDecimal) {
                foundDecimal = true;
                continue;
            } else {
                unitStartIndex = i;
                break;
            }
        }

        if (unitStartIndex > 0) {
            this.number = cleanInput.substring(0, unitStartIndex);
            this.unit = cleanInput.substring(unitStartIndex,unitStartIndex+1);
        } else {
            if(!bDigit){
                cleanInput = "0";
            }
            this.number = cleanInput;
            this.unit = "";
        }
        if (isNegative) {
            this.number = "-" + this.number;
        }
    }

    public String getNumber() {
        return number;
    }

    public String getUnit() {
        return unit;
    }

    public double getNumberAsDouble() {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

}