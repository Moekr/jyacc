package com.moekr.jyacc;

import lombok.Data;

import java.util.List;

@Data
class Grammar {
    private String left;
    private List<String> right;
}
