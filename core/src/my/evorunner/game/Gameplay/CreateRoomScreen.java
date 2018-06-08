package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import my.evorunner.game.EvoRunner;
import my.evorunner.game.NativePlatform;

import static my.evorunner.game.Constants.NOT_ENOUGH_COINS;
import static my.evorunner.game.Constants.SCREEN_HEIGHT;
import static my.evorunner.game.Constants.SCREEN_WIDTH;
import static my.evorunner.game.EvoRunner.generator;
import static my.evorunner.game.EvoRunner.parameter;

/**
 * Created by Peter on 4/9/2018.
 */
// Peter Van & Matthew Phan
// This class is intended to create screen where players can configure multiplayer room properties
// before starting their multiplayer game.
public class CreateRoomScreen implements Screen{
    TextField roomName;
    TextField playerName;
    String playerNameString;
    String roomNameString;
    SelectBox maxUsers;
    SelectBox bet;
    Stage stage;
    AssetManager manager;
    private EvoRunner parent;

    private BitmapFont font;
    private TextButton.TextButtonStyle wordFont;
    private TextField.TextFieldStyle textFieldStyle;
    NativePlatform nativePlatform;


    public CreateRoomScreen(EvoRunner parent,NativePlatform nativePlatform){
        this.nativePlatform = nativePlatform;
        this.parent = parent;
    }

    public Socket socket;

    @Override
    public void show() {
        playerNameString = "";
        roomNameString = "";
        font = generator.generateFont(parameter);
        wordFont = new TextButton.TextButtonStyle();
        wordFont.font = font;
        manager = new AssetManager();
        Table table = new Table();
        table.setFillParent(true);
        stage = new Stage(new ScreenViewport());

        manager.load("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        manager.finishLoading();
        TextureAtlas atlas = manager.get("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.BLACK;
        //Set up skins to be used for UI
        Skin skinForNumberOfUsersBox = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        Skin skinForRoomNameField = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        Skin defaultSkin = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);

        //Set the font sizes for each of the different skins
        skinForRoomNameField.getFont("font").getData().setScale(4f,4f);
        skinForNumberOfUsersBox.getFont("font").getData().setScale(4f,4f);
        defaultSkin.getFont("font").getData().setScale(4f,4f);

        font.getData().setScale(5);
        wordFont.font = font;
        TextButton start = new TextButton("Make Room", wordFont);
        start.setColor(Color.BLUE);
        start.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int betAmount = Integer.parseInt(bet.getSelected().toString());
                int savedCoins = EvoRunner.savedData.getInteger("coins");
                EvoRunner.savedData.putInteger("bet", betAmount);
                if(betAmount <= savedCoins) {
                    parent.multiplayerLobby.socket = socket;
                    parent.multiplayerLobby.roomHost = socket.id();
                    createRoom();
                    parent.changeScreens(EvoRunner.MULTIPLAYER_LOBBY);
                } else {
                    nativePlatform.displayError(NOT_ENOUGH_COINS);
                }
            }
        }));

        Label betLabel = new Label("Bet",skinForRoomNameField);
        betLabel.setColor(Color.WHITE);
        betLabel.setFontScale(7,4);
        Label playerAmounts = new Label("Players",skinForRoomNameField);
        playerAmounts.setColor(Color.WHITE);
        playerAmounts.setFontScale(7,4);

        //Save text fields

        playerName = new TextField("",skinForRoomNameField);
        playerName.setColor(Color.BLUE);
        playerName.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                playerNameString = textField.getText().toString();
            }
        });
        roomName = new TextField("",skinForRoomNameField);
        roomName.setColor(Color.BLUE);
        roomName.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                roomNameString = textField.getText().toString();
            }
        });
        maxUsers = new SelectBox(skinForNumberOfUsersBox);
        maxUsers.setColor(Color.BLUE);
        bet = new SelectBox(skinForNumberOfUsersBox);
        String[] numOfPlayers = new String[]{"2", "3", "4"};
        maxUsers.setItems(numOfPlayers);
        String[] betAmounts = new String[]{"0", "10", "50", "100"};
        bet.setItems(betAmounts);
        bet.setColor(Color.BLUE);
        roomName.setMessageText("Room Name");
        playerName.setMessageText("Player Name");
        playerName.setColor(Color.BLUE);
        maxUsers.setSelectedIndex(0);
        bet.setSelectedIndex(0);

        table.add(playerAmounts).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/6).pad(0,30,0,0);
        table.add(maxUsers).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/6);
        table.row().pad(25, 0, 10, 0);
        table.add(betLabel).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/6).pad(0,30,0,0);
        table.add(bet).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/6);
        table.row().pad(25, 0, 10, 0);
        table.add(playerName).width(SCREEN_WIDTH/2).height(SCREEN_HEIGHT/6);
        table.row().pad(10, 0, 10, 0);
        table.add(roomName).width(SCREEN_WIDTH/2).height(SCREEN_HEIGHT/6);
        table.row().pad(10, 0, 10, 0);

        start.setPosition(SCREEN_WIDTH / 2, 20);
        stage.addActor(start);

        TextButton backButton = new TextButton("<=",wordFont);
        backButton.getLabel().setFontScale(6);
        backButton.setPosition(70,SCREEN_HEIGHT * 7 / 8);
        backButton.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                socket.disconnect();
                dispose();
                parent.changeScreens(EvoRunner.MENU_SCREEN);
            }
        }));
        stage.addActor(table);
        stage.addActor(backButton);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }


    public void createRoom() {
        int betAmount = Integer.parseInt(bet.getSelected().toString());
        JSONObject jsonObject = new JSONObject();
            try {
                if (playerNameString == "") {
                    playerNameString = "New Player";
                }
                if (roomNameString == "") {
                    roomNameString = "Room";
                }
                jsonObject.put("roomName", roomNameString);
                jsonObject.put("maxPlayers", Integer.parseInt(maxUsers.getSelected().toString()));
                jsonObject.put("playerName", playerNameString);
                jsonObject.put("bet", betAmount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("createRoom", jsonObject);

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        stage.dispose();
        manager.dispose();

    }

}
