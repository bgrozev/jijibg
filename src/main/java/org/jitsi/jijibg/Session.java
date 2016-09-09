package org.jitsi.jijibg;

import org.ice4j.socket.*;

import java.net.*;

/**
 * Created by boris on 08/09/16.
 */
public class Session
{
    InetSocketAddress clientAddress;
    String serverUfrag;

    private IceSocketWrapper clientSocket;
    private IceSocketWrapper serverSocket;

    Thread clientReadThread;
    Thread serverReadThread;

    public Session()
    {

    }

    public void start()
    {

    }

    public void stop()
    {

    }
}
