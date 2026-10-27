package com.networkzero.story;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import java.util.Random;

/** Quiet rainfall; positions and speed are kept in source-image pixels. */
public final class RainEffect implements StoryEffect {
    private final Random random=new Random(17);
    private final float[] x=new float[36],y=new float[36],speed=new float[36];
    private final float left,top,width,height,wind;
    public RainEffect(float left,float top,float width,float height,float wind){this.left=left;this.top=top;this.width=width;this.height=height;this.wind=wind;for(int i=0;i<x.length;i++){x[i]=random.nextFloat()*width;y[i]=random.nextFloat()*height;speed[i]=42+random.nextFloat()*40;}}
    @Override public void update(float delta){for(int i=0;i<x.length;i++){y[i]+=speed[i]*delta;if(y[i]>height+14){y[i]=0;x[i]=random.nextFloat()*width;}}}
    @Override public void render(ShapeRenderer shapes,StoryScreen.PanelTransform transform){
        Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);shapes.setColor(.58f,.76f,.88f,.08f);
        for(int i=0;i<x.length;i++){float sx=transform.x(left+x[i],top+y[i]),sy=transform.y(left+x[i],top+y[i]);float ex=transform.x(left+x[i]+wind*.05f,top+y[i]+7),ey=transform.y(left+x[i]+wind*.05f,top+y[i]+7);shapes.line(sx,sy,ex,ey);}
        shapes.end();
    }
}
