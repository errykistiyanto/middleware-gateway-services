package co.id.middleware.finnet.domain.payment;

import lombok.Getter;
import lombok.Setter;

/**
 * @author errykistiyanto@gmail.com 2020-03-06
 */
@Getter
@Setter
public class PaymentRequest {

    private String stan;
    private String retrievalReferenceNumber;
    private String transactionDateTime;
    private String transactionYear;
    private String amount;
    private String accountNumber;
    private String productCode;
    private String channelCode;
    private String clientId;
    private String privateData;
    private String fee;
//    private String transactionType;
    private String productName;

    private String destinationAccount;
    private String feeAccount;
    private String sourceAccount;

}