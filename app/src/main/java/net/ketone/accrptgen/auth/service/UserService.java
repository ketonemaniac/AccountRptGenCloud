package net.ketone.accrptgen.auth.service;


import net.ketone.accrptgen.auth.model.User;

public interface UserService {

    void save(User user);

    User findByUsername(String username);
}
