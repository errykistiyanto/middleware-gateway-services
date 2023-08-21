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

public class Reversal implements TransactionParticipant, Configurable {

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

            message.set(4, "000000036999");
            message.set(39, "00");
            message.set(61, "0081294689020100010130829732610000000036999");

            message.setResponseMTI();
            source.send(message);

        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}