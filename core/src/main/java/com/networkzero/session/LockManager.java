package com.networkzero.session;

import com.networkzero.room.GameState;

/** Host-only exclusive locks for the four shared puzzle panels. */
public final class LockManager {
    public String acquire(GameState state,String puzzle,String player){String owner=state.lockOwner.get(puzzle);if(owner==null||owner.equals(player)){state.lockOwner.put(puzzle,player);return null;}return owner;}
    public void release(GameState state,String puzzle,String player){if(player.equals(state.lockOwner.get(puzzle)))state.lockOwner.remove(puzzle);}
    public void releaseAll(GameState state,String player){state.lockOwner.entrySet().removeIf(e->player.equals(e.getValue()));}
}
