package amgw.amgw.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupForm {
    @NotBlank private String username;
    @NotBlank private String name;
    @Email @NotBlank private String email;
    private String department;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 10, max = 30, message = "비밀번호는 10자 이상 30자 이하로 입력해주세요.")
    private String password;
    @NotBlank private String passwordConfirm;
}
