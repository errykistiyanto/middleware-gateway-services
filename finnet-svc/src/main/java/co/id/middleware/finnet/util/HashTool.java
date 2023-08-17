package co.id.middleware.finnet.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static co.id.middleware.finnet.util.Encode.encode;

/**
 * The type Hash tool.
 */
public class HashTool {

    /**
     * Sha 256 base 64 string.
     *
     * @param myBodyJson the my body json
     * @return the string
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static String sha256Base64(String myBodyJson) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(myBodyJson.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Hmac sha 256 string.
     *
     * @param component the component
     * @param secret    the secret
     * @return the string
     * @throws InvalidKeyException      the invalid key exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static String hmacSHA256(String component, String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] decodedKey = secret.getBytes();
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(originalKey);
        hmacSha256.update(component.getBytes());
        byte[] HmacSha256DigestBytes = hmacSha256.doFinal();
        return Base64.getEncoder().encodeToString(HmacSha256DigestBytes);
    }

    public static String newHmacSha256(String data, String secret) {
        try {

            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
//            Logger.getLogger(JWebToken.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }
}
