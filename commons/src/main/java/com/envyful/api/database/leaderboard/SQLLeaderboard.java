package com.envyful.api.database.leaderboard;

import com.envyful.api.database.Database;
import com.envyful.api.type.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SQLLeaderboard {

    private final Database database;
    private final String table;
    private final Order order;
    private final String orderColumn;
    private final int perPage;
    private final long cacheDuration;
    private final String extraClauses;
    private final SQLBiFunction<ResultSet, Integer, String> formatter;

    private final Map<Integer, Pair<Long, List<String>>> cachedEntries = Maps.newHashMap();

    private SQLLeaderboard(Database database, String table, Order order, String orderColumn, int perPage,
                           long cacheDuration, String extraClauses, SQLBiFunction<ResultSet, Integer, String> formatter) {
        this.database = database;
        this.table = table;
        this.order = order;
        this.orderColumn = orderColumn;
        this.perPage = perPage;
        this.cacheDuration = cacheDuration;
        this.extraClauses = extraClauses;
        this.formatter = formatter;
    }

    public List<String> getPage(int page) {
        Pair<Long, List<String>> longListPair = this.cachedEntries.get(page);

        if (longListPair != null && (System.currentTimeMillis() - longListPair.getX()) <= this.cacheDuration) {
            return longListPair.getY();
        }

        try (Connection connection = this.database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(this.getSQL())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int counter = 0;
            List<String> data = Lists.newArrayList();

            while (resultSet.next()) {
                if ((counter / perPage) == page) {
                    data.add(this.formatter.apply(resultSet, counter));
                }

                ++counter;
            }

            this.cachedEntries.put(counter, Pair.of(System.currentTimeMillis(), data));
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private String getSQL() {
        return "SELECT * FROM `" + this.table + "` WHERE " + this.extraClauses + this.order.getSqlText(this.orderColumn) + ";";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Database database;
        private String table;
        private Order order;
        private String orderColumn;
        private int perPage;
        private long cacheDuration;
        private String extraClauses;
        private SQLBiFunction<ResultSet, Integer, String> formatter;

        Builder() {}

        public Builder database(Database database) {
            this.database = database;
            return this;
        }

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public Builder column(String column) {
            this.orderColumn = column;
            return this;
        }

        public Builder pageSize(int perPage) {
            this.perPage = perPage;
            return this;
        }

        public Builder formatter(SQLBiFunction<ResultSet, Integer, String> formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder cacheDuration(long cacheDuration) {
            this.cacheDuration = cacheDuration;
            return this;
        }

        public Builder extraClauses(String extraClauses) {
            this.extraClauses = extraClauses;
            return this;
        }

        public SQLLeaderboard build() {
            return new SQLLeaderboard(this.database, this.table, this.order, this.orderColumn, this.perPage,
                    this.cacheDuration, extraClauses, this.formatter);
        }
    }
}
