package wiki.feh.externalrestdemo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

public class JsonTokenManager {
    private final String JSON_SECRET;
    private final Key JSON_SECRET_KEY;

    public JsonTokenManager(String secret) {
        this.JSON_SECRET = secret;
        byte[] keyBytes = Decoders.BASE64.decode(this.JSON_SECRET);
        this.JSON_SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken() {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setIssuedAt(new Date(now))
                .signWith(JSON_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
