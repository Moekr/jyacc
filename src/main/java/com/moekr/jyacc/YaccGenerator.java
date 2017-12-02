package com.moekr.jyacc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;

class YaccGenerator {
    private Log logger;
    private DataSet dataSet;
    private PrintWriter writer;

    YaccGenerator(PrintWriter writer){
        logger = LogFactory.getLog(this.getClass());
        dataSet = DataSet.getInstance();
        this.writer = writer;
    }

    void generate(){
        writer.println("import java.io.*;");
        writer.println("import java.util.*;");
        writer.println("import java.util.function.Supplier;");
        writer.println();
        writer.println(dataSet.getExternalCode().toString());
        writer.println("interface Token{");
        writer.println("\tString getText();");
        writer.println("}");
        writer.println();
        writer.println("class Yacc{");
        logger.info("Generating variable section...");
        generateVariable();
        logger.info("Generating constructor section...");
        generateConstructor();
        logger.info("Generating data section...");
        generateData();
        logger.info("Generating logic section...");
        generateLogic();
        writer.println("}");
    }

    private void generateVariable(){
        writer.println("\tprivate Supplier<Token> tokenSupplier;");
        writer.println("\tprivate Stack<Integer> stateStack;");
        writer.println("\tprivate Stack<String> symbolStack;");
        writer.println();
    }

    private void generateConstructor(){
        writer.println("\tYacc() throws IOException {");
        writer.println("\t\tthis(() -> {");
        writer.println("\t\t\ttry {");
        writer.println("\t\t\t\tint nextByte = System.in.read();");
        writer.println("\t\t\t\tif(nextByte >= 0){");
        writer.println("\t\t\t\t\tString text = String.valueOf((char)nextByte);");
        writer.println("\t\t\t\t\treturn () -> text;");
        writer.println("\t\t\t\t}else {");
        writer.println("\t\t\t\t\treturn () -> null;");
        writer.println("\t\t\t\t}");
        writer.println("\t\t\t} catch (IOException e) {");
        writer.println("\t\t\t\tSystem.err.println(\"Built-in token reader error: \" + e.getMessage() + \", exit!\");");
        writer.println("\t\t\t\tSystem.exit(1);");
        writer.println("\t\t\t\treturn null;");
        writer.println("\t\t\t}");
        writer.println("\t\t});");
        writer.println("\t}");
        writer.println();
        writer.println("\tYacc(Supplier<Token> tokenSupplier) throws IOException {");
        writer.println("\t\tObjects.requireNonNull(tokenSupplier);");
        writer.println("\t\tthis.tokenSupplier = tokenSupplier;");
        writer.println("\t\tstateStack = new Stack<>();");
        writer.println("\t\tsymbolStack = new Stack<>();");
        writer.println("\t}");
        writer.println();
    }

    private void generateData(){
        writer.println("\tprivate List<String> terminalList = Arrays.asList(");
        for (String terminalSymbol : dataSet.getTerminalSymbolList()){
            writer.println("\t\t\"" + terminalSymbol + "\",");
        }
        writer.println("\t\tnull");
        writer.println("\t);");
        writer.println();
        writer.println("\tprivate List<String> nonterminalList = Arrays.asList(");
        for (int i = 0; i < dataSet.getNonterminalSymbolList().size(); i++) {
            writer.print("\t\t\"" + dataSet.getNonterminalSymbolList().get(i) + "\"");
            if(i < dataSet.getNonterminalSymbolList().size() - 1){
                writer.println(",");
            }else {
                writer.println();
            }
        }
        writer.println("\t);");
        writer.println();
        writer.println("\tprivate int[][] grammar = new int[][]{");
        for (int i = 0; i < dataSet.getGrammarList().size(); i++) {
            Grammar grammar = dataSet.getGrammarList().get(i);
            writer.print("\t\tnew int[]{");
            writer.print(dataSet.getNonterminalSymbolList().indexOf(grammar.getLeft()) + ",");
            writer.print(grammar.getRight().size() + "}");
            if(i < dataSet.getGrammarList().size() - 1){
                writer.println(",");
            }else {
                writer.println();
            }
        }
        writer.println("\t};");
        writer.println();
        writer.println("\tprivate Integer[][] parseTable = new Integer[][]{");
        for(int i = 0; i < dataSet.getParseTable().length; i++){
            Integer[] row = dataSet.getParseTable()[i];
            writer.print("\t\tnew Integer[]{");
            for(int j = 0; j < row.length; j++){
                writer.print(row[j]);
                if(j < row.length - 1){
                    writer.print(",");
                }else {
                    writer.print("}");
                }
            }
            if(i < dataSet.getParseTable().length - 1){
                writer.println(",");
            }else {
                writer.println();
            }
        }
        writer.println("\t};");
        writer.println();
    }

