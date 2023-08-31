package co.id.middleware.mock.iso;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author errykistiyanto@gmail.com 2021-06-19
 */

public class Inquiry implements TransactionParticipant, Configurable {

    private Configuration cfg;
    private String isorequest;
    private String isosource;
    private String logger;
    private Long timeout;
    private String mux;


    @Override
    public void setConfiguration(Configuration configuration) throws ConfigurationException {
        this.cfg = configuration;
        this.isorequest = cfg.get("isorequest");
        this.isosource = cfg.get("isosource");
        this.logger = cfg.get("logger");
        this.timeout = cfg.getLong("timeout", 10000);
        this.mux = cfg.get("mux");

    }

    @Override
    public int prepare(long id, Serializable context) {
        return PREPARED | READONLY;
    }


    @Override
    public void commit(long id, Serializable context) {
        ISOMsg message = (ISOMsg) ((Context) context).get(this.isorequest);
        ISOSource source = (ISOSource) ((Context) context).get(this.isosource);

        Space psp = SpaceFactory.getSpace("jdbm:myspace");

        final String TRACE = "JPTS_TRACE";
        long traceNumber = SpaceUtil.nextLong(psp, TRACE) % 100000;

        try {

            switch (message.getString(103).trim()){

                case "017001":
//                    Postpaid XL
                    message.set(4, "000000039388");
                    message.set(39, "00");
                    message.set(61, "00812823879341000601501928253       000000176157ANxxxxxxxxxxxNI               ");
                    break;

                case "019004":
//                    Postpaid Smartfren
                    message.set(4, "000000100909");
                    message.set(39, "00");
                    message.set(61, "0088905657655190004156100925551     000000100909ARI FIRMANSYAH                          20230821");
                    break;

                case "013000":
//                    Postpaid Three
                    message.set(4, "000000085101");
                    message.set(39, "00");
                    message.set(61, "00000000008988730292200004MUHAMMAD ROMADHONI                                29120022062037                          000000000020220000000851011DueDate             000020221101Date                202212290620Merchant            000000006016                    000000000000GetSubBillInfoxxxxxxfnt013");
                    break;


                default:
//                    Postpaid Telkomsel Halo
                    message.set(4, "000000229270");
                    message.set(39, "00");
                    message.set(61, "0081282387934100020130838548715000000229270SUXONO                                                      ");
                    break;
            }

            message.setResponseMTI();
            source.send(message);

        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}