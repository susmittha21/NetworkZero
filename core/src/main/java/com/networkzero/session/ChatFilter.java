package com.networkzero.session;

public final class ChatFilter {
    private ChatFilter(){}
    public static String sanitize(String text){if(text==null)return "";String clean=text.replaceAll("[\\p{Cntrl}]","").trim();return clean.substring(0,Math.min(200,clean.length()));}
}
