package com.networkzero.session;

import com.networkzero.net.Network;
import com.networkzero.room.GameState;

/** RoomScreen depends on this interface so solo and LAN share one view. */
public interface GameSession extends AutoCloseable {
    GameState state();
    void submit(Network.Intent intent);
    boolean isRemote();
    boolean isHost();
    String localName();
    String partnerName();
    boolean partnerConnected();
    String partnerActivity();
    void publish();
    default void sendChat(Network.Chat chat){}
    default void send(Object message){}
    default void sendPresence(String activity){}
    default long pingMs(){return 0;}
    default void setNetworkListener(java.util.function.Consumer<Object> listener){}
    @Override void close();
}
