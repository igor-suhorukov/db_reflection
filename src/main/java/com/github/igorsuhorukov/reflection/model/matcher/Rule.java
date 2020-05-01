package com.github.igorsuhorukov.reflection.model.matcher;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value
@AllArgsConstructor
public class Rule {
    List<Matcher> include;
    List<Matcher> exclude;

    public Rule(List<Matcher> include) {
        this.include = include;
        this.exclude = Collections.emptyList();
    }

    public Rule(Matcher include) {
        this.include = Collections.singletonList(include);
        this.exclude = Collections.emptyList();
    }

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
