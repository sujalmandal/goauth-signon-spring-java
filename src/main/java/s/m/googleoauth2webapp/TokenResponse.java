package s.m.googleoauth2webapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class TokenResponse {
    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("expires_in")
    private int expiresIn;
    @JsonAlias("scope")
    private String scope;
    @JsonAlias("token_type")
    private String tokenType;
    @JsonAlias("id_token")
    private String idToken;
}