package net.ketone.accrptgen.common.domain.user;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.auth.Role;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.repo.RoleRepository;
import net.ketone.accrptgen.common.repo.UserRepository;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        roles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, Function.identity(), (a, b) -> b));
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
                        .cc(updatedUser.getCc())
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
                        .id(UUID.randomUUID())
                        .roles(user.getRoles().stream().map(Role::getName).map(roles::get)
                                .collect(Collectors.toSet()))
                        .password(isInit ? user.getPassword() :
                                bCryptPasswordEncoder.encode(user.getPassword()))
                        .cc(user.getCc())
                        .build(), isInit)
                );
    }

    @Override
    public Mono<User> deleteUser(String username) {
        return Mono.fromCallable(() -> userRepository.deleteByUsername(username))
                .map(list -> list.get(0));
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return Mono.fromCallable(() -> Optional.ofNullable(userRepository.findByUsername(username))
                .orElse(User.builder().username("Anonymous")
                        .email("ketoneaussie@gmail.com")
                        .roles(Sets.newHashSet(Role.builder().name("Admin").build())).build()));
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

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }


    private Role save(Role role) {
        return roleRepository.save(role);
    }

    private Mono<User> saveWithEncryptedPassword(User user) {
        return saveWithEncryptedPassword(user, false);
    }

    private Mono<User> saveWithEncryptedPassword(User user, boolean isInit) {
        return Mono.fromCallable(() ->  userRepository.save(user));
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

}
