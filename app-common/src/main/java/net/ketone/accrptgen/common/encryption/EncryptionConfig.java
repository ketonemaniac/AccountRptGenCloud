package net.ketone.accrptgen.common.encryption;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncryptionConfig {

    private static PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        //return
        DelegatingPasswordEncoder encoder =  (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder(); // new BCryptPasswordEncoder();
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }

    public static PasswordEncoder getEncoder() {
        return ENCODER;
    }
}
