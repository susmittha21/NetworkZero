package com.networkzero.session;

import com.badlogic.gdx.Gdx;
import com.google.gson.*;

/** Reads the editable hint strings from assets/data/hints.json. */
public final class HintManager {
    private JsonObject data;
    public HintManager(){reload();}
    public void reload(){try{data=JsonParser.parseString(Gdx.files.internal("data/hints.json").readString("UTF-8")).getAsJsonObject();}catch(Exception e){throw new IllegalStateException("Could not load assets/data/hints.json",e);}}
    public String get(String puzzle,int tier){JsonArray lines=data==null?null:data.getAsJsonArray(puzzle);return lines==null||lines.size()==0?"Hint data unavailable.":lines.get(Math.max(0,Math.min(lines.size()-1,tier-1))).getAsString();}
}
