package com.pizzaria.demo.user.repository;

import com.pizzaria.demo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailAndEnabledTrue(String email);

    List<User> findAllByEnabledTrue();

    Optional<User> findByIdAndEnabledTrue(Integer id);


}
