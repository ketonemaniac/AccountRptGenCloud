package net.ketone.accrptgen.auth.repository;

import net.ketone.accrptgen.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Transactional
    List<User> deleteByUsername(String username);
}
