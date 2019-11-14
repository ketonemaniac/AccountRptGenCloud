package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.service.UserService;
import net.ketone.accrptgen.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping
    public Mono<User> getCurrentUser() {
        return Mono.fromCallable(() -> Optional.ofNullable(userService.findByUsername(UserUtils.getAuthenticatedUser()))
        .orElse(User.builder().username("Anonymous").build()));
    }

    @GetMapping("encode/{pass}")
    public Mono<String> encode(@PathVariable String pass) {
        return Mono.just(bCryptPasswordEncoder.encode(pass));
    }

    @PostMapping("password")
    public Mono<User> updatePassword(@RequestBody User newUser) {
        return getCurrentUser()
                .filter(user -> !user.getUsername().equalsIgnoreCase("Anonymous"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Logged in User found")))
                .map(user -> {
                    user.setPassword(newUser.getPassword());
                    return user;
                })
                .map(user -> userService.save(user));
    }

}