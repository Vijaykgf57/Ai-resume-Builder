package com.resumeai.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Email @NotBlank
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Builder.Default
    private String role = "USER";
}
