package com.github.igorsuhorukov.reflection.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.igorsuhorukov.reflection.model.core.InMemoryResultSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ResultSetSerializer {
    ObjectMapper objectMapper = new ObjectMapper();
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        objectMapper.setDateFormat(df);
    }

    public String toJson(InMemoryResultSet[] resultSets) throws JsonProcessingException {
        return objectMapper.writeValueAsString(resultSets);
    }

    public InMemoryResultSet[] fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<InMemoryResultSet[]>() {});
    }
}
