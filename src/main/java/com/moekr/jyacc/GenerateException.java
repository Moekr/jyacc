package com.moekr.jyacc;

import java.util.HashMap;
import java.util.Map;

class GenerateException extends RuntimeException {
    static final int UNEXPECTED_EOF = 0;
    static final int MALFORMED_GRAMMAR_DEFINE = 1;
    static final int UNDEFINED_SYMBOL = 2;

    private static final Map<Integer, String> MESSAGE = new HashMap<>();
    static{
        MESSAGE.put(UNEXPECTED_EOF, "Unexpected EOF found.");
        MESSAGE.put(MALFORMED_GRAMMAR_DEFINE, "Malformed grammar define.");
        MESSAGE.put(UNDEFINED_SYMBOL, "Undefined symbol.");
    }

    GenerateException(int code){
        super("Line " + DataSet.getInstance().getLineIndex() + ", index " + DataSet.getInstance().getCharIndex() + ": " + MESSAGE.getOrDefault(code, "Unknown error."));
        System.err.print(DataSet.getInstance().getLineBuffer());
        for(int i = 0;i < DataSet.getInstance().getCharIndex();i++){
            System.err.print(' ');
        }
        System.err.println("^");
    }
}
