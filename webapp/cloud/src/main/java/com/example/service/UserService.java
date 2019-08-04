package com.example.service;

import com.example.model.User;

import java.util.List;

public interface UserService {
	public User findUserByEmail(String email);
	//public List<User> getAllUsers();
	public void saveUser(User user);
	public void saveAboutMe(User user);
}
