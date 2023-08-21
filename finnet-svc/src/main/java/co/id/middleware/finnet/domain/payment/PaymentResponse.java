package co.id.middleware.finnet.domain.payment;

import lombok.Getter;
import lombok.Setter;

/**
 * @author errykistiyanto@gmail.com 2020-03-06
 */
@Getter
@Setter
public class PaymentResponse {

    private String pan;
    private String amount;
    private String stan;
    private String retrievalReferenceNumber;
    private String transactionDateTime;
    private String expiredDate;
    private String settlementDate;
    private String channelCode;
    private String acquiringCode;
    private String forwardingCode;
    private String terminalId;
    private String clientId;
    private String locationName;
    private String privateData;
    private String screen;
    private String transactionYear;
    private String productCode;
    private String accountNumber;
    private String fee;
    private String destinationAccount;
    private String feeAccount;
    private String sourceAccount;

//    private String transactionType;

    private String productName;

}