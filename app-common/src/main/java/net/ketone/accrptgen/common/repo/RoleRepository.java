package net.ketone.accrptgen.common.repo;

import net.ketone.accrptgen.common.model.auth.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, Long> {

}
