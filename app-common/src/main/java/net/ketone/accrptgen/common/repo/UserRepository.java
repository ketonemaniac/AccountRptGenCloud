package net.ketone.accrptgen.common.repo;

import net.ketone.accrptgen.common.model.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, Long> {

    User findByUsername(String username);

    List<User> deleteByUsername(String username);
}
