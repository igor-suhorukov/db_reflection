package com.github.igorsuhorukov.reflection.model.matcher;

import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class Rule {
    List<Matcher> include;
    List<Matcher> exclude;

    public boolean match(String name){
        Optional<Matcher> matchExclude = exclude.stream().filter(matcher -> matcher.match(name)).findFirst();
        if(matchExclude.isPresent()){
            return false;
        }
        if(include.isEmpty()){
            return true;
        } else {
            Optional<Matcher> matchInclude = include.stream().filter(matcher -> matcher.match(name)).findFirst();
            return matchInclude.isPresent();
        }
    }
}
