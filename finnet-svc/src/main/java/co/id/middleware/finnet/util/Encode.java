package co.id.middleware.finnet.util;

import java.util.Base64;

/**
 * @author errykistiyanto@gmail.com 2023-01-24
 */
public class Encode {

    public static String encode(byte[] bytes) {

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
