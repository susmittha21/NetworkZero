package com.networkzero.net;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.badlogic.gdx.Gdx;
import com.networkzero.room.GameState;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicLong;

/** One-client host transport. All callbacks are marshalled onto LibGDX's render thread. */
public final class GameServer implements AutoCloseable {
    public interface Handler { void onConnected(String player); void onMessage(Object message); void onDisconnected(String player); }
    private final Server server=Network.newServer();
    private final String hostName;
    private final int tcpPort,udpPort;
    private final Supplier<GameState> state;
    private final Handler handler;
    private volatile Connection peer;
    private volatile String peerName="Player 2";
    private volatile boolean welcomed;
    private final AtomicLong messageCount=new AtomicLong();
    private final AtomicLong pingMs=new AtomicLong();
    private long lastSync;

    public GameServer(String hostName,Supplier<GameState> state,Handler handler){this(hostName,Network.TCP_PORT,Network.UDP_PORT,state,handler);}
    public GameServer(String hostName,int tcpPort,int udpPort,Supplier<GameState> state,Handler handler){this.hostName=hostName;this.tcpPort=tcpPort;this.udpPort=udpPort;this.state=state;this.handler=handler;}
    public synchronized void start() throws IOException {
        server.addListener(new Listener(){
            @Override public void connected(Connection c){ synchronized(GameServer.this){if(peer!=null){c.sendTCP(new Network.Reject("Game is full"));c.close();return;}peer=c;} }
            @Override public void received(Connection c,Object message){
                messageCount.incrementAndGet();
                if(message instanceof Network.Hello hello){
                    if(!Network.GAME_VERSION.equals(hello.gameVersion)){c.sendTCP(new Network.Reject("Version mismatch"));c.close();return;}
                    post(()->{synchronized(GameServer.this){if(c!=peer){c.sendTCP(new Network.Reject("Game is full"));c.close();return;}peerName=safeName(hello.playerName,"Player 2");welcomed=true;}GameState snapshot=state.get();c.sendTCP(new Network.Welcome(hostName,Network.JSON.toJson(snapshot)));handler.onConnected(peerName);});return;
                }
                if(c!=peer||!welcomed)return;
                if(message instanceof Network.Ping ping){pingMs.set(Math.max(0,System.currentTimeMillis()-ping.sentAt));c.sendTCP(new Network.Pong(ping.sentAt));}
                else post(()->handler.onMessage(message));
            }
            @Override public void disconnected(Connection c){String name; synchronized(GameServer.this){if(c!=peer)return;name=peerName;peer=null;welcomed=false;}post(()->handler.onDisconnected(name));}
        });
        server.start();server.bind(tcpPort,udpPort);
    }
    private static String safeName(String n,String fallback){if(n==null)return fallback;String s=n.replaceAll("[\\p{Cntrl}]","").trim();if(s.isEmpty())return fallback;return s.substring(0,Math.min(16,s.length()));}
    private static void post(Runnable r){if(Gdx.app!=null)Gdx.app.postRunnable(r);else r.run();}
    public synchronized boolean connected(){return peer!=null&&welcomed;}
    public String partnerName(){return peerName;}
    public void send(Object message){Connection c=peer;if(c!=null&&welcomed)c.sendTCP(message);}
    public void sync(){GameState s=state.get();if(s!=null)send(new Network.StateSync(Network.JSON.toJson(s),System.currentTimeMillis()));}
    public void closePeer(){Connection c=peer;if(c!=null)c.close();}
    public long messageCount(){return messageCount.get();}
    public long pingMs(){return pingMs.get();}
    @Override public void close(){closePeer();server.stop();}
}
