package com.github.igorsuhorukov.reflection.model.copy;

import com.github.igorsuhorukov.reflection.model.copy.refresh.RefreshSettings;
import lombok.Value;

import java.util.List;

@Value
public class Settings {
    List<Partition> partitions;
    List<Sort> sorts;
    //predicate list for sql 'where'?
    //calculate statistics (sql 'group by' partitions) before fetch data
    RefreshSettings refreshSettings;
}
