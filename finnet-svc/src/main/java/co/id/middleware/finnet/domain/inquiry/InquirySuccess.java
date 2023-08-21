package co.id.middleware.finnet.domain.inquiry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author errykistiyanto@gmail.com 2022-09-21
 */

@Getter
@Setter
@ToString
public class InquirySuccess {

    private String responseCode;
    private String responseMessage;
    public InquiryResponse data;

}
