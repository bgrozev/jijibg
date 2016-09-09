package org.jitsi.jijibg;

import org.ice4j.*;
import org.ice4j.ice.harvest.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Boris Grozev
 */
public class Jijibg
{
    private static int UDP_LISTEN_PORT = 10000;
    private static int TCP_LISTEN_PORT = 4443;
    private static int SERVER_PORT = 10000;

    private final TcpListenerImpl tcpListener;
    private final List<UdpListenerImpl> udpListeners = new LinkedList<>();

    private List<Session> sessions = new LinkedList<>();

    Jijibg()
        throws IOException
    {
        for (TransportAddress address : AbstractUdpListener
            .getAllowedAddresses(UDP_LISTEN_PORT))
        {
            udpListeners.add(new UdpListenerImpl(address));
            System.err.println("New UdpListenerImpl for " + address);
        }

        tcpListener = new TcpListenerImpl(TCP_LISTEN_PORT);
        System.err.println("New TcpListenerImpl for port " + TCP_LISTEN_PORT);
    }

    public void start()
    {}

    private Session findSession(InetSocketAddress clientAddress, String ufrag)
    {
        for (Session session : sessions)
        {
            if (session.serverUfrag == ufrag && clientAddress == session.clientAddress)
            {
                return session;
            }
        }

        return null;
    }

    private InetSocketAddress extractAddress(String ufrag)
    {
        // Naive encoding
        if (ufrag == null || ufrag.length() < 8)
            return null;

        byte[] addr = new byte[4];
        try
        {
            addr[0] = Byte.parseByte(ufrag.substring(0, 2), 16);
            addr[1] = Byte.parseByte(ufrag.substring(2, 4), 16);
            addr[2] = Byte.parseByte(ufrag.substring(4, 6), 16);
            addr[3] = Byte.parseByte(ufrag.substring(6, 8), 16);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Can't parse ufrag: " + ufrag);
            return null;
        }

        InetAddress serverAddress;
        try
        {
            serverAddress = InetAddress.getByAddress(addr);
        }
        catch (UnknownHostException uhe)
        {
            System.err.println("uhe...");
            return null;
        }

        InetSocketAddress serverSocketAddress
            = new InetSocketAddress(serverAddress, SERVER_PORT);
        if (!addressAllowed(serverSocketAddress))
        {
            System.err.println("Address not allowed: " + serverSocketAddress);
            return null;
        }

        return serverSocketAddress;
    }

    private boolean addressAllowed(InetSocketAddress address)
    {
        return true;
    }

    private class UdpListenerImpl
        extends AbstractUdpListener
    {
        /**
         * Initializes a new <tt>SinglePortUdpHarvester</tt> instance which is to
         * bind
         * on the specified local address.
         *
         * @param localAddress the address to bind to.
         * @throws IOException if initialization fails.
         */
        UdpListenerImpl(TransportAddress localAddress)
            throws IOException
        {
            super(localAddress);
        }

        @Override
        protected void maybeAcceptNewSession(Buffer buf,
                                             InetSocketAddress clientAddress,
                                             String ufrag)
        {
            InetSocketAddress serverAddress = extractAddress(ufrag);
            if (serverAddress == null)
            {
                System.err.println("Can't extract address from ufrag: " + ufrag);
                return;
            }

            Session session = findSession(clientAddress, ufrag);
            if (session != null)
            {
                System.err.println("Something fishy, a session already exists for "
                                       + clientAddress + " and " + ufrag);
            }

            // Register the socket with the AbstractUdpListener.
            MySocket socket;
            try
            {
                socket = addSocket(clientAddress);
            }
            catch (SocketException se)
            {
                System.err.println("Failed to add socket to UdpListener");
                se.printStackTrace();
                return;
            }

            // Push back the first packet.
            socket.addBuffer(buf);

            session = new Session();
            sessions.add(session);
            session.start();
        }
    }
}
