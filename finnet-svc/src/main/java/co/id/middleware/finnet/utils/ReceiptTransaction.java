package co.id.middleware.finnet.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


/**
 * @author errykistiyanto@gmail.com 2023-08-02
 */

@Slf4j
@Component
public class ReceiptTransaction {

    public static final String delimeter = " : ";
    public static final String newline = "|";

    public static String receiptHeader(String header) {

        StringBuffer map = new StringBuffer();

        if (header.length() > 50) {
            header = header.substring(0, 50);
        }

        map.append(StringUtils.rightPad(header, 50, " "));
        map.append(newline);
        map.append(newline);

        return map.toString();
    }

    public static String receiptInfo(String key, String value) {

        if (key.length() > 17) {
            key = key.substring(0, 17);
        }

        if (value.length() > 30) {
            value = value.substring(0, 30);
        }

        StringBuffer map = new StringBuffer();
        map.append(StringUtils.rightPad(key, 17, " ") + delimeter + StringUtils.rightPad(value, 30, " "));
        map.append(newline);

        return map.toString();
    }

    public static String receiptFooter(String footer) {

        if (footer.length() > 50) {
            footer = footer.substring(0, 50);
        }

        StringBuffer map = new StringBuffer();
        map.append(newline);
        map.append(StringUtils.center(footer, 50, " "));

        return map.toString();
    }

    public static String noReceiptFooter() {

        StringBuffer map = new StringBuffer();
        map.append(newline);

        return map.toString();
    }

}
