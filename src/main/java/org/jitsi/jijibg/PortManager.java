package org.jitsi.jijibg;

import java.util.*;

/**
 * @author Boris Grozev
 */
public class PortManager
{
    private static int MIN_PORT = 10001;
    private static int MAX_PORT = 20000;

    private static PortManager instance = new PortManager();
    public static PortManager getInstance()
    {
        return instance;
    }

    private final LinkedList<Integer> freePorts;

    private PortManager()
    {
        freePorts = new LinkedList<>();
        for (int i = MIN_PORT; i <= MAX_PORT; i++)
            freePorts.add(i);
    }

    public void returnPort(int port)
    {
        synchronized (freePorts)
        {
            freePorts.add(port);
        }
    }

    public int getPort()
    {
        synchronized (freePorts)
        {
            if (freePorts.isEmpty())
                throw new RuntimeException("No more free ports.");

            return freePorts.remove(0);
        }
    }
}
