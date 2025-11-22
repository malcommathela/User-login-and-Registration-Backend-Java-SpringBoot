package com.example.User.login.and.Registration.Backend_Java_SpringBoot.registration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.bind.annotation.CrossOrigin;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString

public class RegistrationRequest {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
}
