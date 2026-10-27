package com.networkzero.session;

import com.networkzero.net.Network;
import java.util.ArrayDeque;
import java.util.List;

/** Ephemeral in-session chat; retains at most the most recent 100 lines. */
public final class ChatModel {
    private final ArrayDeque<Network.ChatMessage> messages=new ArrayDeque<>();private int unread;
    public void add(Network.ChatMessage message,boolean open){if(message==null||message.text==null||message.text.isBlank())return;if(messages.size()==100)messages.removeFirst();messages.addLast(message);if(!open&&!message.system)unread++;}
    public List<Network.ChatMessage> messages(){return List.copyOf(messages);}public int unread(){return unread;}public void opened(){unread=0;}
}
