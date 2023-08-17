package co.id.middleware.finnet.domain.inquiry;

import lombok.Getter;
import lombok.Setter;

/**
 * @author errykistiyanto@gmail.com 2020-03-06
 */
@Getter
@Setter
public class InquiryResponse {

    private String stan;
    private String amount;
    private String transactionDateTime;
    private String transactionYear;
    private String retrievalReferenceNumber;
    private String screen;
    private String fee;
    private String productCode;
    private String accountNumber;
    private String channelCode;
    private String clientId;
    private String productName;
//    private String transactionType;
    private String privateData;
//    private String reserveData;
    private String destinationAccount;
    private String feeAccount;

}