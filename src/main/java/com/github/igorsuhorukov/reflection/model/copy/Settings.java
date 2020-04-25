package com.github.igorsuhorukov.reflection.model.copy;

import lombok.Value;

import java.util.List;
import com.github.igorsuhorukov.reflection.model.copy.refresh.RefreshSettings;

@Value
public class Settings {
    List<Partition> partitions;
    List<Sort> sorts;
    //predicate list for sql 'where'?
    //calculate statistics (sql 'group by' partitions) before fetch data
    RefreshSettings refreshSettings;
}
