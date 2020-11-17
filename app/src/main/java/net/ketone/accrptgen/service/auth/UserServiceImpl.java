package net.ketone.accrptgen.service.auth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.auth.Role;
import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.repo.auth.RoleRepository;
import net.ketone.accrptgen.repo.auth.UserRepository;
import net.ketone.accrptgen.service.mail.EmailService;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;
import static net.ketone.accrptgen.config.Constants.USERS_FILE_SEPARATOR;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private EmailService emailService;


    private Map<String, Role> roles;

    @PostConstruct
    public void init() {
        roles = ImmutableMap.of(
                "User", save(Role.builder().name("User").build()),
                "Admin", save(Role.builder().name("Admin").build())
        );
    }


    @Override
    public Mono<User> save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return saveWithEncryptedPassword(user);
    }


    @Override
    public Mono<User> updatePassword(String username, String clearPassword) {
        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Logged in User found")))
                .map(user -> {
                    user.setPassword(clearPassword);
                    return user;
                })
                .flatMap(this::save)
                .map(this::ripPassword);
    }

    @Override
    public Mono<User> updateUser(User updatedUser) {
        return Mono.fromCallable(() -> userRepository.findByUsername(updatedUser.getUsername()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User %s not found", updatedUser.getUsername()))))
                .map(user -> user.toBuilder()
                        .email(updatedUser.getEmail())
                        .roles(updatedUser.getRoles().stream()
                                .map(Role::getName)
                                .map(roles::get)
                                .collect(Collectors.toSet()))
                        .build())
                .flatMap(this::saveWithEncryptedPassword)
                .map(this::ripPassword);

    }

    @Override
    public Mono<User> createUser(User user, boolean isInit) {
        return Mono.fromCallable(() -> userRepository.findByUsername(user.getUsername()))
                .flatMap(existingUser -> Mono.error(new RuntimeException(
                        String.format("user %s already exists", existingUser.getUsername()))))
                .then(saveWithEncryptedPassword(user.toBuilder()
                        .roles(user.getRoles().stream().map(Role::getName).map(roles::get)
                                .collect(Collectors.toSet()))
                        .password(isInit ? user.getPassword() :
                                bCryptPasswordEncoder.encode(user.getPassword()))
                        .build(), isInit)
                );
    }

    @Override
    public Mono<User> deleteUser(String username) {
        return Mono.fromCallable(() -> userRepository.deleteByUsername(username))
                .doOnSuccess(r -> persistUsers())
                .map(list -> list.get(0));
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return Mono.fromCallable(() -> Optional.ofNullable(userRepository.findByUsername(username))
                .orElse(User.builder().username("Anonymous")
                        .email("anon@mail.com")
                        .roles(Sets.newHashSet(Role.builder().name("User").build())).build()));
    }

    @Override
    public Flux<User> findAllUsers() {
        return Mono.fromCallable(() -> userRepository.findAll())
                .flatMapMany(users -> Flux.fromStream(users.stream()))
                .map(this::ripPassword);
    }

    @Override
    public Mono<User> resetPassword(User user) throws Exception {
        final String pwd = generatePassword(8);
        return this.updatePassword(user.getUsername(), pwd)
                .doOnNext(u -> {
                    u.setPassword(pwd);
                    try {
                        emailService.sendResetPasswordEmail(u);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(this::ripPassword);
    }


    private Role save(Role role) {
        return roleRepository.save(role);
    }

    private Mono<User> saveWithEncryptedPassword(User user) {
        return saveWithEncryptedPassword(user, false);
    }

    private Mono<User> saveWithEncryptedPassword(User user, boolean isInit) {
        return Mono.fromCallable(() ->  userRepository.save(user))
                .doOnSuccess(r -> {if(!isInit) persistUsers();});
    }

    private void persistUsers() {
        List<User> users = userRepository.findAll();
        StringBuffer sb = new StringBuffer();
        try {
            users.forEach(user -> {
                sb.append(user.getUsername()).append(USERS_FILE_SEPARATOR)
                        .append(user.getPassword()).append(USERS_FILE_SEPARATOR)
                        .append(user.getEmail()).append(USERS_FILE_SEPARATOR)
                        .append(user.getRoles().stream()
                        .map(Role::getName).collect(Collectors.joining(USERS_FILE_SEPARATOR)))
                        .append(System.lineSeparator());
            });
            persistentStorage.store(sb.toString().getBytes(), USERS_FILE);
        } catch (IOException e) {
            log.error("Error persisting users", e);
        }
    }

    private User ripPassword(User user) {
        user.setPassword(null);
        return user;
    }

    private static String generatePassword(int length) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++) {
            char ch = (char) ((Math.random() * 10000) % (122 - 48) + 48);
            sb.append(ch);
        }
        return sb.toString();
    }


    public static void main(String [] args) {
        for(int i = 0; i < 100; i++)
            System.out.println(generatePassword(8));
    }
}
