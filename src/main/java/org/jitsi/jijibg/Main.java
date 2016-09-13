package org.jitsi.jijibg;

/**
 * @author Boris Grozev
 */
public class Main
{
    public static void main(String[] args)
        throws Exception
    {
        System.err.println("Jijibg running");
        Jijibg jijibg = new Jijibg();
        jijibg.start();

        try
        {
            Thread.sleep(3600 * 100000);
        }
        catch (Exception e)
        {}
        System.err.println("Jijibg over and out.");
    }
}
