package com.blog.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度为6位")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50")
    private String newPassword;
}
