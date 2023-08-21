package co.id.middleware.mock.channel;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by bankdki on 8/2/17.
 */
public class FinnetChannel extends BaseChannel {

    public FinnetChannel() {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public FinnetChannel(String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException on error
     * @see ISOPackager
     */
    public FinnetChannel(ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException on error
     * @see ISOPackager
     */
    public FinnetChannel(ISOPackager p, ServerSocket serverSocket)
            throws IOException
    {
        super(p, serverSocket);
    }


    protected void sendMessageLength(int len) throws IOException {
        serverOut.write(len);
        serverOut.write(len >> 8);
    }

    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[2];
        serverIn.readFully(b,0,2);
        return (((int)b[0])&0xFF) | ((((int)b[1])&0xFF) << 8);
    }
}
