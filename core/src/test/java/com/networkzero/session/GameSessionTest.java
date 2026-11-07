package com.networkzero.session;

import com.networkzero.net.Network;
import com.networkzero.net.GameClient;
import com.networkzero.room.GameState;
import org.junit.jupiter.api.Test;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    @Test void hostIntentMutatesStateOnce(){GameState state=new GameState();IntentHandler h=new IntentHandler();assertTrue(h.apply(state,intent(Network.Intent.Type.PICK_UP,"crimper"),"Host").accepted());assertEquals(java.util.List.of("Crimping Tool"),state.inventory);assertFalse(h.apply(state,intent(Network.Intent.Type.PICK_UP,"crimper"),"Host").accepted());}
    @Test void puzzleLocksAreExclusiveAndReleased(){GameState s=new GameState();IntentHandler h=new IntentHandler();assertTrue(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Alice").accepted());assertFalse(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Bob").accepted());assertTrue(h.apply(s,intent(Network.Intent.Type.CLOSE_PANEL,"crimp"),"Alice").accepted());assertTrue(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Bob").accepted());hLocksRelease(s,"Bob");assertTrue(s.lockOwner.isEmpty());}
    @Test void asymmetricCluesAreRoleSpecificAndCanBeDisabled(){assertTrue(CoopRules.canOpen(true,"logbook"));assertFalse(CoopRules.canOpen(false,"logbook"));assertFalse(CoopRules.canOpen(true,"notice_board"));assertTrue(CoopRules.canOpen(false,"notice_board"));assertTrue(CoopRules.canOpen(false,"pc"));}
    @Test void playerActivityIsRelayedToBothChatTranscripts() throws Exception {
        int tcpPort,udpPort;
        try(ServerSocket tcp=new ServerSocket(0);DatagramSocket udp=new DatagramSocket(0)){tcpPort=tcp.getLocalPort();udpPort=udp.getLocalPort();}
        HostSession host=new HostSession(new GameState(),"Host",null,tcpPort,udpPort);
        CountDownLatch hostSawClientActivity=new CountDownLatch(1),clientSawClientActivity=new CountDownLatch(1),clientSawHostActivity=new CountDownLatch(1);
        AtomicReference<String> hostEntry=new AtomicReference<>();
        host.setNetworkListener(message->{if(message instanceof Network.ChatMessage chat&&chat.text.contains("viewing PATCH PANEL")){hostEntry.set(chat.text);hostSawClientActivity.countDown();}});
        GameClient client=new GameClient(new GameClient.Handler(){public void onDisconnected(){}public void onMessage(Object message){if(message instanceof Network.ChatMessage chat){if(chat.text.contains("Client: viewing PATCH PANEL"))clientSawClientActivity.countDown();if(chat.text.contains("Host: viewing CONTROL TERMINAL"))clientSawHostActivity.countDown();}}});
        try {
            client.start();
            client.connect("127.0.0.1:"+tcpPort,udpPort,"Client",4000);
            client.send(new Network.PresenceUpdate("viewing PATCH PANEL"));
            assertTrue(hostSawClientActivity.await(5,TimeUnit.SECONDS));
            assertTrue(clientSawClientActivity.await(5,TimeUnit.SECONDS));
            assertEquals("Client: viewing PATCH PANEL",hostEntry.get());
            host.sendPresence("viewing CONTROL TERMINAL");
            assertTrue(clientSawHostActivity.await(5,TimeUnit.SECONDS));
        } finally {
            client.close();
            host.close();
        }
    }
    private static void hLocksRelease(GameState s,String owner){new LockManager().releaseAll(s,owner);}
    private static Network.Intent intent(Network.Intent.Type type,String a){Network.Intent i=new Network.Intent(type);i.a=a;return i;}
}
