package my.evorunner.game.Gameplay.Skill_Buttons;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import my.evorunner.game.Gameplay.Player;


/**
 * Version 1.0
 * Last Edited: Matthew Phan
 * Changelog 1.0
 * Added attack button and jump button
 */
public class ButtonTable {
    private Stage stage;
    private Player player;
    private Table table;
    private Table table2;
    private AbilityButton abilityButton;

    public ButtonTable(final Player player, Stage stage) {
        table = new Table();
        table2 = new Table();
        this.player = player;
        //Creates jump button and adds a listener
        JumpButton jumpButton = new JumpButton(player);
        LeftButton left = new LeftButton(player);
        RightButton right = new RightButton(player);
        //Creates attack button and adds a listener
        abilityButton = selectAbilityForCharacter();

        //Adds buttons to the table and adds it to the stage
        table.add(jumpButton.getImageButton()).pad(0, 20, 0, 20);
        table.add(abilityButton.getImageButton()).pad(0, 20, 0, 30);
        table.setFillParent(true);
        table.right();
        table.bottom();
        //table.debug(); //Uncomment to turn on table lines

        table2.add(left.getImageButton()).pad(0, 20, 0, 20);
        table2.add(right.getImageButton()).pad(0, 20, 0, 20);
        table2.setFillParent(true);
        table2.left();
        table2.bottom();

        stage.addActor(table);
        stage.addActor(table2);

    }

        // Will Add different abilities to the screen depending on character skin
        private AbilityButton selectAbilityForCharacter() {
            int characterID = player.getCharacterID();
            AbilityButton abilityButton;
            switch(characterID) {
                default:
                    abilityButton = new DragonAttackButton(player);
            }
            return abilityButton;
        }
    public void update() {
        abilityButton.cooldownChecker();
    }

}

