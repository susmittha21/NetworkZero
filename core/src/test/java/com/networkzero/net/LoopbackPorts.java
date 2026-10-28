package com.networkzero.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

record LoopbackPorts(int tcp, int udp) {
    static LoopbackPorts findAvailable() throws IOException {
        try (ServerSocket tcpSocket = new ServerSocket(0);
             DatagramSocket udpSocket = new DatagramSocket(0)) {
            return new LoopbackPorts(tcpSocket.getLocalPort(), udpSocket.getLocalPort());
        }
    }
}
