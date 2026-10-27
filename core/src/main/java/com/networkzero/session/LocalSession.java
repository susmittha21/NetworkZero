package com.networkzero.session;

import com.networkzero.net.Network;
import com.networkzero.room.GameState;
import java.util.function.Consumer;

/** Existing solo flow: intents are applied immediately without networking. */
public final class LocalSession implements GameSession {
    private final GameState state; private final String name; private final IntentHandler handler=new IntentHandler(); private final Consumer<IntentHandler.Result> result;
    public LocalSession(GameState state,String name,Consumer<IntentHandler.Result> result){this.state=state;this.name=name;this.result=result;}
    public GameState state(){return state;} public void submit(Network.Intent i){IntentHandler.Result r=handler.apply(state,i,name);if(result!=null)result.accept(r);} public boolean isRemote(){return false;} public boolean isHost(){return true;} public String localName(){return name;} public String partnerName(){return "";} public boolean partnerConnected(){return false;} public String partnerActivity(){return "";} public void publish(){}
    public void close(){}
}
