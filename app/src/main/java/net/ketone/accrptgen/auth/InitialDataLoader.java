package net.ketone.accrptgen.auth;

import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.service.UserService;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.logging.Logger;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;
import static net.ketone.accrptgen.config.Constants.USERS_FILE_SEPARATOR;

@Configuration
public class InitialDataLoader {

    private static final Logger logger = Logger.getLogger(InitialDataLoader.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;

    @Bean
    public CommandLineRunner init() {
        return args -> {
        InputStream resource = storageService.loadAsInputStream(USERS_FILE);

        try ( BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource)) ) {
                    reader.lines().forEach(line -> {
                        String[] userPass = line.split(USERS_FILE_SEPARATOR);
                        User user = User.builder().username(userPass[0])
                                .password(userPass[1])
                                .email(userPass[2]).build();
//                        userStr.add(line);
                        logger.info("saving user " + user.getUsername());
                        userService.saveWithEncryptedPassword(user);
                    });
            } catch (Exception e) {
            logger.severe("Error loading " + USERS_FILE + " " + e.getMessage());
        }
    };
    }

}
