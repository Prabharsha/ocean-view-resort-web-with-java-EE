package com.oceanview.dao;

import com.oceanview.model.User;

import java.util.List;

public interface UserDAO {
    void save(User user) throws DAOException;
    void update(User user) throws DAOException;
    void updateLastLogin(String userId) throws DAOException;
    User findById(String id) throws DAOException;
    User findByUsername(String username) throws DAOException;
    User findByEmail(String email) throws DAOException;
    List<User> findAll() throws DAOException;
    List<User> findByRole(String role) throws DAOException;
    void delete(String id) throws DAOException;
    void updatePassword(String userId, String hashedPassword) throws DAOException;
}

