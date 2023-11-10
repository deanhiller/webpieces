package org.webpieces.googleauth.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.webpieces.googleauth.client.api.FetchProfileResponse;
import org.webpieces.googleauth.client.api.UserProfile;
import org.webpieces.http.exception.ForbiddenException;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

public class GoogleAuth {

    private final AuthApiConfig config;

    @Inject
    public GoogleAuth(AuthApiConfig config) {

        this.config = config;
    }

    public XFuture<UserProfile> fetchProfile(String idTokenString) {
        try {
            return fetchProfileImpl(idTokenString);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }

    public XFuture<UserProfile> fetchProfileImpl(String idTokenString) throws GeneralSecurityException, IOException {
        // Initialize the HTTP transport
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        // Initialize the JSON factory
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(config.getClientId()))
                .build();

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if(idToken == null)
            throw new ForbiddenException("Invalid token");

        Payload payload = idToken.getPayload();

        UserProfile profile = new UserProfile();

        // Print user identifier
        profile.setUserId(payload.getSubject());
        profile.setEmail(payload.getEmail());
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        profile.setEmailVerified(emailVerified);
        String name = (String) payload.get("name");
        profile.setName(name);
        String pictureUrl = (String) payload.get("picture");
        profile.setPictureUrl(pictureUrl);
        String locale = (String) payload.get("locale");
        profile.setLocale(locale);
        String familyName = (String) payload.get("family_name");
        profile.setFamilyName(familyName);
        String givenName = (String) payload.get("given_name");
        profile.setGivenName(givenName);

        return XFuture.completedFuture(profile);
    }
}
