package co.id.middleware.finnet.domain.inquiry;

import lombok.Getter;
import lombok.Setter;

/**
 * @author errykistiyanto@gmail.com 2020-03-06
 */
@Getter
@Setter
public class InquiryRequest {

    private String stan;
    private String retrievalReferenceNumber;
    private String transactionDateTime;
    private String transactionYear;
    private String amount;
    private String accountNumber;
    private String productCode;
    private String channelCode;
    private String clientId;
//    private String transactionType;
    private String productName;

}