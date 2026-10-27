package com.networkzero.room;

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
    public int room2SecondsLeft=2700, room2Score=1000, room2Hints, differentPorts, tamperedCamera;
    public final int[] dialDigits={0,0,0};
    public boolean keycardTaken, drawerOpen, logSolved, portsSolved, cctvSolved, attackerBlocked, lockdownReleased;
    public boolean doorConfirmHost, doorConfirmClient, room2Complete;
    public String logSource="", terminalBuffer="", doorCode="";
    public final Map<String,String> registerPairs=new LinkedHashMap<>();

    // Backward compatibility aliases
    public int room3SecondsLeft=2700, room3Score=1000, room3Hints;
    public boolean room3Complete;

    public void copyFrom(GameState s){if(s==null)return;roomNumber=s.roomNumber;secondsLeft=s.secondsLeft;score=s.score;hintsUsed=s.hintsUsed;routerOnline=s.routerOnline;boardSolved=s.boardSolved;pcSolved=s.pcSolved;roomComplete=s.roomComplete;paused=s.paused;pausedBy=s.pausedBy;logbookRead=s.logbookRead;noticeRead=s.noticeRead;inventory.clear();inventory.addAll(s.inventory);pickedUp.clear();pickedUp.addAll(s.pickedUp);solved.clear();solved.addAll(s.solved);links.clear();for(int[] l:s.links)links.add(l.clone());System.arraycopy(s.wires,0,wires,0,Math.min(wires.length,s.wires.length));System.arraycopy(s.board,0,board,0,Math.min(board.length,s.board.length));wirePick=s.wirePick;portPick=s.portPick;devicePick=s.devicePick;hintTier=s.hintTier;hintTiers.clear();hintTiers.putAll(s.hintTiers);lockOwner.clear();lockOwner.putAll(s.lockOwner);ip=s.ip;mask=s.mask;gateway=s.gateway;terminalText=s.terminalText;roomMessage=s.roomMessage;int sec=s.room2SecondsLeft!=2700?s.room2SecondsLeft:s.room3SecondsLeft;room2SecondsLeft=sec;room3SecondsLeft=sec;int sc=s.room2Score!=1000?s.room2Score:s.room3Score;room2Score=sc;room3Score=sc;int h=s.room2Hints!=0?s.room2Hints:s.room3Hints;room2Hints=h;room3Hints=h;System.arraycopy(s.dialDigits,0,dialDigits,0,3);differentPorts=s.differentPorts;tamperedCamera=s.tamperedCamera;keycardTaken=s.keycardTaken;drawerOpen=s.drawerOpen;logSolved=s.logSolved;portsSolved=s.portsSolved;cctvSolved=s.cctvSolved;attackerBlocked=s.attackerBlocked;lockdownReleased=s.lockdownReleased;doorConfirmHost=s.doorConfirmHost;doorConfirmClient=s.doorConfirmClient;boolean comp=s.room2Complete||s.room3Complete;room2Complete=comp;room3Complete=comp;logSource=s.logSource;terminalBuffer=s.terminalBuffer;doorCode=s.doorCode;registerPairs.clear();registerPairs.putAll(s.registerPairs);}

    public void beginRoom2(){roomNumber=2;room2SecondsLeft=2700;room3SecondsLeft=2700;room2Score=1000;room3Score=1000;room2Hints=0;room3Hints=0;Arrays.fill(dialDigits,0);differentPorts=0;tamperedCamera=0;keycardTaken=false;drawerOpen=false;logSolved=false;portsSolved=false;cctvSolved=false;attackerBlocked=false;lockdownReleased=false;doorConfirmHost=false;doorConfirmClient=false;room2Complete=false;room3Complete=false;logSource="";terminalBuffer="CONTROL TERMINAL v2.0\nType help for commands.";doorCode="";registerPairs.clear();lockOwner.clear();paused=false;pausedBy="";roomComplete=false;solved.clear();inventory.clear();hintTiers.clear();}
    public void beginRoom3(){beginRoom2();}
}
