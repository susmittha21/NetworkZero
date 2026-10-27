package com.networkzero.engine;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.JsonValue;
import java.util.*;

/** Restrained room ambience: rain outside and small rack activity LEDs. */
public final class Atmosphere {
    private final Random random=new Random(31);
    private final float[] ledX=new float[22],ledY=new float[22],ledT=new float[22];
    private final float[] rainX=new float[78],rainY=new float[78],rainV=new float[78];
    private float time;
    private float rainLeft=1170,rainTop=20,rainWidth=206,rainHeight=420;
    public Atmosphere(){for(int i=0;i<ledX.length;i++){ledX[i]=i<3?890+random.nextFloat()*28:830+random.nextFloat()*150;ledY[i]=i<3?253+random.nextFloat()*18:290+random.nextFloat()*220;ledT[i]=random.nextFloat()*2;}for(int i=0;i<rainX.length;i++){rainX[i]=1170+random.nextFloat()*206;rainY[i]=random.nextFloat()*440;rainV[i]=120+random.nextFloat()*100;}}
    public void update(float dt){time+=dt;for(int i=0;i<rainX.length;i++){rainY[i]+=rainV[i]*dt;if(rainY[i]>445){rainY[i]=0;rainX[i]=1170+random.nextFloat()*206;}}for(int i=0;i<ledT.length;i++)ledT[i]-=dt;}
    public void configure(JsonValue root){JsonValue rain=root.get("rainWindow");if(rain!=null&&rain.size>=4){rainLeft=rain.get(0).asFloat();rainTop=rain.get(1).asFloat();rainWidth=rain.get(2).asFloat();rainHeight=rain.get(3).asFloat();for(int i=0;i<rainX.length;i++)if(rainX[i]<rainLeft||rainX[i]>rainLeft+rainWidth)rainX[i]=rainLeft+random.nextFloat()*rainWidth;}}
    public void draw(ShapeRenderer s, boolean ignoredScanlines){
        s.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0,0,1376,768));
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        s.begin(ShapeRenderer.ShapeType.Line);s.setColor(.62f,.78f,.9f,.18f);for(int i=0;i<rainX.length;i++)if(rainY[i]>=rainTop&&rainY[i]<=rainTop+rainHeight)s.line(rainX[i],768-rainY[i],rainX[i]-2,768-rainY[i]+7);
        s.setColor(.25f,.8f,.5f,.62f);for(int i=0;i<ledX.length;i++){if(ledT[i]<=0)ledT[i]=.45f+random.nextFloat()*2.2f;if(ledT[i]<.55f){s.setColor(i<3?Color.RED:(i%3==0?Color.GREEN:Color.ORANGE));s.circle(ledX[i],768-ledY[i],1.35f);}}
        s.end();
    }
}
