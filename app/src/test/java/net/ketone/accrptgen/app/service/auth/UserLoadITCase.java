package net.ketone.accrptgen.app.service.auth;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.auth.Role;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.domain.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * For legacy user file load
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"prod","gCloudStandard"})
public class UserLoadITCase {

    // login users
    public static final String USERS_FILE = "users.txt";
    public static final String USERS_FILE_SEPARATOR = ",";

    @Autowired
    private UserService userService;
    @Autowired
    private StorageService persistentStorage;

    @Test
    public void loadData() {
        userService.deleteAll();
        InputStream resource = persistentStorage.loadAsInputStream(USERS_FILE);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource))) {
            reader.lines().forEach(line -> {
                String[] userPass = line.split(USERS_FILE_SEPARATOR);
                Set<Role> userRoles = Arrays.asList(userPass).stream().skip(3)
                        .map(roleName -> Role.builder()
                                .id(UUID.randomUUID())
                                .name(roleName).build())
                        .collect(Collectors.toSet());
                User user = User.builder()
                        .id(UUID.randomUUID())
                        .username(userPass[0])
                        .password(userPass[1])
                        .email(userPass[2])
                        .roles(userRoles)
                        .build();
                log.info("saving user " + user.getUsername());
                userService.createUser(user, true).subscribe();
            });
        } catch (Exception e) {
            log.error("Error loading " + USERS_FILE + " ", e);
            e.printStackTrace();
        }
    }
}
