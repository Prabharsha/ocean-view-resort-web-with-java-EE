package com.oceanview.service.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;
import com.oceanview.model.User;
import com.oceanview.service.ServiceException;
import com.oceanview.service.UserService;
import com.oceanview.util.EmailUtil;
import com.oceanview.util.PasswordUtil;
import com.oceanview.util.ServiceResult;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDAO userDAO;

    public UserServiceImpl() { this.userDAO = new UserDAOImpl(); }
    public UserServiceImpl(UserDAO userDAO) { this.userDAO = userDAO; }

    @Override
    public ServiceResult authenticate(String username, String password) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(password))
                return ServiceResult.failure("Username and password are required");
            User user = userDAO.findByUsername(username);
            if (user == null) return ServiceResult.failure("Invalid username or password");
            if (!user.isActive()) return ServiceResult.failure("Account is deactivated. Contact administrator.");
            if (!PasswordUtil.check(password, user.getPassword())) return ServiceResult.failure("Invalid username or password");
            userDAO.updateLastLogin(user.getId());
            return ServiceResult.success("Login successful", user);
        } catch (DAOException e) {
            throw new ServiceException("Authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult register(User user) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(user.getFname())) return ServiceResult.failure("First name is required");
            if (ValidationUtil.isNullOrEmpty(user.getLname())) return ServiceResult.failure("Last name is required");
            if (ValidationUtil.isNullOrEmpty(user.getUsername())) return ServiceResult.failure("Username is required");
            if (ValidationUtil.isNullOrEmpty(user.getPassword())) return ServiceResult.failure("Password is required");
            if (ValidationUtil.isNullOrEmpty(user.getEmail())) return ServiceResult.failure("Email is required");
            if (!ValidationUtil.isValidEmail(user.getEmail())) return ServiceResult.failure("Invalid email format");
            if (ValidationUtil.isNullOrEmpty(user.getRole())) return ServiceResult.failure("Role is required");
            if (userDAO.findByUsername(user.getUsername()) != null) return ServiceResult.failure("Username already exists");
            if (userDAO.findByEmail(user.getEmail()) != null) return ServiceResult.failure("Email already exists");
            user.setId(UUID.randomUUID().toString());
            user.setPassword(PasswordUtil.hash(user.getPassword()));
            user.setActive(true);
            userDAO.save(user);
            return ServiceResult.success("User registered successfully", user);
        } catch (DAOException e) {
            throw new ServiceException("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult createStaffUser(User user) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(user.getFname())) return ServiceResult.failure("First name is required");
            if (ValidationUtil.isNullOrEmpty(user.getLname()))  return ServiceResult.failure("Last name is required");
            if (ValidationUtil.isNullOrEmpty(user.getUsername())) return ServiceResult.failure("Username is required");
            if (ValidationUtil.isNullOrEmpty(user.getEmail())) return ServiceResult.failure("Email is required");
            if (!ValidationUtil.isValidEmail(user.getEmail())) return ServiceResult.failure("Invalid email format");
            if (ValidationUtil.isNullOrEmpty(user.getRole())) return ServiceResult.failure("Role is required");
            if (userDAO.findByUsername(user.getUsername()) != null) return ServiceResult.failure("Username already exists");
            if (userDAO.findByEmail(user.getEmail()) != null) return ServiceResult.failure("Email already exists");

            // Generate temp password
            String tempPassword = generateTempPassword();
            user.setId(UUID.randomUUID().toString());
            user.setPassword(PasswordUtil.hash(tempPassword));
            user.setActive(true);
            userDAO.save(user);

            // Send credentials email
            String fullName = user.getFname() + " " + user.getLname();
            EmailUtil.sendStaffCredentials(user.getEmail(), fullName, user.getUsername(), tempPassword, user.getRole());
            log.info("Staff user created: {} [role={}], credentials sent to {}", user.getUsername(), user.getRole(), user.getEmail());
            return ServiceResult.success("Staff account created. Login credentials sent to " + user.getEmail(), user);
        } catch (DAOException e) {
            throw new ServiceException("Staff user creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult changePassword(String userId, String currentPassword, String newPassword) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(currentPassword)) return ServiceResult.failure("Current password is required");
            if (ValidationUtil.isNullOrEmpty(newPassword)) return ServiceResult.failure("New password is required");
            if (newPassword.length() < 6) return ServiceResult.failure("New password must be at least 6 characters");
            if (currentPassword.equals(newPassword)) return ServiceResult.failure("New password must differ from current password");

            User user = userDAO.findById(userId);
            if (user == null) return ServiceResult.failure("User not found");
            if (!PasswordUtil.check(currentPassword, user.getPassword()))
                return ServiceResult.failure("Current password is incorrect");

            userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
            log.info("Password changed for userId={}", userId);
            return ServiceResult.success("Password changed successfully");
        } catch (DAOException e) {
            throw new ServiceException("Password change failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult toggleUserActive(String userId) throws ServiceException {
        try {
            User user = userDAO.findById(userId);
            if (user == null) return ServiceResult.failure("User not found");
            userDAO.toggleActive(userId);
            boolean newState = !user.isActive();
            return ServiceResult.success("User " + (newState ? "activated" : "deactivated") + " successfully");
        } catch (DAOException e) {
            throw new ServiceException("Toggle failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult resetPassword(String userId, String newPassword) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(newPassword)) return ServiceResult.failure("New password is required");
            if (newPassword.length() < 6) return ServiceResult.failure("Password must be at least 6 characters");
            User user = userDAO.findById(userId);
            if (user == null) return ServiceResult.failure("User not found");
            userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
            return ServiceResult.success("Password reset successfully");
        } catch (DAOException e) {
            throw new ServiceException("Password reset failed: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(String id) throws ServiceException {
        try { return userDAO.findById(id); } catch (DAOException e) { throw new ServiceException(e.getMessage(), e); }
    }

    @Override
    public User getUserByUsername(String username) throws ServiceException {
        try { return userDAO.findByUsername(username); } catch (DAOException e) { throw new ServiceException(e.getMessage(), e); }
    }

    @Override
    public List<User> getAllUsers() throws ServiceException {
        try { return userDAO.findAll(); } catch (DAOException e) { throw new ServiceException(e.getMessage(), e); }
    }

    @Override
    public List<User> getUsersByRole(String role) throws ServiceException {
        try { return userDAO.findByRole(role); } catch (DAOException e) { throw new ServiceException(e.getMessage(), e); }
    }

    @Override
    public ServiceResult updateUser(User user) throws ServiceException {
        try {
            if (ValidationUtil.isNullOrEmpty(user.getId())) return ServiceResult.failure("User ID is required");
            if (userDAO.findById(user.getId()) == null) return ServiceResult.failure("User not found");
            userDAO.update(user);
            return ServiceResult.success("User updated successfully");
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult deleteUser(String id) throws ServiceException {
        try {
            if (userDAO.findById(id) == null) return ServiceResult.failure("User not found");
            userDAO.delete(id);
            return ServiceResult.success("User deleted successfully");
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}


