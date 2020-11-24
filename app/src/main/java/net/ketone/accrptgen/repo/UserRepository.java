package net.ketone.accrptgen.repo;

import net.ketone.accrptgen.domain.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, Long> {

    User findByUsername(String username);

    List<User> deleteByUsername(String username);
}
