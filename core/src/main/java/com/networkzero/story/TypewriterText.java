package com.networkzero.story;

import java.util.List;

public final class TypewriterText {
    private List<String> lines=List.of();
    private int lineIndex;
    private float elapsed;
    private static final float CHARS_PER_SECOND=35f;
    public void setLines(List<String> newLines) { lines=newLines==null?List.of():newLines;lineIndex=0;elapsed=0; }
    public void update(float delta) { if(isTyping())elapsed+=delta; }
    public boolean isTyping() { return lineIndex<lines.size()&&visibleCharacters()<currentLine().length(); }
    public boolean hasLine() { return lineIndex<lines.size(); }
    public String currentLine() { return hasLine()?lines.get(lineIndex):""; }
    public int visibleCharacters() { return hasLine()?Math.min(currentLine().length(),(int)(elapsed*CHARS_PER_SECOND)):0; }
    public int lineIndex() { return lineIndex; }
    public void completeLine() { if(hasLine())elapsed=(currentLine().length()+1)/CHARS_PER_SECOND; }
    /** Completes the current line, starts the next, or returns true after the final line. */
    public boolean advance() {
        if(!hasLine())return true;
        if(isTyping()){completeLine();return false;}
        lineIndex++;elapsed=0;
        return lineIndex>=lines.size();
    }
    public void restart() { lineIndex=0;elapsed=0; }
}
