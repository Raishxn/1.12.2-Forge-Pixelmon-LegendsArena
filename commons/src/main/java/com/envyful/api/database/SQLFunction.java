package com.envyful.api.database;

import java.sql.SQLException;

public interface SQLFunction<A, B> {

    B apply(A a) throws SQLException;

}
