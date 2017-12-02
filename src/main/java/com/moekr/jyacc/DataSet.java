package com.moekr.jyacc;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Data
class DataSet {

    private StringBuilder externalCode = new StringBuilder();
    private List<String> terminalSymbolList = new ArrayList<>();
    private List<String> nonterminalSymbolList = new ArrayList<>();
    private List<Grammar> grammarList = new ArrayList<>();
    private HashMap<String, Set<String>> firstMap = new HashMap<>();

    private Integer[][] parseTable;

    private boolean reachEOF;
    private String lineBuffer;
    private int lineIndex;
    private int charIndex;

    private DataSet(){}
    private static DataSet instance;
    static DataSet getInstance(){
        if(instance == null){
            instance = new DataSet();
        }
        return instance;
    }
}
