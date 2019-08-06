package org.jitsi.jijibg;

import org.ice4j.*;
import org.ice4j.ice.harvest.*;
import org.ice4j.socket.*;

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
    // The port on which jitsi-videobridge listens
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

    private Session findSession(SocketAddress clientAddress, String ufrag)
    {
        /*
        for (Session session : sessions)
        {
            if (session.serverUfrag.equals(ufrag) && clientAddress.equals(session.clientAddress))
            {
                return session;
            }
        }

        */
        return null;
    }

    private InetSocketAddress extractAddress(String ufrag)
    {
        // Naive encoding
        if (ufrag == null || ufrag.length() < 8)
            return null;

        int[] addr = new int[4];
        try
        {
            addr[0] = Integer.parseInt(ufrag.substring(0, 2), 16);
            addr[1] = Integer.parseInt(ufrag.substring(2, 4), 16);
            addr[2] = Integer.parseInt(ufrag.substring(4, 6), 16);
            addr[3] = Integer.parseInt(ufrag.substring(6, 8), 16);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Can't parse ufrag: " + ufrag + ". Msg: " + nfe.getMessage());
            nfe.printStackTrace();
            return null;
        }
        byte[] addr2 = new byte[4];
        addr2[0] = (byte) addr[0];
        addr2[1] = (byte) addr[1];
        addr2[2] = (byte) addr[2];
        addr2[3] = (byte) addr[3];

        InetAddress serverAddress;
        try
        {
            serverAddress = InetAddress.getByAddress(addr2);
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
            System.err.println("Extracted address: " + serverAddress);

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

            session = new Session(new IceUdpSocketWrapper(socket), clientAddress, serverAddress);
            sessions.add(session);
            session.start();
        }
    }

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
            InetSocketAddress serverAddress = extractAddress(ufrag);
            if (serverAddress == null)
            {
                System.err.println("Can't extract address from ufrag: " + ufrag);
                return;
            }
            System.err.println("Extracted address: " + serverAddress);

            SocketAddress clientAddress = socket.getRemoteSocketAddress();
            Session session = findSession(clientAddress, ufrag);
            if (session != null)
            {
                System.err.println("Something fishy, a session already exists for "
                                       + clientAddress + " and " + ufrag);
            }

            PushBackIceSocketWrapper wrapper
                = new PushBackIceSocketWrapper(new IceTcpSocketWrapper(socket), pushback);

            session = new Session(wrapper, clientAddress, serverAddress);
            sessions.add(session);
            session.start();
        }
    }
}
