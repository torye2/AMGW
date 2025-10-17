package amgw.amgw.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupForm {
    @NotBlank private String username;
    @NotBlank private String name;
    @Email @NotBlank private String email;
    private String department;
    @NotBlank private String password;
    @NotBlank private String passwordConfirm;
}
