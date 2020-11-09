package net.ketone.accrptgen.repo.auth;

import net.ketone.accrptgen.domain.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
