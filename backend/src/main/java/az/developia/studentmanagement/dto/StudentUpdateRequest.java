package az.developia.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateRequest {

    @NotNull(message = "ID boş ola bilməz")
    private Long id;

    @NotBlank(message = "Ad boş ola bilməz")
    private String name;

    @NotBlank(message = "Soyad boş ola bilməz")
    private String surname;

    @Email(message = "Düzgün email formatı daxil edin")
    @NotBlank(message = "Email boş ola bilməz")
    private String email;

    @Min(value = 18, message = "Yaş minimum 18 olmalıdır")
    private Integer age;
}