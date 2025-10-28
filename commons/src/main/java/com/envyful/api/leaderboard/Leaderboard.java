package com.envyful.api.leaderboard;

import com.envyful.api.database.Database;
import com.envyful.api.database.SQLFunction;
import com.envyful.api.database.leaderboard.Order;
import com.envyful.api.type.TimeOutHashMap;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Leaderboard<A> {

    private final Database database;
    private final String table;
    private final Order order;
    private final String orderColumn;
    private final int perPage;
    private final long cacheDuration;
    private final String extraClauses;
    private final SQLFunction<ResultSet, A> formatter;
    private final Map<Integer, List<A>> cachedEntries;

    private Leaderboard(Database database, String table, Order order, String orderColumn, int perPage, long cacheDuration, String extraClauses, SQLFunction<ResultSet, A> formatter) {
        this.database = database;
        this.table = table;
        this.order = order;
        this.orderColumn = orderColumn;
        this.perPage = perPage;
        this.cacheDuration = cacheDuration;
        this.extraClauses = extraClauses;
        this.formatter = formatter;
        this.cachedEntries = new TimeOutHashMap<>(cacheDuration);
    }

    public List<A> getPage(int page) {
        List<A> cachedPage = this.cachedEntries.get(page);

        if (cachedPage != null) {
            return cachedPage;
        }

        return this.loadPage(page);
    }

    public List<A> loadPage(int page) {
        try (Connection connection = this.database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(this.getSQL())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int counter = 0;
            List<A> data = Lists.newArrayList();

            while (resultSet.next()) {
                if ((counter / perPage) == page) {
                    data.add(this.formatter.apply(resultSet));
                }

                ++counter;
            }

            this.cachedEntries.put(page, data);
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private String getSQL() {
        return "SELECT * FROM `" + this.table + "` WHERE " + this.extraClauses + this.order.getSqlText(this.orderColumn) + ";";
    }

    public static <A> Builder<A> builder(Class<A> a) {
        return new Builder<>();
    }

    public static class Builder<A> {

        private Database database;
        private String table;
        private Order order;
        private String orderColumn;
        private int perPage;
        private long cacheDuration;
        private String extraClauses;
        private SQLFunction<ResultSet, A> formatter;

        Builder() {}

        public Builder<A> database(Database database) {
            this.database = database;
            return this;
        }

        public Builder<A> table(String table) {
            this.table = table;
            return this;
        }

        public Builder<A> order(Order order) {
            this.order = order;
            return this;
        }

        public Builder<A> column(String column) {
            this.orderColumn = column;
            return this;
        }

        public Builder<A> pageSize(int perPage) {
            this.perPage = perPage;
            return this;
        }

        public Builder<A> formatter(SQLFunction<ResultSet, A> formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder<A> cacheDuration(long cacheDuration) {
            this.cacheDuration = cacheDuration;
            return this;
        }

        public Builder<A> extraClauses(String extraClauses) {
            this.extraClauses = extraClauses;
            return this;
        }

        public Leaderboard<A> build() {
            return new Leaderboard<>(this.database, this.table, this.order, this.orderColumn, this.perPage,
                    this.cacheDuration, extraClauses, this.formatter);
        }
    }
}
