package com.xrtb.tools;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Networks {

    public static void main(String args[]) throws Exception {
        List<String> addresses = new ArrayList<>();
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            System.out.println( "'" + intf.getName() + "'");
        }
    }
}
