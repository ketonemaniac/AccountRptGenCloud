package net.ketone.accrptgen.common.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @Id
    @JsonIgnore
    private UUID id;

    private String username;

    private String password;

    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    private List<String> cc;

    private Boolean noCCemail;

}
