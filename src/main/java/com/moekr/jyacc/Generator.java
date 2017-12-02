package com.moekr.jyacc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

class Generator {
    private final Log logger;
    private final DataSet dataSet;
    private final InputReader reader;
    private final PrintWriter writer;

    private final YaccGenerator yaccGenerator;

    Generator(String file) throws IOException {
        logger = LogFactory.getLog(this.getClass());
        dataSet = DataSet.getInstance();
        reader = new InputReader(file);
        logger.info("Input yacc define file: " + file);
        writer = new PrintWriter(file + ".java", "US-ASCII");
        logger.info("Output yacc source code file: " + file + ".java");
        yaccGenerator = new YaccGenerator(writer);
    }

    void generate() throws IOException {
        logger.info("Parsing customized code section...");
        parseCode();
        logger.info("Parsing terminal symbol section...");
        parseTerminalSymbol();
        logger.info("Parsed " + dataSet.getTerminalSymbolList().size() + " terminal symbols.");
        logger.info("Parsing nonterminal symbol section...");
        parseNonterminalSymbol();
        logger.info("Parsed " + dataSet.getNonterminalSymbolList().size() + " nonterminal symbols.");
        logger.info("Parsing grammar definition section...");
        parseGrammarDefinition();
        logger.info("Calculating FIRST of all nonterminal symbols...");
        for(String terminalSymbol : dataSet.getTerminalSymbolList()){
            dataSet.getFirstMap().put(terminalSymbol, Collections.singleton(terminalSymbol));
        }
        for(String nonterminalSymbol : dataSet.getNonterminalSymbolList()){
            calculateFirst(nonterminalSymbol);
        }
        logger.info("Generating parse table...");
        generateParseTable();
        logger.info("Generating grammar analyzer source code...");
        yaccGenerator.generate();
        logger.info("Finished, use \"javac <source file>\" to compile.");
        writer.flush();
        writer.close();
        reader.close();
    }

    private void parseCode() throws IOException {
        while (true){
            if(reader.readLine()){
                throw new GenerateException(GenerateException.UNEXPECTED_EOF);
            }
            if(dataSet.getLineBuffer().startsWith("%%")){
                return;
            }
            dataSet.getExternalCode().append(dataSet.getLineBuffer());
        }
    }

    private void parseTerminalSymbol() throws IOException {
        while (true){
            if(reader.readLine()){
                throw new GenerateException(GenerateException.UNEXPECTED_EOF);
            }
            if(dataSet.getLineBuffer().startsWith("%%")){
                return;
            }
            dataSet.getTerminalSymbolList().add(ToolKit.replaceNewLine(dataSet.getLineBuffer()));
        }
    }

    private void parseNonterminalSymbol() throws IOException {
        while (true){
            if(reader.readLine()){
                throw new GenerateException(GenerateException.UNEXPECTED_EOF);
            }
            if(dataSet.getLineBuffer().startsWith("%%")){
                return;
            }
            dataSet.getNonterminalSymbolList().add(ToolKit.replaceNewLine(dataSet.getLineBuffer()));
        }
    }

    private void parseGrammarDefinition() throws IOException {
        if(reader.readLine()){
            throw new GenerateException(GenerateException.UNEXPECTED_EOF);
        }
        while (true){
            int splitIndex = dataSet.getLineBuffer().indexOf('=');
            if(splitIndex == -1){
                throw new GenerateException(GenerateException.MALFORMED_GRAMMAR_DEFINE);
            }
            String nonterminalSymbol = dataSet.getLineBuffer().substring(0,splitIndex);
            if(!dataSet.getNonterminalSymbolList().contains(nonterminalSymbol)){
                throw new GenerateException(GenerateException.UNDEFINED_SYMBOL);
            }
            dataSet.setCharIndex(splitIndex + 1);
            List<String> symbolList = new ArrayList<>();
            for (int i = dataSet.getCharIndex() + 1; dataSet.getCharIndex() < i && i < dataSet.getLineBuffer().length(); i++) {
                String current = dataSet.getLineBuffer().substring(dataSet.getCharIndex(),i);
                if(dataSet.getTerminalSymbolList().contains(current) || dataSet.getNonterminalSymbolList().contains(current)){
                    symbolList.add(current);
                    dataSet.setCharIndex(i);
                }
            }
            if(symbolList.isEmpty()){
                throw new GenerateException(GenerateException.MALFORMED_GRAMMAR_DEFINE);
            }
            Grammar grammar = new Grammar();
            grammar.setLeft(nonterminalSymbol);
            grammar.setRight(symbolList);
            dataSet.getGrammarList().add(grammar);
            if(reader.readLine()){
                return;
            }
        }
    }

    private void calculateFirst(String symbol){
        if(dataSet.getFirstMap().containsKey(symbol)){
            return;
        }
        List<Grammar> grammarList = dataSet.getGrammarList().stream().filter(grammar -> grammar.getLeft().equals(symbol)).collect(Collectors.toList());
        Set<String> firstSet = new HashSet<>();
        for(Grammar grammar : grammarList){
            for(String rightSymbol : grammar.getRight()){
                if(rightSymbol.equals(symbol)){
                    break;
                }
                if(!dataSet.getFirstMap().containsKey(rightSymbol)){
                    calculateFirst(rightSymbol);
                }
                firstSet.addAll(dataSet.getFirstMap().get(rightSymbol));
                if(!firstSet.remove("")){
                    break;
                }
            }
        }
        dataSet.getFirstMap().put(symbol, firstSet);
    }

