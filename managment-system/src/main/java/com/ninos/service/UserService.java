package com.ninos.service;

import java.util.List;

import com.ninos.domain.User;
import com.ninos.exception.domain.EmailExistException;
import com.ninos.exception.domain.UserNotFoundException;
import com.ninos.exception.domain.UsernameExistException;

public interface UserService {
	
	User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException;
	List<User> getUsers();
	User findUserByUsername(String username);
	User findUserByEmail(String email);

}
