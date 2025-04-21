package net.ketone.accrptgen.app.api;

import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.domain.user.UserService;
import net.ketone.accrptgen.common.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/user")
public class UserAdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    // @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @PostMapping("password/reset")
    public Mono<User> resetPassword(@RequestBody User user) throws Exception {
        return userService.resetPassword(user)
                .doOnNext(u -> {
                    try {
                        emailService.sendResetPasswordEmail(u);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }})
                .map(u -> {
                    u.setPassword(null);
                    return u;
                });
    };

    @PutMapping
    public Mono<User> updateUser(@RequestBody User updatedUser) {
        return userService.updateUser(updatedUser);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody User newUser) {
        return userService.createUser(newUser, false);
    }

    @DeleteMapping("{username}")
    public Mono<User> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }


    /**
     * List basic information about Users (without password or sensitive information
     * @return
     */
    @GetMapping("all")
    public Flux<User> listUsers() {
        return userService.findAllUsers();
    }



}