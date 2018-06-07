package my.evorunner.game.Gameplay;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;

// Peter Van & Matthew Phan
// This class is intended to assist in taking user inputs from UI elements
public class MyTextFieldListener implements TextFieldListener {
    @Override
    public void keyTyped(TextField textField, char c) {
        textField.setText(textField.getText() + c);
    }
}
