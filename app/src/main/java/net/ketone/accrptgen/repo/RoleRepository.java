package net.ketone.accrptgen.repo;

import net.ketone.accrptgen.domain.auth.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, Long> {

}
