package com.networkzero.puzzles;

import com.networkzero.room.GameState;
import com.networkzero.net.Network;
import com.networkzero.session.IntentHandler;
import com.networkzero.session.CoopRules;
import com.networkzero.engine.ProgressStore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Room2LogicTest {
    @Test void finalProgressFileMarksTheGameComplete(@TempDir Path temp) throws Exception {
        Path progress=temp.resolve("saves").resolve("progress.json");ProgressStore.writeComplete(progress,900,1,740,1200,0,1000);
        JsonObject saved=JsonParser.parseString(Files.readString(progress)).getAsJsonObject();
        assertEquals("complete",saved.get("room").getAsString());assertTrue(saved.get("storySeen").getAsBoolean());
        assertEquals(900,saved.getAsJsonObject("room1").get("time").getAsInt());
        assertEquals(1200,saved.getAsJsonObject("room2").get("time").getAsInt());
        assertFalse(saved.has("room3"));
    }
    @Test void previousRoomNamesLoadIntoTheRoom2State() {
        GameState saved=new Gson().fromJson("{\"roomNumber\":3,\"room3SecondsLeft\":1200,\"room3Complete\":true,\"hintTiers\":{\"r3_P1\":2}}",GameState.class);
        saved.migrateLegacyRoom2State();
        assertEquals(2,saved.roomNumber);
        assertEquals(1200,saved.room2SecondsLeft);
        assertTrue(saved.room2Complete);
        assertEquals(2,saved.hintTiers.get("r2_P1"));
        assertFalse(new Gson().toJson(saved).contains("room3"));
    }
    @Test void packetLogChecksIpAndTrimmedDistinctPortCount() {
        assertTrue(LogChecker.check(" 10.20.30.77 ", " 7 ").correct());
        assertFalse(LogChecker.check("10.20.30.12", "7").correctIp());
        assertFalse(LogChecker.check("10.20.30.77", "6").correctCount());
        assertFalse(LogChecker.check("10.20.30.77", "seven").correctCount());
    }

    @Test void registerAndCameraOnlyAcceptExpectedAnswer() {
        assertEquals(6, ServiceRegisterMatcher.countCorrect(ServiceRegisterMatcher.ANSWER));
        assertTrue(ServiceRegisterMatcher.complete(ServiceRegisterMatcher.ANSWER));
        assertFalse(ServiceRegisterMatcher.correct("SSH", "21"));
        for (int camera=1; camera<=9; camera++) assertEquals(camera==6,CctvChecker.isTampered(camera));
        assertFalse(CctvChecker.isValidCamera(0));
        assertFalse(CctvChecker.isValidCamera(10));
    }

    @Test void terminalValidatesEveryBlockAndReleaseGate() {
        assertEquals("Invalid address.", TerminalInterpreter2.execute("block 999.1.1.1",false,false,false,false,false).output());
        assertEquals("Evidence incomplete. Analyse the packet log first.", TerminalInterpreter2.execute("block 10.20.30.77",false,false,false,false,false).output());
        assertEquals("No malicious traffic from that address. Block rejected.", TerminalInterpreter2.execute("block 10.20.30.12",true,false,false,false,false).output());
        assertTrue(TerminalInterpreter2.execute("block 10.20.30.77",true,false,false,false,false).attackerBlocked());
        assertEquals("Cannot release: threat still active.", TerminalInterpreter2.execute("release",false,false,false,false,false).output());
        assertEquals("Cannot release: camera integrity not verified.", TerminalInterpreter2.execute("release",true,true,false,true,false).output());
        assertEquals("Cannot release: service register not verified.", TerminalInterpreter2.execute("release",true,true,true,false,false).output());
        assertTrue(TerminalInterpreter2.execute(" release ",true,true,true,true,false).lockdownReleased());
        for(String bad : new String[]{"1.2.3", "256.1.1.1", "1.2.3.04", "a.b.c.d"}) assertFalse(TerminalInterpreter2.validIpv4(bad),bad);
    }

    @Test void room2IntentFlowValidatesDialLocksAndDualConfirmation() {
        IntentHandler handler=new IntentHandler(); GameState s=new GameState();s.beginRoom2();
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"cabinet"),"Host").accepted());
        for(int d=0;d<3;d++){Network.Intent set=new Network.Intent(Network.Intent.Type.DIAL_SET);set.i=d;set.j=new int[]{3,1,7}[d];assertTrue(handler.apply(s,set,"Host").accepted());}
        assertTrue(handler.apply(s,new Network.Intent(Network.Intent.Type.DRAWER_OPEN),"Host").accepted());
        assertTrue(handler.apply(s,new Network.Intent(Network.Intent.Type.TAKE_KEYCARD),"Host").accepted());
        Network.Intent openLog=intent(Network.Intent.Type.OPEN_PANEL,"log_archive");assertTrue(handler.apply(s,openLog,"Host").accepted());
        Network.Intent log=intent(Network.Intent.Type.LOG_SUBMIT,"10.20.30.77");log.i=7;assertTrue(handler.apply(s,log,"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.CLOSE_PANEL,"log_archive"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"service_register"),"Host").accepted());
        for(Map.Entry<String,String> pair:ServiceRegisterMatcher.ANSWER.entrySet()){Network.Intent p=intent(Network.Intent.Type.REGISTER_PAIR,pair.getKey());p.b=pair.getValue();assertTrue(handler.apply(s,p,"Host").accepted());}
        assertTrue(handler.apply(s,new Network.Intent(Network.Intent.Type.REGISTER_CHECK),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.CLOSE_PANEL,"service_register"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"cctv_wall"),"Host").accepted());
        Network.Intent camera=new Network.Intent(Network.Intent.Type.CCTV_FLAG);camera.i=6;assertTrue(handler.apply(s,camera,"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.CLOSE_PANEL,"cctv_wall"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"terminal2"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.TERMINAL_COMMAND,"block 10.20.30.77"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.TERMINAL_COMMAND,"release"),"Host").accepted());
        assertFalse(s.room2Complete);
        Network.Intent confirm=intent(Network.Intent.Type.DOOR_CONFIRM,"host");confirm.i=1;assertTrue(handler.apply(s,confirm,"Host").accepted());
        confirm=intent(Network.Intent.Type.DOOR_CONFIRM,"client");confirm.i=1;assertTrue(handler.apply(s,confirm,"Client").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"exit_keypad"),"Host").accepted());
        assertTrue(handler.apply(s,intent(Network.Intent.Type.KEYPAD_SUBMIT,"6742"),"Host").accepted());
        assertTrue(s.room2Complete);
    }

    @Test void clueOwnershipIsConfiguredByRoleAndSoloHasFullAccess() {
        assertTrue(CoopRules.canOpen(3,true,"bookshelf"));assertFalse(CoopRules.canOpen(3,false,"bookshelf"));
        assertFalse(CoopRules.canOpen(3,true,"incident_board"));assertTrue(CoopRules.canOpen(3,false,"incident_board"));
        assertTrue(CoopRules.canOpen(3,true,"plant"));
    }
    @Test void cctvFlagValidatesTamperedCameraAndRejectsOthers() {
        IntentHandler handler=new IntentHandler(); GameState s=new GameState();s.beginRoom2();
        assertTrue(handler.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"cctv_wall"),"Host").accepted());
        Network.Intent wrongCam=new Network.Intent(Network.Intent.Type.CCTV_FLAG);wrongCam.i=8;
        IntentHandler.Result wrongRes=handler.apply(s,wrongCam,"Host");
        assertFalse(wrongRes.accepted());
        assertEquals("CAM 8's clock matches the server. It is not tampered.",wrongRes.notice());
        assertFalse(s.cctvSolved);

        Network.Intent tamperedCam=new Network.Intent(Network.Intent.Type.CCTV_FLAG);tamperedCam.i=6;
        IntentHandler.Result rightRes=handler.apply(s,tamperedCam,"Host");
        assertTrue(rightRes.accepted());
        assertEquals("TamperedCameraFound",rightRes.event());
        assertTrue(s.cctvSolved);
    }
    private static Network.Intent intent(Network.Intent.Type type,String a){Network.Intent i=new Network.Intent(type);i.a=a;return i;}
}
