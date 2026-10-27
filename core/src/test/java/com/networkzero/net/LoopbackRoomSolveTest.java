package com.networkzero.net;

import com.google.gson.Gson;
import com.networkzero.room.GameState;
import com.networkzero.session.IntentHandler;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LoopbackRoomSolveTest {
    @Test void twoPeersCompleteRoomThreeThroughHostValidatedIntents() throws Exception {
        GameState hostState=new GameState(),clientState=new GameState();hostState.beginRoom3();IntentHandler rules=new IntentHandler();CountDownLatch welcomed=new CountDownLatch(1);AtomicInteger syncs=new AtomicInteger();
        java.util.List<Network.GameEvent> hostEvents=new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.List<Network.GameEvent> clientEvents=new java.util.concurrent.CopyOnWriteArrayList<>();
        GameServer server=new GameServer("Host",()->hostState,new GameServer.Handler(){public void onConnected(String p){}public void onDisconnected(String p){}public void onMessage(Object message){if(message instanceof Network.Intent i){IntentHandler.Result r=rules.apply(hostState,i,"Client");if(!r.event().isEmpty()){Network.GameEvent ev=new Network.GameEvent(r.event(),"Client");serverRef[0].send(ev);hostEvents.add(ev);}serverRef[0].sync();}}});serverRef[0]=server;
        GameClient client=new GameClient(new GameClient.Handler(){public void onDisconnected(){}public void onMessage(Object m){if(m instanceof Network.Welcome)welcomed.countDown();if(m instanceof Network.StateSync s){clientState.copyFrom(new Gson().fromJson(s.stateJson,GameState.class));syncs.incrementAndGet();}if(m instanceof Network.GameEvent e)clientEvents.add(e);}});
        try {
            server.start();client.start();client.connect("127.0.0.1","Client",4000);assertTrue(welcomed.await(5,TimeUnit.SECONDS),"Room 3 handshake timed out");
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"cabinet"));
            for(int n=0;n<3;n++){Network.Intent dial=new Network.Intent(Network.Intent.Type.DIAL_SET);dial.i=n;dial.j=new int[]{3,1,7}[n];send(client,hostState,clientState,syncs,dial);}
            send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.DRAWER_OPEN));send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.TAKE_KEYCARD));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"cabinet"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"log_archive"));Network.Intent log=intent(Network.Intent.Type.LOG_SUBMIT,"10.20.30.77");log.i=7;send(client,hostState,clientState,syncs,log);send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"log_archive"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"service_register"));for(var pair:com.networkzero.puzzles.ServiceRegisterMatcher.ANSWER.entrySet()){Network.Intent p=intent(Network.Intent.Type.REGISTER_PAIR,pair.getKey());p.b=pair.getValue();send(client,hostState,clientState,syncs,p);}send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.REGISTER_CHECK));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"service_register"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"cctv_wall"));Network.Intent camera=new Network.Intent(Network.Intent.Type.CCTV_FLAG);camera.i=6;send(client,hostState,clientState,syncs,camera);send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"cctv_wall"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"terminal3"));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.TERMINAL_COMMAND,"block 10.20.30.77"));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.TERMINAL_COMMAND,"release"));
            Network.Intent hostConfirm=intent(Network.Intent.Type.DOOR_CONFIRM,"host");hostConfirm.i=1;int before=syncs.get();assertTrue(rules.apply(hostState,hostConfirm,"Host").accepted());server.sync();awaitSync(syncs,before);
            Network.Intent clientConfirm=intent(Network.Intent.Type.DOOR_CONFIRM,"client");clientConfirm.i=1;send(client,hostState,clientState,syncs,clientConfirm);send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"exit_keypad"));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.KEYPAD_SUBMIT,"6742"));
            assertTrue(hostEvents.stream().anyMatch(e->"EndingStart".equals(e.name)),"Host must receive EndingStart event");
            assertTrue(clientEvents.stream().anyMatch(e->"EndingStart".equals(e.name)),"Client must receive EndingStart event");
            assertTrue(hostState.room3Complete,"Host state must be complete");
            assertTrue(clientState.room3Complete,"Client state must be complete");
            assertSameState(hostState,clientState);

            java.nio.file.Path progressFile=com.networkzero.engine.ProfilePaths.saves().resolve("progress.json");
            java.nio.file.Files.deleteIfExists(progressFile);
            com.networkzero.engine.ProgressStore.writeComplete(progressFile,600,0,1000,2700-hostState.room3SecondsLeft,hostState.room3Hints,hostState.room3Score);
            String progressContent=java.nio.file.Files.readString(progressFile);
            int first=progressContent.indexOf("\"room\": \"complete\"");
            assertTrue(first>=0,"progress.json must contain \"room\": \"complete\"");
            int second=progressContent.indexOf("\"room\": \"complete\"",first+1);
            assertEquals(-1,second,"progress.json must contain \"room\": \"complete\" exactly once");
        } finally {client.close();server.close();}
    }
    @Test void twoPeersCompleteRoomOneFromHostState() throws Exception {
        GameState hostState=new GameState(),clientState=new GameState();IntentHandler rules=new IntentHandler();CountDownLatch welcomed=new CountDownLatch(1);AtomicInteger syncs=new AtomicInteger();
        GameServer server=new GameServer("Host",()->hostState,new GameServer.Handler(){public void onConnected(String p){}public void onDisconnected(String p){}public void onMessage(Object message){if(message instanceof Network.Intent i){rules.apply(hostState,i,"Client");serverRef[0].sync();}}});serverRef[0]=server;
        GameClient client=new GameClient(new GameClient.Handler(){public void onDisconnected(){}public void onMessage(Object m){if(m instanceof Network.Welcome)welcomed.countDown();if(m instanceof Network.StateSync s){clientState.copyFrom(new Gson().fromJson(s.stateJson,GameState.class));syncs.incrementAndGet();}}});
        try {
            server.start();client.start();client.connect("127.0.0.1","Client",4000);assertTrue(welcomed.await(5,TimeUnit.SECONDS),"handshake timed out");
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"crimp"));
            for(String id:new String[]{"crimper","rj45_box","cable_coil"})send(client,hostState,clientState,syncs,intent(Network.Intent.Type.PICK_UP,id));
            String[] target={"White-Orange","Orange","White-Green","Blue","White-Blue","Green","White-Brown","Brown"};
            for(int i=0;i<target.length;i++){int at=i;for(int j=i;j<target.length;j++)if(hostState.wires[j].equals(target[i])){at=j;break;}if(at!=i){Network.Intent swap=new Network.Intent(Network.Intent.Type.SWAP_WIRES);swap.i=i;swap.j=at;send(client,hostState,clientState,syncs,swap);}}
            send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.SUBMIT_CRIMP));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"crimp"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"patch"));int[][] links={{1,5},{2,3},{4,6},{7,8}};for(int[] l:links){Network.Intent add=new Network.Intent(Network.Intent.Type.PATCH_CLICK_PORT);add.i=l[0];add.j=l[1];send(client,hostState,clientState,syncs,add);}send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.SUBMIT_PATCH));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"patch"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"topology"));String[] devices={"Router","Switch","PC1","PC3"};for(int i=0;i<4;i++){Network.Intent place=intent(Network.Intent.Type.WHITEBOARD_PLACE,devices[i]);place.i=i;send(client,hostState,clientState,syncs,place);}send(client,hostState,clientState,syncs,new Network.Intent(Network.Intent.Type.SUBMIT_WHITEBOARD));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"topology"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"terminal"));for(String command:new String[]{"set ip 192.168.1.30","set mask 255.255.255.0","set gateway 192.168.1.1","ping 192.168.1.1"})send(client,hostState,clientState,syncs,intent(Network.Intent.Type.TERMINAL_COMMAND,command));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.CLOSE_PANEL,"terminal"));
            send(client,hostState,clientState,syncs,intent(Network.Intent.Type.OPEN_PANEL,"keypad"));send(client,hostState,clientState,syncs,intent(Network.Intent.Type.KEYPAD_SUBMIT,"2791"));assertTrue(hostState.roomComplete);assertEquals(hostState.secondsLeft,clientState.secondsLeft);assertEquals(hostState.score,clientState.score);assertEquals(hostState.hintsUsed,clientState.hintsUsed);assertEquals(hostState.inventory,clientState.inventory);assertArrayEquals(hostState.wires,clientState.wires);assertArrayEquals(hostState.board,clientState.board);assertEquals(hostState.routerOnline,clientState.routerOnline);assertEquals(hostState.pcSolved,clientState.pcSolved);assertEquals(hostState.roomComplete,clientState.roomComplete);assertEquals(new Gson().toJson(hostState),new Gson().toJson(clientState));
        } finally {client.close();server.close();}
    }
    private static final GameServer[] serverRef=new GameServer[1];
    private static Network.Intent intent(Network.Intent.Type t,String a){Network.Intent i=new Network.Intent(t);i.a=a;return i;}
    private static void send(GameClient client,GameState host,GameState replica,AtomicInteger syncs,Network.Intent in)throws Exception{int before=syncs.get();client.send(in);awaitSync(syncs,before);assertSameState(host,replica);}
    private static void assertSameState(GameState host,GameState replica){assertEquals(com.google.gson.JsonParser.parseString(new Gson().toJson(host)),com.google.gson.JsonParser.parseString(new Gson().toJson(replica)),"host/client state diverged");}
    private static void awaitSync(AtomicInteger syncs,int before)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(syncs.get()<=before&&System.nanoTime()<end)Thread.sleep(10);assertTrue(syncs.get()>before,"intent was not followed by StateSync");}
}
