package com.github.igorsuhorukov.reflection.service;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.schemaspy.model.Database;

import java.io.ObjectInputStream;

@UtilityClass
public class Utils {
    @SneakyThrows
    public Database getDatabase() {
        ObjectInputStream objectInputStream = new ObjectInputStream(
                SchemaFilterTest.class.getResourceAsStream("/model.obj"));
        return (Database) objectInputStream.readObject();
    }
}
