package s.m.googleoauth2webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Value("${gauth.auth_uri}")
    private String authRequestURI;
    @Value("${gauth.token_uri}")
    private String tokenRequestURI;
    @Value("${gauth.client_id}")
    private String clientId;
    @Value("${gauth.client_secret}")
    private String clientSecret;
    @Value("${gauth.redirect_uri}")
    private String redirectURI;

    private String SCOPE_SEPARATOR=" ";
    private List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile"
    ,"https://www.googleapis.com/auth/userinfo.email");

    private TokenResponse tokenResponse;
    private final RestTemplate restTemplate;

    @Autowired
    AuthController(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @GetMapping("/success")
    ResponseEntity<String> success(){
        log.info("redirecting to consent screen..");
        return ResponseEntity.ok("Authorization successful");
    }

    @GetMapping("/login")
    ResponseEntity<Void> redirect(){
        log.info("redirecting to consent screen..");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(getConsentRequestURI(authRequestURI, clientId, redirectURI))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<?> authCallback(@RequestParam("code") String authorizationCode){
        log.info("code received {}", authorizationCode);
        this.tokenResponse = exchangeCode(
                tokenRequestURI,
                authorizationCode,
                clientSecret,
                clientId,
                redirectURI
                );
        log.info("token received {}", tokenResponse);
        UserIdentity identity = UserIdentity.fromToken(this.tokenResponse.getIdToken(), clientId);
        log.info("verified user identity {}", identity);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/auth/success")).build();
    }

    private TokenResponse exchangeCode(
            String tokenURI,String token, String clientSecret,
            String clientId, String redirectURI){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(tokenURI)
                .queryParam("client_secret", clientSecret)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectURI)
                .queryParam("code", token)
                .queryParam("grant_type", "authorization_code");
        log.info("access code uri: {}",builder.toUriString());
        Map rawResult = restTemplate.postForObject(
                builder.toUriString(), HttpEntity.EMPTY, Map.class);
        return new ObjectMapper().convertValue(rawResult,TokenResponse.class);
    }

    private URI getConsentRequestURI(String uri, String clientId, String redirectURI){
        SCOPE_SEPARATOR = " ";
        URI consentRequestURI = UriComponentsBuilder.fromUriString(uri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectURI)
                .queryParam("scope", scopes.stream().collect(Collectors.joining(SCOPE_SEPARATOR)))
                .queryParam("response_type", "code")
                .build()
                .toUri();
        log.info("consent request uri : {}", consentRequestURI);
        return consentRequestURI;
    }
}
