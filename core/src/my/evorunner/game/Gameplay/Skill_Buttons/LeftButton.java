package my.evorunner.game.Gameplay.Skill_Buttons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import my.evorunner.game.Gameplay.Player;

/**
 * Created by Matthew on 6/6/2018.
 */

public class LeftButton {
    private Texture myTexture;
    private TextureRegion myTextureRegion;
    private TextureRegionDrawable myTexRegionDrawable;
    private ImageButton leftButton;
    public LeftButton(final Player player) {
        myTexture = new Texture(Gdx.files.internal("Items/LeftButton.png"));
        myTextureRegion = new TextureRegion(myTexture);
        myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        leftButton = new ImageButton(myTexRegionDrawable); //Set the button up

        leftButton.addListener(new ClickListener() {

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.moveLeft = true;
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                player.moveLeft = false;
            }
        });
    }

    public ImageButton getImageButton() {
        return leftButton;
    }
}
