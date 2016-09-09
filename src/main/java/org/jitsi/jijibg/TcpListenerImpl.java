package org.jitsi.jijibg;

import org.ice4j.*;
import org.ice4j.ice.harvest.*;

import java.io.*;
import java.net.*;

/**
 * @author Boris Grozev
 */
public class TcpListenerImpl
    extends AbstractTcpListener
{
    TcpListenerImpl(int port)
        throws IOException
    {
        super(port);
    }

    @Override
    protected void acceptSession(Socket socket, String ufrag,
                                 DatagramPacket pushback)
        throws IOException, IllegalStateException
    {

    }
}
