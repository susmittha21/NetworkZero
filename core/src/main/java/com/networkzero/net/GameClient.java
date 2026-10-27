package com.networkzero.net;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.badlogic.gdx.Gdx;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Client transport. Incoming objects are never delivered off the render thread. */
public final class GameClient implements AutoCloseable {
    public interface Handler { void onMessage(Object message); void onDisconnected(); }
    private final Client client=Network.newClient();
    private final Handler handler;
    private final AtomicBoolean closed=new AtomicBoolean();
    private volatile String rejection="";
    private volatile long lastPong;
    private final AtomicLong messageCount=new AtomicLong();private volatile long roundTrip;
    private long lastPing;

    public GameClient(Handler handler){this.handler=handler;client.addListener(new Listener(){
        @Override public void received(com.esotericsoftware.kryonet.Connection c,Object object){
            messageCount.incrementAndGet();
            if(object instanceof Network.Reject r){rejection=r.reason;post(()->handler.onMessage(r));return;}
            if(object instanceof Network.Pong p){lastPong=System.currentTimeMillis();roundTrip=Math.max(0,lastPong-p.sentAt);post(()->handler.onMessage(p));return;}
            post(()->handler.onMessage(object));
        }
        @Override public void disconnected(com.esotericsoftware.kryonet.Connection c){if(!closed.get())post(handler::onDisconnected);}
    });}
    private static void post(Runnable r){if(Gdx.app!=null)Gdx.app.postRunnable(r);else r.run();}
    public void start(){client.start();client.setTimeout(15000);}
    public void connect(String host,String name,int timeoutMs)throws IOException,InterruptedException{
        client.connect(timeoutMs,host,Network.TCP_PORT,Network.UDP_PORT);client.setKeepAliveTCP(2000);client.setKeepAliveUDP(1000);
        client.sendTCP(new Network.Hello(Network.GAME_VERSION,name));
    }
    public boolean connected(){return client.isConnected();}
    public void send(Object message){if(client.isConnected())client.sendTCP(message);}
    public String rejection(){return rejection;}
    public long pingMs(){return lastPong==0?0:roundTrip;}
    public long messageCount(){return messageCount.get();}
    public void ping(){long now=System.currentTimeMillis();if(now-lastPing>1000){lastPing=now;send(new Network.Ping(now));}}
    public InetAddress discover(int timeoutMs)throws IOException{return client.discoverHost(Network.UDP_PORT,timeoutMs);}
    @Override public void close(){if(closed.compareAndSet(false,true))client.stop();}
}
