package co.id.middleware.finnet.domain.reversal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author errykistiyanto@gmail.com 2022-09-21
 */

@Getter
@Setter
@ToString
public class ReversalSuccess {

    private String responseCode;
    private String responseMessage;
    public ReversalResponse data;

}
