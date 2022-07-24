package com.etrieu00.auth.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Configuration
public class SecurityConfig {

  private JWKSet jwkSet;
  private RSAKey rsaKeyAccess;
  private RSAKey rsaKeyRefresh;
  private JWSHeader rsaHeaderAccess;
  private JWSHeader rsaHeaderRefresh;
  private RSASSASigner rsassaSignerAccess;
  private RSASSASigner rsassaSignerRefresh;
  private RSASSAVerifier rsassaVerifierRefresh;

  public SecurityConfig(@Value("${secrets.rsa.key.size:2048}") Integer keySize) throws JOSEException {
    initializeWithNewKeyId(keySize);
  }

  private void initializeWithNewKeyId(Integer keySize) throws JOSEException {
    String accessId = UUID.randomUUID().toString();
    String refreshId = UUID.randomUUID().toString();
    rsaKeyAccess = generateRSAKey(accessId, keySize);
    rsaHeaderAccess = generateRsaHeadersAccess(accessId);
    rsassaSignerAccess = generateRSASigner(rsaKeyAccess);
    jwkSet = generateNewKeySet(rsaKeyAccess);

    rsaKeyRefresh = generateRSAKey(refreshId, keySize);
    rsaHeaderRefresh = generateRsaHeadersRefresh(refreshId);
    rsassaSignerRefresh = generateRSASigner(rsaKeyRefresh);
    rsassaVerifierRefresh = generateRSAVerifier(rsaKeyRefresh);
  }

  public String generateAccessToken(Date now, String uuid) {
    try {
      SignedJWT signedJWT = new SignedJWT(rsaHeaderAccess, buildClaimsAccess(now, uuid));
      signedJWT.sign(rsassaSignerAccess);
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new RuntimeException();
    }
  }

  public String generateRefreshToken(Date now, String uuid) {
    try {
      SignedJWT signedJWT = new SignedJWT(rsaHeaderRefresh, buildClaimsRefresh(now, uuid));
      signedJWT.sign(rsassaSignerRefresh);
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new RuntimeException();
    }
  }

  public JWTClaimsSet parseAndGetClaimsRefresh(String token) {
    try {
      var signedJWT = SignedJWT.parse(token);
      if (signedJWT.verify(rsassaVerifierRefresh)) {
        return signedJWT.getJWTClaimsSet();
      } else {
        throw new RuntimeException("Failed to verify token");
      }
    } catch (ParseException | JOSEException e) {
      throw new RuntimeException("Failed to verify token");
    }
  }

  public RSAKey generateRSAKey(String keyId, Integer keySize) throws JOSEException {
    return new RSAKeyGenerator(keySize)
      .keyID(keyId)
      .generate();
  }

  private JWKSet generateNewKeySet(RSAKey rsaKey) {
    return new JWKSet(rsaKey.toPublicJWK());
  }

  private JWSHeader generateRsaHeadersAccess(String keyId) {
    return new JWSHeader.Builder(JWSAlgorithm.RS256)
      .keyID(keyId)
      .type(new JOSEObjectType("acc+jwt"))
      .build();
  }

  private JWSHeader generateRsaHeadersRefresh(String keyId) {
    return new JWSHeader.Builder(JWSAlgorithm.RS256)
      .keyID(keyId)
      .type(new JOSEObjectType("ref+jwt"))
      .build();
  }

  private RSASSASigner generateRSASigner(RSAKey rsaKey) throws JOSEException {
    return new RSASSASigner(rsaKey);
  }

  private RSASSAVerifier generateRSAVerifier(RSAKey rsaKey) throws JOSEException {
    return new RSASSAVerifier(rsaKey);
  }

  public JWKSet getJWKSet() {
    return jwkSet;
  }

  private JWTClaimsSet buildClaimsAccess(Date now, String uuid) {
    return new JWTClaimsSet.Builder()
      .issuer("https://localhost:8081")
      .subject("client")
      .audience(List.of("https://localhost:8080"))
      .expirationTime(new Date(now.getTime() + 1000 * 60 * 10))
      .notBeforeTime(now)
      .issueTime(now)
      .claim("uid", uuid)
      .jwtID(UUID.randomUUID().toString())
      .build();
  }

  private JWTClaimsSet buildClaimsRefresh(Date now, String uuid) {
    return new JWTClaimsSet.Builder()
      .issuer("https://localhost:8081")
      .subject("client")
      .audience(List.of("https://localhost:8080"))
      .expirationTime(new Date(now.getTime() + 1000 * 60 * 100))
      .notBeforeTime(now)
      .issueTime(now)
      .claim("uid", uuid)
      .jwtID(UUID.randomUUID().toString())
      .build();
  }
}
