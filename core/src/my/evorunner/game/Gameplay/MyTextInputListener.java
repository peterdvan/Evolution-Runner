package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Input;

/**
 * Created by Matthew on 4/15/2018.
 */
// Peter Van & Matthew Phan
// This class is intended to assist in taking user inputs from UI elements
public class MyTextInputListener implements Input.TextInputListener{

    String input;
    @Override
    public void input (String text) {
        input = text;
    }

    @Override
    public void canceled() {

    }

    public String getText() {
        return input;
    }
}