    private void generateLogic(){
        writer.println("\tvoid parse() throws IOException {");
        writer.println("\t\tStack<String> reduceStack = new Stack<>();");
        writer.println("\t\tstateStack.push(0);");
        writer.println("\t\tToken token = tokenSupplier.get();");
        writer.println("\t\twhile (token != null){");
        writer.println("\t\t\tint tokenIndex = terminalList.indexOf(token.getText());");
        writer.println("\t\t\tif(tokenIndex == -1){");
        writer.println("\t\t\t\tthrow new RuntimeException(\"Unmatched token.\");");
        writer.println("\t\t\t}");
        writer.println("\t\t\tInteger action = parseTable[stateStack.peek()][tokenIndex];");
        writer.println("\t\t\tif(action == null){");
        writer.println("\t\t\t\tthrow new RuntimeException(\"Action error.\");");
        writer.println("\t\t\t}else if(action > 0){");
        writer.println("\t\t\t\tstateStack.push(action);");
        writer.println("\t\t\t\tsymbolStack.push(token.getText());");
        writer.println("\t\t\t\ttoken = tokenSupplier.get();");
        writer.println("\t\t\t}else if(action < 0){");
        writer.println("\t\t\t\tSystem.out.println(\"=====================================\");");
        writer.println("\t\t\t\tSystem.out.println(\"Symbol stack before:\\t\" + symbolStack);");
        writer.println("\t\t\t\tfor (int i = 0; i < grammar[Math.abs(action) - 1][1]; i++) {");
        writer.println("\t\t\t\t\tstateStack.pop();");
        writer.println("\t\t\t\t\treduceStack.push(symbolStack.pop());");
        writer.println("\t\t\t\t}");
        writer.println("\t\t\t\tstateStack.push(parseTable[stateStack.peek()][terminalList.size() + grammar[Math.abs(action) - 1][0]]);");
        writer.println("\t\t\t\tsymbolStack.push(nonterminalList.get(grammar[Math.abs(action) - 1][0]));");
        writer.println("\t\t\t\tSystem.out.print(\"Reduce production:\\t\" + nonterminalList.get(grammar[Math.abs(action) - 1][0]) + \" = \");");
        writer.println("\t\t\t\twhile(!reduceStack.empty()){");
        writer.println("\t\t\t\t\tSystem.out.print(reduceStack.pop());");
        writer.println("\t\t\t\t}");
        writer.println("\t\t\t\tSystem.out.println();");
        writer.println("\t\t\t\tSystem.out.println(\"Symbol stack after:\\t\" + symbolStack);");
        writer.println("\t\t\t\tSystem.out.println(\"=====================================\");");
        writer.println("\t\t\t}else{");
        writer.println("\t\t\t\tSystem.out.println(\"=====================================\");");
        writer.println("\t\t\t\tSystem.out.println(\"Accepted!\");");
        writer.println("\t\t\t\tSystem.out.println(\"=====================================\");");
        writer.println("\t\t\t\treturn;");
        writer.println("\t\t\t}");
        writer.println("\t\t}");
        writer.println("\t}");
    }
}
