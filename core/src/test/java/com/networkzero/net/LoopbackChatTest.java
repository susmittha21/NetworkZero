package com.networkzero.net;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LoopbackChatTest {
    @Test void relaysChatBetweenLanPeers() throws Exception {
        CountDownLatch clientReceived = new CountDownLatch(1);
        CountDownLatch hostReceived = new CountDownLatch(1);
        AtomicReference<String> clientText = new AtomicReference<>();
        AtomicReference<String> hostText = new AtomicReference<>();
        LoopbackPorts ports = LoopbackPorts.findAvailable();
        GameServer[] serverRef = new GameServer[1];
        GameServer server = new GameServer("Host", ports.tcp(), ports.udp(), com.networkzero.room.GameState::new, new GameServer.Handler() {
            public void onConnected(String player) {}
            public void onDisconnected(String player) {}
            public void onMessage(Object message) {
                if (message instanceof Network.Chat chat) {
                    hostText.set(chat.text);
                    hostReceived.countDown();
                    serverRef[0].send(new Network.ChatMessage("Client", chat.text, System.currentTimeMillis(), false));
                }
            }
        });
        serverRef[0] = server;
        GameClient client = new GameClient(new GameClient.Handler() {
            public void onDisconnected() {}
            public void onMessage(Object message) {
                if (message instanceof Network.ChatMessage chat) {
                    clientText.set(chat.text);
                    clientReceived.countDown();
                }
            }
        });
        try {
            server.start();
            client.start();
            client.connect("127.0.0.1:"+ports.tcp(), ports.udp(), "Client", 4000);
            client.send(new Network.Chat("LAN chat check"));
            assertTrue(hostReceived.await(5, TimeUnit.SECONDS), "Host did not receive client chat");
            assertTrue(clientReceived.await(5, TimeUnit.SECONDS), "Client did not receive relayed chat");
            assertEquals("LAN chat check", hostText.get());
            assertEquals("LAN chat check", clientText.get());
        } finally {
            client.close();
            server.close();
        }
    }
}
