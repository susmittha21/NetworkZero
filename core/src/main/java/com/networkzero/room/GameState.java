package com.networkzero.room;

import com.google.gson.annotations.SerializedName;
import java.util.*;

/** Serializable room progress, independent of rendering. */
public final class GameState {
    public int roomNumber=1;
    public int secondsLeft=2700, score=1000, hintsUsed;
    public boolean routerOnline, boardSolved, pcSolved, roomComplete, paused;
    public boolean logbookRead, noticeRead;
    public final List<String> inventory=new ArrayList<>();
    public final Set<String> pickedUp=new HashSet<>(), solved=new HashSet<>();
    public final List<int[]> links=new ArrayList<>();
    public final String[] wires={"Brown","White-Green","Orange","White-Brown","Blue","White-Orange","Green","White-Blue"};
    public final String[] board={"","","",""};
    public int wirePick=-1, portPick=-1, devicePick=-1, hintTier;
    public final Map<String,Integer> hintTiers=new HashMap<>();
    /** Puzzle id to owning player name; empty/missing means unlocked. */
    public final Map<String,String> lockOwner=new HashMap<>();
    public String ip="",mask="",gateway="", terminalText="", roomMessage="",pausedBy="";

    // Room 2 state is kept in the same host-authoritative snapshot as Room 1.
    @SerializedName(value="room2SecondsLeft", alternate={"room3SecondsLeft"})
    public int room2SecondsLeft=2700;
    @SerializedName(value="room2Score", alternate={"room3Score"})
    public int room2Score=1000;
    @SerializedName(value="room2Hints", alternate={"room3Hints"})
    public int room2Hints;
    public int differentPorts, tamperedCamera;
    public final int[] dialDigits={0,0,0};
    public boolean keycardTaken, drawerOpen, logSolved, portsSolved, cctvSolved, attackerBlocked, lockdownReleased;
    public boolean doorConfirmHost, doorConfirmClient;
    @SerializedName(value="room2Complete", alternate={"room3Complete"})
    public boolean room2Complete;
    public String logSource="", terminalBuffer="", doorCode="";
    public final Map<String,String> registerPairs=new LinkedHashMap<>();

    public void copyFrom(GameState s){if(s==null)return;s.migrateLegacyRoom2State();roomNumber=s.roomNumber;secondsLeft=s.secondsLeft;score=s.score;hintsUsed=s.hintsUsed;routerOnline=s.routerOnline;boardSolved=s.boardSolved;pcSolved=s.pcSolved;roomComplete=s.roomComplete;paused=s.paused;pausedBy=s.pausedBy;logbookRead=s.logbookRead;noticeRead=s.noticeRead;inventory.clear();inventory.addAll(s.inventory);pickedUp.clear();pickedUp.addAll(s.pickedUp);solved.clear();solved.addAll(s.solved);links.clear();for(int[] l:s.links)links.add(l.clone());System.arraycopy(s.wires,0,wires,0,Math.min(wires.length,s.wires.length));System.arraycopy(s.board,0,board,0,Math.min(board.length,s.board.length));wirePick=s.wirePick;portPick=s.portPick;devicePick=s.devicePick;hintTier=s.hintTier;hintTiers.clear();hintTiers.putAll(s.hintTiers);lockOwner.clear();lockOwner.putAll(s.lockOwner);ip=s.ip;mask=s.mask;gateway=s.gateway;terminalText=s.terminalText;roomMessage=s.roomMessage;room2SecondsLeft=s.room2SecondsLeft;room2Score=s.room2Score;room2Hints=s.room2Hints;System.arraycopy(s.dialDigits,0,dialDigits,0,3);differentPorts=s.differentPorts;tamperedCamera=s.tamperedCamera;keycardTaken=s.keycardTaken;drawerOpen=s.drawerOpen;logSolved=s.logSolved;portsSolved=s.portsSolved;cctvSolved=s.cctvSolved;attackerBlocked=s.attackerBlocked;lockdownReleased=s.lockdownReleased;doorConfirmHost=s.doorConfirmHost;doorConfirmClient=s.doorConfirmClient;room2Complete=s.room2Complete;logSource=s.logSource;terminalBuffer=s.terminalBuffer;doorCode=s.doorCode;registerPairs.clear();registerPairs.putAll(s.registerPairs);}

    public void migrateLegacyRoom2State(){if(roomNumber==3)roomNumber=2;Map<String,Integer> legacy=new HashMap<>(hintTiers);for(Map.Entry<String,Integer> hint:legacy.entrySet())if(hint.getKey().startsWith("r3_"))hintTiers.putIfAbsent("r2_"+hint.getKey().substring(3),hint.getValue());if(solved.remove("R3_RELEASE"))solved.add("R2_RELEASE");}

    public void beginRoom2(){roomNumber=2;room2SecondsLeft=2700;room2Score=1000;room2Hints=0;Arrays.fill(dialDigits,0);differentPorts=0;tamperedCamera=0;keycardTaken=false;drawerOpen=false;logSolved=false;portsSolved=false;cctvSolved=false;attackerBlocked=false;lockdownReleased=false;doorConfirmHost=false;doorConfirmClient=false;room2Complete=false;logSource="";terminalBuffer="CONTROL TERMINAL v2.0\nType help for commands.";doorCode="";registerPairs.clear();lockOwner.clear();paused=false;pausedBy="";roomComplete=false;solved.clear();inventory.clear();hintTiers.clear();}
}
