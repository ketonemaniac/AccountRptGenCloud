package net.ketone.accrptgen.repo.auth;

import net.ketone.accrptgen.domain.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Transactional
    List<User> deleteByUsername(String username);
}
