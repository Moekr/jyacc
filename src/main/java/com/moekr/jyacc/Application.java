package com.moekr.jyacc;

import java.io.IOException;

public abstract class Application {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.err.println("Usage: java -jar JYacc.jar <filename>");
        }else {
            new Generator(args[0]).generate();
        }
    }
}
