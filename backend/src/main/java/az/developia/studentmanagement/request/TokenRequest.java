package az.developia.studentmanagement.request;

import lombok.Data;

@Data
public class TokenRequest {
    private String refreshToken;
}