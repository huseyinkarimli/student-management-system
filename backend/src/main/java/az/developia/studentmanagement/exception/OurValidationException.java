package az.developia.studentmanagement.exception;

import org.springframework.validation.BindingResult;

import lombok.Getter;

@Getter
public class OurValidationException extends RuntimeException {

    private BindingResult br;

    public OurValidationException(BindingResult br) {
        this.br = br;
    }
}