package com.networkzero.session;

import com.networkzero.net.*;
import com.networkzero.room.GameState;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.function.Consumer;

/** Client replica: submits intents and only changes shared state on host snapshots. */
public final class RemoteSession implements GameSession {
    private final GameState state=new GameState();private final String name;private final GameClient client;private volatile Consumer<Object> listener;private volatile String host="Host",activity="in the room";private volatile boolean welcomed,connected;private volatile long lastSync;
    public RemoteSession(String name,Consumer<Object> listener){this.name=name;this.listener=listener;client=new GameClient(new GameClient.Handler(){public void onMessage(Object m){message(m);}public void onDisconnected(){connected=false;if(listener!=null)listener.accept("HOST_LEFT");}});}
    public void connect(String address,int timeoutMs)throws IOException,InterruptedException{try{client.start();client.connect(address,name,timeoutMs);connected=true;}catch(IOException|InterruptedException|RuntimeException e){connected=false;client.close();throw e;}}
    private void message(Object m){if(m instanceof Network.Welcome w){host=w.hostName;state.copyFrom(new Gson().fromJson(w.stateJson,GameState.class));welcomed=true;}else if(m instanceof Network.StateSync s){state.copyFrom(new Gson().fromJson(s.stateJson,GameState.class));lastSync=System.currentTimeMillis();}else if(m instanceof Network.PresenceUpdate p)activity=p.activity;else if(m instanceof Network.PartnerLeft)connected=false;if(listener!=null)listener.accept(m);}
    public GameState state(){return state;}public void submit(Network.Intent i){client.send(i);}public boolean isRemote(){return true;}public boolean isHost(){return false;}public String localName(){return name;}public String partnerName(){return host;}public boolean partnerConnected(){return connected&&client.connected();}public String partnerActivity(){return activity;}public void publish(){}public boolean welcomed(){return welcomed;}public long lastSync(){return lastSync;}public long messageCount(){return client.messageCount();}public void send(Object m){client.send(m);}public void sendChat(Network.Chat c){client.send(c);}public void sendPresence(String a){activity=a;client.send(new Network.PresenceUpdate(a));}public long pingMs(){return client.pingMs();}public void tick(){client.ping();}public void setNetworkListener(Consumer<Object> c){listener=c;}public void close(){client.close();}
}