    private void generateParseTable(){
        List<Set<Item>> itemSetList = new ArrayList<>();
        List<Map<String, Integer>> moveMapList = new ArrayList<>();

        Item init = new Item();
        init.getRight().add(dataSet.getGrammarList().get(0).getLeft());
        init.getTerminalSymbolSet().add(null);
        itemSetList.add(closure(Collections.singleton(init)));
        moveMapList.add(new HashMap<>());
        logger.debug(itemSetList.get(0));

        List<String> symbolList = new ArrayList<>();
        symbolList.addAll(dataSet.getTerminalSymbolList());
        symbolList.addAll(dataSet.getNonterminalSymbolList());

        for (int i = 0; i < itemSetList.size(); i++) {
            for (String symbol : symbolList) {
                Set<Item> newItemSet = move(itemSetList.get(i), symbol);
                if(!newItemSet.isEmpty()){
                    int index = itemSetList.indexOf(newItemSet);
                    if(index == -1){
                        itemSetList.add(newItemSet);
                        moveMapList.add(new HashMap<>());
                        logger.debug(newItemSet);
                        moveMapList.get(i).put(symbol,itemSetList.size() - 1);
                    }else {
                        moveMapList.get(i).put(symbol,index);
                    }
                }
            }
        }
        Integer[][] parseTable = new Integer[itemSetList.size()][dataSet.getTerminalSymbolList().size() + dataSet.getNonterminalSymbolList().size() + 1];

        for(int i = 0; i < itemSetList.size(); i++){
            Set<Item> itemSet = itemSetList.get(i);
            for(Item item : itemSet){
                if(!item.getRight().isEmpty() && dataSet.getTerminalSymbolList().contains(item.getRight().get(0))){
                    String symbol = item.getRight().get(0);
                    parseTable[i][dataSet.getTerminalSymbolList().indexOf(symbol)] = moveMapList.get(i).get(symbol);
                }
                if(item.getRight().isEmpty() && item.getNonterminalSymbol() != null){
                    for(String symbol : item.getTerminalSymbolSet()){
                        int index = symbol == null ? dataSet.getTerminalSymbolList().size() : dataSet.getTerminalSymbolList().indexOf(symbol);
                        int grammarIndex = 0;
                        for(Grammar grammar : dataSet.getGrammarList()){
                            if(grammar.getLeft().equals(item.getNonterminalSymbol()) && grammar.getRight().equals(item.getLeft())){
                                break;
                            }
                            grammarIndex++;
                        }
                        parseTable[i][index] = -(grammarIndex + 1);
                    }
                }
                if(item.getNonterminalSymbol() == null && item.getRight().isEmpty()){
                    parseTable[i][dataSet.getTerminalSymbolList().size()] = 0;
                }
            }
        }
        for (int i = 0; i < moveMapList.size(); i++) {
            Map<String, Integer> moveMap = moveMapList.get(i);
            for(Map.Entry<String, Integer> entry : moveMap.entrySet()){
                if(dataSet.getNonterminalSymbolList().contains(entry.getKey())){
                    parseTable[i][dataSet.getTerminalSymbolList().size() + 1 + dataSet.getNonterminalSymbolList().indexOf(entry.getKey())] = entry.getValue();
                }
            }
        }

        dataSet.setParseTable(parseTable);
    }

    private Set<Item> closure(Set<Item> itemSet){
        List<Item> itemList = new ArrayList<>(itemSet);
        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);
            if(item.getRight().isEmpty() || !dataSet.getNonterminalSymbolList().contains(item.getRight().get(0))){
                continue;
            }
            for (Grammar grammar : dataSet.getGrammarList()) {
                if(grammar.getLeft().equals(item.getRight().get(0))){
                    Item newItem = new Item();
                    newItem.setNonterminalSymbol(grammar.getLeft());
                    newItem.getRight().addAll(grammar.getRight());
                    if(item.getRight().size() == 1){
                        newItem.getTerminalSymbolSet().addAll(item.getTerminalSymbolSet());
                    }else if(dataSet.getNonterminalSymbolList().contains(item.getRight().get(1))){
                        newItem.getTerminalSymbolSet().addAll(dataSet.getFirstMap().get(item.getRight().get(1)));
                    }else{
                        newItem.getTerminalSymbolSet().add(item.getRight().get(1));
                    }
                    if(!itemList.contains(newItem)){
                        itemList.add(newItem);
                    }
                }
            }
        }
        return new HashSet<>(itemList);
    }

    private Set<Item> move(Set<Item> itemSet, String symbol){
        Set<Item> newItemSet = new HashSet<>();
        for(Item item : itemSet){
            if(item.getRight().isEmpty() || !item.getRight().get(0).equals(symbol)){
                continue;
            }
            Item newItem = new Item();
            newItem.setNonterminalSymbol(item.getNonterminalSymbol());
            newItem.getLeft().addAll(item.getLeft());
            newItem.getLeft().add(item.getRight().get(0));
            newItem.getRight().addAll(item.getRight());
            newItem.getRight().remove(0);
            newItem.getTerminalSymbolSet().addAll(item.getTerminalSymbolSet());
            newItemSet.add(newItem);
        }
        return closure(newItemSet);
    }
}
