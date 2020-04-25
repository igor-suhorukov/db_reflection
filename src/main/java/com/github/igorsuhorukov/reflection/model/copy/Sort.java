package com.github.igorsuhorukov.reflection.model.copy;

import lombok.Value;

@Value
public class Sort {
    String column;
    Order order;

    public enum Order {
        ASC, DESC
    }
}
