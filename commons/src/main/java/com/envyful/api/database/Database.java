package com.envyful.api.database;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * Interface representing a database connection
 *
 */
public interface Database {

    /**
     *
     * Gets the SQL connection
     *
     * @return An SQL Connection
     * @throws SQLException An error if there's no connections
     * @throws UnsupportedOperationException If this isn't an SQL database
     */
    Connection getConnection() throws SQLException,UnsupportedOperationException;

    /**
     *
     * Gets the Jedis connection from the database if available
     *
     * @return The jedis connection
     * @throws JedisException If there's an error with the jedis
     * @throws UnsupportedOperationException If this database isn't redis lol
     */
    Jedis getJedis() throws JedisException,UnsupportedOperationException;

    /**
     *
     *
     *
     * @param o
     * @throws JedisException
     * @throws UnsupportedOperationException
     */
    void subscribe(Object o) throws JedisException,UnsupportedOperationException;

    /**
     *
     * Closes the connection
     *
     */
    void close();

}
