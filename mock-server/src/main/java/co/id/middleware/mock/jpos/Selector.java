package co.id.middleware.mock.jpos;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import org.jpos.util.Log;

import java.io.Serializable;

/**
 * @author errykistiyanto@gmail.com 2021-06-19
 */
public class Selector implements GroupSelector, Configurable {
    private Configuration cfg;
    private String isorequest;
    private String logger;

    @Override
    public void setConfiguration(Configuration configuration) throws ConfigurationException {
        this.cfg = configuration;
        this.isorequest = configuration.get("isorequest");
        this.logger = configuration.get("logger");
    }

    @Override
    public String select(long l, Serializable serializable) {
        String group = "Unknown";
        ISOMsg m = (ISOMsg) ((Context) serializable).get(this.isorequest);
        try {
            switch(m.getMTI().toString()) {
                case "0800":
                    group = cfg.get (m.getMTI()+m.getString(70).trim());
                    break;
                case "0200":
                    group = cfg.get (m.getMTI()+m.getString(3));
                    break;
                case "0400":
                    group = cfg.get (m.getMTI()+m.getString(3));
                    break;
                case "0420":
                    group = cfg.get (m.getMTI()+m.getString(3));
                    break;
                case "0421":
                    group = cfg.get (m.getMTI()+m.getString(3));
                    break;
                default:
                    group = cfg.get (m.getMTI());
                    break;
            }
            Log.getLog("Q2","Q2").info("group "+group);
        } catch (Exception e) {
            StringBuffer ste = new StringBuffer();
            ste.append(e.getMessage());
            for(StackTraceElement element : e.getStackTrace()) {
                ste.append(element.toString()+"\n");
            }
            Log.getLog(this.logger, this.logger).error(ste.toString());
            ste = null;
        }

        return group;
    }

    @Override
    public int prepare(long l, Serializable serializable) {
        return PREPARED | READONLY;
    }

    @Override
    public void commit(long l, Serializable serializable) {

    }

    @Override
    public void abort(long l, Serializable serializable) {

    }
}