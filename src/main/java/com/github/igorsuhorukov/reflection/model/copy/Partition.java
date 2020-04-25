package com.github.igorsuhorukov.reflection.model.copy;

import lombok.Value;

@Value
public class Partition {
    String partitionName;
    String column;
    String transformation;
}
