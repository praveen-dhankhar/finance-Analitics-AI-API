package com.financeapp.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity validation
 */
class UserEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUser() {
        User user = new User("testuser", "test@example.com", "hashedpassword123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should have no validation violations");
    }

    @Test
    void testUserWithNullUsername() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword123");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with null username should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserWithShortUsername() {
        User user = new User("ab", "test@example.com", "hashedpassword123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with short username should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserWithLongUsername() {
        String longUsername = "a".repeat(51);
        User user = new User(longUsername, "test@example.com", "hashedpassword123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with long username should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserWithInvalidEmail() {
        User user = new User("testuser", "invalid-email", "hashedpassword123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with invalid email should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserWithNullEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword123");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with null email should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserWithShortPasswordHash() {
        User user = new User("testuser", "test@example.com", "short");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with short password hash should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("passwordHash")));
    }

    @Test
    void testUserWithNullPasswordHash() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "User with null password hash should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("passwordHash")));
    }

    @Test
    void testUserEqualsAndHashCode() {
        User user1 = new User("testuser", "test@example.com", "hashedpassword123");
        User user2 = new User("testuser", "test@example.com", "hashedpassword123");
        User user3 = new User("differentuser", "test@example.com", "hashedpassword123");
        
        assertEquals(user1, user2, "Users with same username and email should be equal");
        assertNotEquals(user1, user3, "Users with different usernames should not be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "Equal users should have same hash code");
    }

    @Test
    void testUserToString() {
        User user = new User("testuser", "test@example.com", "hashedpassword123");
        String userString = user.toString();
        
        assertTrue(userString.contains("testuser"), "toString should contain username");
        assertTrue(userString.contains("test@example.com"), "toString should contain email");
        assertFalse(userString.contains("hashedpassword123"), "toString should not contain password hash");
    }
}
