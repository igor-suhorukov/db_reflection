package com.github.igorsuhorukov.reflection.model.copy.refresh;

import lombok.Value;

@Value
public class RefreshSettings {
    RefreshType refreshType;
    String cron;
    Long defermentTime;
    /*
    java.math.BigDecimal
    java.math.BigInteger
    java.sql.Timestamp
    java.util.Date
    java.lang.Integer
    java.lang.Long
    java.lang.Float
    java.lang.Double
    java.lang.String
    */
    String incrementalField;
}
