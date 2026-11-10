package com.evchargehub.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic Data Access Object (DAO) Interface.
 * - Generic Interface.
 *
 * @param <T> The entity type
 */
public interface GenericDAO<T> {
    boolean insert(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(int id) throws SQLException;
    T findById(int id) throws SQLException;
    List<T> findAll() throws SQLException;
}
