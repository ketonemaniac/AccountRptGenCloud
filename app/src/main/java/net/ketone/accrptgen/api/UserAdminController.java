package net.ketone.accrptgen.api;

import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/admin/user")
public class UserAdminController {

    private static final Logger logger = Logger.getLogger(UserAdminController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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