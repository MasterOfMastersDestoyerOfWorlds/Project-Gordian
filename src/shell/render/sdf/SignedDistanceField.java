package shell.render.sdf;

import shell.render.Color;
import shell.render.Texture;
import shell.render.shaders.ShaderProgram;
import shell.render.shaders.SignedDistanceFieldShader;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class SignedDistanceField {

    private Texture texture;
    public ShaderProgram shader;
    private Color borderColor;
    private float borderInner;
    private float borderOuter;

    public SignedDistanceField(ShaderProgram sdfShader, String sdfLocation) {
        texture = Texture.loadTexture("decal_sdf.png");
        shader = sdfShader;
    }

    public SignedDistanceField(SignedDistanceFieldShader sdfShader, String string, Color borderColor,
            float borderDist) {
        texture = Texture.loadTexture("decal_sdf.png");
        shader = sdfShader;
        this.borderColor = borderColor;
        this.borderInner = borderDist - 0.1f;
        this.borderOuter = borderDist;
    }

    public void draw(int drawX, int drawY, int width, int height, int zIndex, Color c) {
        texture.bind();
        shader.use();
        shader.setTexture("texImage", texture, GL_TEXTURE0, 0);
        shader.setFloat("borderInner", borderInner);
        shader.setFloat("borderOuter", borderOuter);
        shader.setVec4("borderColor", borderColor.toVector4f());
        shader.begin();

        shader.drawTextureRegion(texture, drawX, drawY, drawX + width, drawY + height, zIndex, 0, 0, texture.width,
                texture.height, c);

        shader.end();
    }

    public void drawCentered(int drawX, int drawY, int width, int height, int zIndex, Color c) {
        draw(drawX - (width / 2), drawY - (height / 2), width, height, zIndex, c);
    }

    public void setBorderDist(float borderDist) {
        this.borderInner = borderDist - 0.1f;
        this.borderOuter = borderDist;
    }

}