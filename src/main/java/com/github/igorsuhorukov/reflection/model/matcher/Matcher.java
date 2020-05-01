package com.github.igorsuhorukov.reflection.model.matcher;


import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Matcher {
    String exact;
    String regexp;

    public Matcher(String exact) {
        this.exact = exact;
        this.regexp=null;
    }

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
