package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.model.Role;
import org.springframework.transaction.annotation.Transactional;

@Repository("roleRepository")
public interface RoleRepository extends JpaRepository<Role, Integer>{

	@Modifying
	@Query(value = "INSERT INTO role VALUES (1,'ADMIN')", nativeQuery = true)
	@Transactional
	void insertROle();

	Role findByRole(String role);
}
