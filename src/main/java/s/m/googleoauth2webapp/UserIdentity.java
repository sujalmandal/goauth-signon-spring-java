package s.m.googleoauth2webapp;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Getter
@Slf4j
@ToString
public class UserIdentity {

    public String userId;
    public String email;
    public boolean isEmailVerified;
    public String name;
    public String picture;
    public String locale;

    public static UserIdentity fromToken(String token, String clientId) {
        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String userId = payload.getSubject();
                log.info("user {} verified", userId);
                UserIdentity identity = new UserIdentity();
                identity.userId = userId;
                identity.email = payload.getEmail();
                identity.isEmailVerified = payload.getEmailVerified();
                identity.name = (String) payload.get("name");
                identity.picture = (String) payload.get("picture");
                identity.locale = (String) payload.get("locale");
                return identity;
            } else {
                log.warn("invalid token");
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
