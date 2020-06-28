package com.ninos.service.Impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ninos.domain.User;
import com.ninos.domain.UserPrincipal;
import com.ninos.enumeration.Role;
import com.ninos.exception.domain.EmailExistException;
import com.ninos.exception.domain.UserNotFoundException;
import com.ninos.exception.domain.UsernameExistException;
import com.ninos.repository.UserRepository;
import com.ninos.service.LoginAttemptService;
import com.ninos.service.UserService;




@Qualifier("UserDetailsService")
@Transactional
@Service
public class UserServiceImple implements UserService, UserDetailsService {
	
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService;
	
	
    @Autowired
	public UserServiceImple(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
	}



	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
	     User user = userRepository.findUserByUsername(username);
	     if(user == null) {
	    	 LOGGER.error("User not found by username: "+username);
	    	 throw new UsernameNotFoundException("User not found by username: "+username);
	     }else {
	    	 validateLoginAttempt(user);
	    	 user.setLastLoginDateDisplay(user.getLastLoginDate());
	    	 user.setLastLoginDate(new Date());
	    	 userRepository.save(user);
	    	 UserPrincipal userPrincipal = new UserPrincipal(user);
	    	 LOGGER.info("Returning found user by username: "+ username);
	    	 return userPrincipal;
	     }
		
		
	}



	private void validateLoginAttempt(User user) {
		if(user.isNotLocked()) {
			if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
				   user.setNotLocked(false);
			}else {
				   user.setNotLocked(true);
			}
			
			
		}else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
		
	}



	@Override
	public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl());
		userRepository.save(user);
		LOGGER.info("New user password: " + password);
		return user;
	}



	private String getTemporaryProfileImageUrl() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
	}
	
	



	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}



	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10); // return 10 random letters
	}



	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);   // return a random String and it has a length = 10 numbers
	}


	
	
	
	

	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        
        if(StringUtils.isNotBlank(currentUsername)) {
        	
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException("No user found by username" + currentUsername);
            }
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException("Username already exists");
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException("Email alreay Exists");
            }
            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UsernameExistException("Username already exists");
            }
            if(userByNewEmail != null) {
                throw new EmailExistException("Email already exists");
            }
            return null;
        }
    }



	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}



	@Override
	public User findUserByUsername(String username) {
		return userRepository.findUserByUsername(username);
	}



	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

}
