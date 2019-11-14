package net.ketone.accrptgen.auth.service;


import net.ketone.accrptgen.auth.model.User;

public interface UserService {

    User save(User user);

    User saveWithEncryptedPassword(User user);

    User findByUsername(String username);
}
