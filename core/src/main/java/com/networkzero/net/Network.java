package com.networkzero.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Client;
import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Shared wire protocol and Kryo registration for both LAN peers. */
public final class Network {
    public static final int TCP_PORT = 54555, UDP_PORT = 54777;
    public static final String GAME_VERSION = "1";
    public static final Gson JSON = new Gson();
    private Network() {}

    public static void register(Kryo kryo) {
        kryo.register(Hello.class); kryo.register(Welcome.class); kryo.register(Reject.class);
        kryo.register(Intent.class); kryo.register(Intent.Type.class); kryo.register(Chat.class);
        kryo.register(Ping.class); kryo.register(Pong.class); kryo.register(StateSync.class);
        kryo.register(PresenceUpdate.class); kryo.register(ChatMessage.class); kryo.register(Notice.class);
        kryo.register(PartnerLeft.class); kryo.register(GameEvent.class); kryo.register(Discovery.class);
        kryo.register(DiscoveryReply.class); kryo.register(Ready.class); kryo.register(StartGame.class);
        kryo.register(StoryReady.class); kryo.register(RoomReady.class);
    }

    public static final class Hello { public String gameVersion, playerName; public Hello(){} public Hello(String v,String n){gameVersion=v;playerName=n;} }
    public static final class Welcome { public String hostName,stateJson; public Welcome(){} public Welcome(String h,String s){hostName=h;stateJson=s;} }
    public static final class Reject { public String reason; public Reject(){} public Reject(String r){reason=r;} }
    public static final class Intent {
        public enum Type { PICK_UP, OPEN_PANEL, CLOSE_PANEL, SWAP_WIRES, SUBMIT_CRIMP, PATCH_CLICK_PORT, PATCH_REMOVE_LINK, SUBMIT_PATCH, WHITEBOARD_PLACE, WHITEBOARD_RETURN, SUBMIT_WHITEBOARD, TERMINAL_COMMAND, KEYPAD_SUBMIT, USE_HINT, PAUSE, RESUME, UI_CLICK, DIAL_SET, DRAWER_OPEN, TAKE_KEYCARD, LOG_SUBMIT, REGISTER_PAIR, REGISTER_UNPAIR, REGISTER_CHECK, CCTV_FLAG, DOOR_CONFIRM }
        public Type type; public String a="",b="",panel=""; public int i,j; public float x,y;
        public Intent(){} public Intent(Type t){type=t;}
    }
    public static final class Chat { public String text; public Chat(){} public Chat(String t){text=t;} }
    public static final class Ping { public long sentAt; public Ping(){} public Ping(long t){sentAt=t;} }
    public static final class Pong { public long sentAt; public Pong(){} public Pong(long t){sentAt=t;} }
    public static final class StateSync { public String stateJson; public long sentAt; public StateSync(){} public StateSync(String s,long t){stateJson=s;sentAt=t;} }
    public static final class PresenceUpdate { public String activity; public PresenceUpdate(){} public PresenceUpdate(String a){activity=a;} }
    public static final class ChatMessage { public String sender,text; public long time; public boolean system; public ChatMessage(){} public ChatMessage(String n,String t,long when,boolean s){sender=n;text=t;time=when;system=s;} }
    public static final class Notice { public String text; public Notice(){} public Notice(String t){text=t;} }
    public static final class PartnerLeft { public String playerName; public PartnerLeft(){} public PartnerLeft(String n){playerName=n;} }
    public static final class GameEvent { public String name,payload; public GameEvent(){} public GameEvent(String n,String p){name=n;payload=p;} }
    public static final class Discovery { public String marker="NETWORK_ZERO"; public Discovery(){} }
    public static final class DiscoveryReply { public String hostName; public DiscoveryReply(){} public DiscoveryReply(String n){hostName=n;} }
    public static final class Ready { public boolean ready; public Ready(){} public Ready(boolean r){ready=r;} }
    public static final class StartGame { public StartGame(){} }
    public static final class StoryReady { public StoryReady(){} }
    public static final class RoomReady { public RoomReady(){} }

    public static Server newServer() { Server s = new Server(65536, 65536); register(s.getKryo()); return s; }
    public static Client newClient() { Client c = new Client(65536, 65536); register(c.getKryo()); return c; }

    /** Discovery uses KryoNet's UDP broadcast request/reply on UDP_PORT. */
    public static java.net.InetAddress discover(int timeoutMs) throws IOException {
        Client probe = newClient();
        try { probe.start(); return probe.discoverHost(UDP_PORT, timeoutMs); }
        finally { probe.stop(); }
    }
}
