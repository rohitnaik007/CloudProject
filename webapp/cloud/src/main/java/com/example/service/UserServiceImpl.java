package com.example.service;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	@Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        user.setAboutme("");
		Role userRole = new Role();
		userRole.setRole("ADMIN");
		roleRepository.save(userRole);
//        userRole = roleRepository.findByRole("ADMIN");
//        if (null==userRole)
//		{
//			try {
//				userRole.setRole("ADMIN");
//				roleRepository.save(userRole);
//				//roleRepository.insertROle();
//				//userRole = roleRepository.findByRole("ADMIN");
//			}
//			catch (Exception e)
//			{
//				System.out.println("userRole: "+userRole);
//			}
//		}
		/*Role userRole = new Role();
		userRole.setId(1);*/
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
	}
	@Override
	public void saveAboutMe(User user) {
	//	user.setAboutme("Rohit");
		userRepository.save(user);
	}

	//@Override
	//public List<User> getAllUsers() {
	//	return userRepository.findAllUsers();
	//}
}
