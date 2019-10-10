package net.ketone.accrptgen.auth.repository;

import net.ketone.accrptgen.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
