package co.id.middleware.mock.jpos;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;

/**
 * @author errykistiyanto@gmail.com 2021-06-19
 */
public class RequestListener implements ISORequestListener, Configurable {
    private Configuration cfg;
    private String space;
    private String isorequest;
    private String isosource;
    private String queue;
    private Long timeout;
    private LocalSpace sp;

    @Override
    public boolean process(ISOSource isoSource, ISOMsg isoMsg) {
        Context ctx = new Context();
        ctx.put(this.isosource, isoSource);
        ctx.put(this.isorequest, isoMsg);
        ctx.put("ipaddress", ((BaseChannel)isoSource).getSocket().getInetAddress().getHostAddress());
        sp.out (this.queue, ctx, this.timeout);
        return true;
    }

    @Override
    public void setConfiguration(Configuration configuration) throws ConfigurationException {
        this.cfg = configuration;
        this.space = this.cfg.get("space");
        this.isorequest = this.cfg.get("isorequest");
        this.isosource = this.cfg.get("isosource");
        this.queue = this.cfg.get("queue");
        this.timeout = this.cfg.getLong("timeout");
        sp = (LocalSpace) SpaceFactory.getSpace(this.space);
    }
}