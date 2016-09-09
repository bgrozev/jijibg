package org.jitsi.jijibg;

import org.ice4j.socket.*;

import java.io.*;
import java.net.*;

/**
 * @author Boris Grozev
 */
public class Session
{
    SocketAddress clientAddress;
    InetSocketAddress serverAddress;
    String serverUfrag;

    private IceSocketWrapper clientSocket;
    private IceSocketWrapper serverSocket;

    Thread clientReadThread;
    Thread serverReadThread;

    public Session(IceSocketWrapper clientSocket, SocketAddress clientAddress,
                   InetSocketAddress serverAddress)
    {
        this.clientSocket = clientSocket;
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;

        int localPort = PortManager.getInstance().getPort();
        try
        {
            serverSocket = new IceUdpSocketWrapper(new DatagramSocket(new InetSocketAddress(localPort)));
        }
        catch (SocketException se)
        {
            System.err.println("Failed to create server socket: "+ se);
        }

        clientReadThread = new Thread(){
            @Override
            public void run()
            {
                runInClientReadThread();
            }
        };
        clientReadThread.setDaemon(true);
        clientReadThread.setName("Client read thread " + clientAddress);

        serverReadThread = new Thread(){
            @Override
            public void run()
            {
                runInServerReadThread();
            }
        };
        serverReadThread.setDaemon(true);
        serverReadThread.setName(
            "Server read thread " + serverSocket.getLocalSocketAddress());

        System.err.println(
            "Created Session for client address " + clientAddress + "; local " +
                "server address " + serverSocket
                .getLocalSocketAddress() + "; server address" + serverAddress);
    }

    public void start()
    {
        clientReadThread.start();
        serverReadThread.start();
    }

    public void stop()
    {

    }

    private void runInClientReadThread()
    {
        DatagramPacket p = new DatagramPacket(new byte[1500], 0, 1500);
        while (true)
        {
            if (!readFromSocket(p, clientSocket, serverSocket, serverAddress))
            {
                System.err.println("Failed to read, closing "+Thread.currentThread().getName());
                break;
            }
        }
    }

    private void runInServerReadThread()
    {
        DatagramPacket p = new DatagramPacket(new byte[1500], 0, 1500);
        while (true)
        {
            if (!readFromSocket(p, serverSocket, clientSocket, clientAddress))
            {
                System.err.println("Failed to read, closing "+Thread.currentThread().getName());
                break;
            }

        }

    }

    private boolean readFromSocket(DatagramPacket p,
                                   IceSocketWrapper readSock,
                                   IceSocketWrapper writeSock,
                                   SocketAddress writeDest)
    {
        try
        {
            readSock.receive(p);
        }
        catch (IOException ioe)
        {
            System.err.println("Ops: "+ioe);
            return false;
        }

        p.setSocketAddress(writeDest);
        try
        {
            writeSock.send(p);
        }
        catch (IOException ioe)
        {
            System.err.println("Ops2: "+ioe);
            return false;
        }

        return true;
    }
}
