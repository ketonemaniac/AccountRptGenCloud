package net.ketone.accrptgen.auth.repository;

import net.ketone.accrptgen.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Transactional
    Integer deleteByUsername(String username);
}
