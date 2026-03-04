package com.oceanview.service;

import com.oceanview.model.User;
import com.oceanview.util.ServiceResult;

import java.util.List;

public interface UserService {
    ServiceResult authenticate(String username, String password) throws ServiceException;
    ServiceResult register(User user) throws ServiceException;
    ServiceResult resetPassword(String userId, String newPassword) throws ServiceException;
    ServiceResult changePassword(String userId, String currentPassword, String newPassword) throws ServiceException;
    ServiceResult createStaffUser(User user) throws ServiceException;
    ServiceResult toggleUserActive(String userId) throws ServiceException;
    User getUserById(String id) throws ServiceException;
    User getUserByUsername(String username) throws ServiceException;
    List<User> getAllUsers() throws ServiceException;
    List<User> getUsersByRole(String role) throws ServiceException;
    ServiceResult updateUser(User user) throws ServiceException;
    ServiceResult deleteUser(String id) throws ServiceException;
}



