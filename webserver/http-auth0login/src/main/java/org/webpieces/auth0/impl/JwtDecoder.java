package org.webpieces.auth0.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.auth0.api.JwtAuth0Body;
import org.webpieces.auth0.client.api.FetchTokenResponse;
import org.webpieces.plugin.json.JacksonJsonConverter;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.Base64;

public class JwtDecoder {
    private static final Logger log = LoggerFactory.getLogger(JwtDecoder.class);

    private JacksonJsonConverter converter;

    @Inject
    public JwtDecoder(
        JacksonJsonConverter converter
    ) {
        this.converter = converter;
    }

    public JwtAuth0Body decodeJwt(FetchTokenResponse resp) {
        DecodedJWT jwt = JWT.decode(resp.getIdToken());
        byte[] jsonBytes = Base64.getDecoder().decode(jwt.getPayload());
        String json = new String(jsonBytes, Charset.defaultCharset());
        JwtAuth0Body body = converter.readValue(json, JwtAuth0Body.class);

        return body;
    }

}
