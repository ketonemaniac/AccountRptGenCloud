package net.ketone.accrptgen.app.service.auth;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.auth.Role;
import net.ketone.accrptgen.common.repo.RoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

/**
 * For legacy user file load
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"prod","gCloudStandard"})
public class RoleLoadITCase {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void loadData() {
        roleRepository.deleteAll();
        roleRepository.saveAll(List.of(Role.builder().id(UUID.randomUUID()).name("User").build(),
                Role.builder().id(UUID.randomUUID()).name("Admin").build()));
    }
}
