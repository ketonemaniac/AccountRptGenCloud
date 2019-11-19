package net.ketone.accrptgen.auth.service;

import net.ketone.accrptgen.auth.InitialDataLoader;
import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.repository.RoleRepository;
import net.ketone.accrptgen.auth.repository.UserRepository;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;
import static net.ketone.accrptgen.config.Constants.USERS_FILE_SEPARATOR;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    @Override
    public User save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return saveWithEncryptedPassword(user);
    }


    @Override
    public User saveWithEncryptedPassword(User user) {
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void persistUsers() {
        List<User> users = userRepository.findAll();
        StringBuffer sb = new StringBuffer();
        try {
            users.forEach(user -> {
               sb.append(user.getUsername()).append(USERS_FILE_SEPARATOR)
                       .append(user.getPassword()).append(USERS_FILE_SEPARATOR)
                       .append(user.getEmail())
                       .append(System.lineSeparator());
            });
            storageService.store(sb.toString().getBytes(), USERS_FILE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error persisting users", e);
        }
    }

}
