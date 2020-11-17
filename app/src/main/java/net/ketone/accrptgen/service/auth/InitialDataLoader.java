package net.ketone.accrptgen.service.auth;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.auth.Role;
import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;
import static net.ketone.accrptgen.config.Constants.USERS_FILE_SEPARATOR;

@Slf4j
@Configuration
public class InitialDataLoader {

    @Autowired
    private UserService userService;
    @Autowired
    private StorageService persistentStorage;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            InputStream resource = persistentStorage.loadAsInputStream(USERS_FILE);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource))) {
                reader.lines().forEach(line -> {
                    String[] userPass = line.split(USERS_FILE_SEPARATOR);
                    Set<Role> userRoles = Arrays.asList(userPass).stream().skip(3)
                            .map(roleName -> Role.builder().name(roleName).build())
                            .collect(Collectors.toSet());
                    User user = User.builder().username(userPass[0])
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
        };
    }
}
