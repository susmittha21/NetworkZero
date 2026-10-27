package com.networkzero.session;

import com.networkzero.net.Network;
import com.networkzero.room.GameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    @Test void hostIntentMutatesStateOnce(){GameState state=new GameState();IntentHandler h=new IntentHandler();assertTrue(h.apply(state,intent(Network.Intent.Type.PICK_UP,"crimper"),"Host").accepted());assertEquals(java.util.List.of("Crimping Tool"),state.inventory);assertFalse(h.apply(state,intent(Network.Intent.Type.PICK_UP,"crimper"),"Host").accepted());}
    @Test void puzzleLocksAreExclusiveAndReleased(){GameState s=new GameState();IntentHandler h=new IntentHandler();assertTrue(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Alice").accepted());assertFalse(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Bob").accepted());assertTrue(h.apply(s,intent(Network.Intent.Type.CLOSE_PANEL,"crimp"),"Alice").accepted());assertTrue(h.apply(s,intent(Network.Intent.Type.OPEN_PANEL,"crimp"),"Bob").accepted());hLocksRelease(s,"Bob");assertTrue(s.lockOwner.isEmpty());}
    @Test void asymmetricCluesAreRoleSpecificAndCanBeDisabled(){assertTrue(CoopRules.canOpen(true,"logbook"));assertFalse(CoopRules.canOpen(false,"logbook"));assertFalse(CoopRules.canOpen(true,"notice_board"));assertTrue(CoopRules.canOpen(false,"notice_board"));assertTrue(CoopRules.canOpen(false,"pc"));}
    private static void hLocksRelease(GameState s,String owner){new LockManager().releaseAll(s,owner);}
    private static Network.Intent intent(Network.Intent.Type type,String a){Network.Intent i=new Network.Intent(type);i.a=a;return i;}
}
