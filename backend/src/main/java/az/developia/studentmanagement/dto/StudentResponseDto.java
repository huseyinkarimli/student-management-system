package az.developia.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDto {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private Integer age;
}