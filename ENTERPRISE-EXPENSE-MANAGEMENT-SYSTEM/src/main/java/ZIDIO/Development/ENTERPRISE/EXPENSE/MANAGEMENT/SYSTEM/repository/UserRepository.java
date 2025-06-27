package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository;


import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;


import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}