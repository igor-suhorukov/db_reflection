package com.github.igorsuhorukov.reflection.model.matcher;


import lombok.Value;

@Value
public class Matcher {
    String exact;
    String regexp;

    public boolean match(String name){
        if(exact!=null && !exact.isEmpty()){
            return exact.equalsIgnoreCase(name);
        }
        if(regexp!=null && !regexp.isEmpty()){
            return name.matches(regexp);
        }
        return false;
    }
}
