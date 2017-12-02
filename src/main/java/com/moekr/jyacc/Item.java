package com.moekr.jyacc;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
class Item {
    private String nonterminalSymbol;
    private List<String> left = new ArrayList<>();
    private List<String> right = new ArrayList<>();
    private Set<String> terminalSymbolSet = new HashSet<>();

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Item)) {
            return false;
        }
        Item another = (Item) object;
        return StringUtils.equals(this.nonterminalSymbol, another.nonterminalSymbol)
                && left.equals(another.left)
                && right.equals(another.right)
                && terminalSymbolSet.equals(another.terminalSymbolSet);
    }

    @Override
    public int hashCode(){
        int hash = nonterminalSymbol == null ? 0 : nonterminalSymbol.hashCode();
        hash = (hash >> 8) + left.hashCode();
        hash = (hash >> 8) + right.hashCode();
        hash = (hash >> 8) + terminalSymbolSet.hashCode();
        return hash;
    }

    @Override
    public String toString(){
        return "(" + nonterminalSymbol + "->" + left + "·" + right + ", " + terminalSymbolSet + ")";
    }
}
