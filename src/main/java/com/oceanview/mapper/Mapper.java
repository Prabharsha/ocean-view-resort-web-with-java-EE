package com.oceanview.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}

