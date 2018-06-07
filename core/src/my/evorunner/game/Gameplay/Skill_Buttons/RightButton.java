package my.evorunner.game.Gameplay.Skill_Buttons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import my.evorunner.game.Gameplay.Player;

/**
 * Created by Matthew on 6/6/2018.
 */

public class RightButton {
    private Texture myTexture;
    private TextureRegion myTextureRegion;
    private TextureRegionDrawable myTexRegionDrawable;
    private ImageButton rightButton;
    public RightButton(final Player player) {
        myTexture = new Texture(Gdx.files.internal("Items/RightButton.png"));
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        rightButton = new ImageButton(myTexRegionDrawable); //Set the button up
        rightButton.addListener(new ClickListener() {

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.moveRight = true;
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                player.moveRight = false;
            }
        });
    }

    public ImageButton getImageButton() {
        return rightButton;
    }
}