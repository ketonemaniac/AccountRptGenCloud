package net.ketone.accrptgen.app.api;

import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.domain.user.UserService;
import net.ketone.accrptgen.app.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    // @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

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

}