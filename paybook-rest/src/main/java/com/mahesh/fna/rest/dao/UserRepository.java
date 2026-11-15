package com.mahesh.fna.rest.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mahesh.fna.rest.dao.entity.UserDataEntity;

@Repository
public interface UserRepository extends JpaRepository<UserDataEntity, Integer> {

	Optional<UserDataEntity> findByEmailId(String emailId);

	boolean existsByEmailId(String emailId);
}