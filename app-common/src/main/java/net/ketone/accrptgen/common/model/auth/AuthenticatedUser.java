package net.ketone.accrptgen.common.model.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    @Getter
    private User user;

    public AuthenticatedUser(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), user.getPassword(), authorities);
        this.user = user;
    }


}