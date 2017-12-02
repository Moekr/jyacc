package com.moekr.jyacc;

abstract class ToolKit {
    static boolean isSpace(char c){
        return '\b' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c || ' ' == c;
    }

    static boolean isSpaceLine(String line){
        return line.chars().allMatch(value -> isSpace((char)value));
    }

    static String replaceNewLine(String str){
        return str.replace("\n","");
    }
}
