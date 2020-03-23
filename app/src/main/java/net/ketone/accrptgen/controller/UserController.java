package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.service.UserService;
import net.ketone.accrptgen.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping
    public User getCurrentUser() {
        return userService.findByUsername(UserUtils.getAuthenticatedUser()).block();
    }

    @GetMapping("encode/{pass}")
    public String encode(@PathVariable String pass) {
        return bCryptPasswordEncoder.encode(pass);
    }

    @PostMapping("password")
    public User updatePassword(@RequestBody User newUser) {
        return userService.updatePassword(UserUtils.getAuthenticatedUser(), newUser.getPassword())
                .block();
    }

    @PostMapping("password/reset")
    public User resetPassword(@RequestBody User user) throws Exception {
        return userService.resetPassword(user)
                .block();
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        return userService.updateUser(updatedUser)
                .block();
    }

    @PostMapping
    public User createUser(@RequestBody User newUser) {
        return userService.createUser(newUser, false)
                .block();
    }

    @DeleteMapping("{username}")
    public User deleteUser(@PathVariable String username) {
        return userService.deleteUser(username)
                .block();
    }


    /**
     * List basic information about Users (without password or sensitive information
     * @return
     */
    @GetMapping("all")
    public List<User> listUsers() {
        return userService.findAllUsers()
                .collectList().block();
    }



}