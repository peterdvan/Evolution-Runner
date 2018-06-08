package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.KeyEvent;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import my.evorunner.game.EvoRunner;

import static my.evorunner.game.Constants.SCREEN_HEIGHT;
import static my.evorunner.game.Constants.SCREEN_WIDTH;
import static my.evorunner.game.EvoRunner.generator;
import static my.evorunner.game.EvoRunner.parameter;
import static my.evorunner.game.EvoRunner.savedData;

/**
 * Created by Matthew on 4/15/2018.
 *
 * each player id is their array room id in server
 */

public class JoinScreen implements Screen, InputProcessor {

    private TextureRegion region;
    private EvoRunner parent;
    private Stage stage;
    private Stage buttonStage;
    private Table table;

    AssetManager manager;
    private TextField.TextFieldStyle textFieldStyle;

    public Socket socket;
    public boolean joined = false;
    public String roomHost = "";
    TextField playerName;

    private BitmapFont font;
    private TextButton.TextButtonStyle wordFont;

    OrthographicCamera camera;
    ScreenViewport view;

    public String playerNameString;

    public JoinScreen (EvoRunner parent) {

        this.parent = parent;

    }

    public void addLobby(String information, final String roomHost, int height, final int bet) {
        TextButton lobby = new TextButton(information, wordFont);
        lobby.getLabel().setFontScale(3);
        lobby.setPosition(SCREEN_WIDTH / 5 * 2.5f, height);
        lobby.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!joined) {
                    if (EvoRunner.savedData.getInteger("coins") > bet) {
                        savedData.putInteger("bet", bet);
                        joinRoom(roomHost);
                    }
                }
            }
        }));
        stage.addActor(lobby);
    }

    public void joinRoom(String roomHost) {
        JSONObject data = new JSONObject();
        try {
            if (playerNameString == "") {
                playerNameString = "New Player";
            }

            data.put("roomHost", roomHost);
            this.roomHost = roomHost;
            data.put("name", playerNameString);
            socket.emit("joinRoom", data);
        } catch (JSONException e) {
            Gdx.app.log("multiplayer", "unable to join rooms");
        }
    }
    public void askForRooms() {
        JSONObject data = new JSONObject();
        socket.emit("requestListOfAvailableGames", data);
    }

    public void setUpStage(Stage stage) {
        table = new Table();
        buttonStage = new Stage();
    }

    public void addUI() {
        TextButton backButton = new TextButton("<=",wordFont);
        backButton.getLabel().setFontScale(5);
        backButton.setPosition(70,SCREEN_HEIGHT * 7 / 8);
        backButton.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                socket.disconnect();
                dispose();
                parent.changeScreens(EvoRunner.MENU_SCREEN);
            }
        }));

        TextButton refresh = new TextButton("Refresh", wordFont);
        refresh.getLabel().setFontScale(5);
        refresh.setPosition(400, SCREEN_HEIGHT * 7 / 8);
        refresh.addListener((new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                askForRooms();
            }
        }));
        stage.addActor(table);
        stage.addActor(backButton);
        stage.addActor(refresh);
    }

    @Override
    public void show() {
        playerNameString = "";
        manager = new AssetManager();
        manager.load("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        manager.finishLoading();
        TextureAtlas atlas = manager.get("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        Skin skinForRoomNameField = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        Skin defaultSkin = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);

        skinForRoomNameField.getFont("font").getData().setScale(4f,4f);
        defaultSkin.getFont("font").getData().setScale(4f,4f);

        font = generator.generateFont(parameter);
        wordFont = new TextButton.TextButtonStyle();
        wordFont.font = font;

        textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.BLACK;
        playerName = new TextField("",skinForRoomNameField);
        playerName.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                playerNameString = textField.getText().toString();
            }
        });
        playerName.setColor(Color.BLUE);
        playerName.setMessageText("Player Name");

        Label playerAmounts = new Label("Players",defaultSkin);
        playerAmounts.setFontScale(7,4);

        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        view = new ScreenViewport(camera);

        stage = new Stage(view);
        setUpStage(stage);

        table.setPosition(SCREEN_WIDTH / 7, SCREEN_HEIGHT * 7 / 8 - 200);
        table.add(playerName).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/8);

        InputMultiplexer multi = new InputMultiplexer(buttonStage, stage, this);
        Gdx.input.setInputProcessor(multi);


        addUI();
        configSocketEvents();
        askForRooms();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);        //Changes the screen color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);    //Clears the screen
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        camera.update();
        stage.draw();

        if (joined) {
            joined = false;
            parent.multiplayerLobby.socket = socket;
            parent.multiplayerLobby.roomHost = this.roomHost;
            dispose();
            parent.changeScreens(EvoRunner.MULTIPLAYER_LOBBY);

        }
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

    public void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
            }
        }).on("showRooms", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    stage.clear();
                    addUI();
                    for (int i = 0; i < objects.length(); i++) {
                        Gdx.app.log("multiplayer", "showing room: " + i);
                        int numberPlayers = (int) objects.getJSONObject(i).getJSONArray("players").length();
                        String roomName = (String) objects.getJSONObject(i).getJSONObject("game").getString("roomName");
                        int maxPlayers = (int) objects.getJSONObject(i).getJSONObject("game").getInt("maxPlayers");
                        String roomHost = (String) objects.getJSONObject(i).getJSONObject("game").getString("roomHost");
                        int bet = (int) objects.getJSONObject(i).getJSONObject("game").getInt("bet");
                        addLobby("Room: " + roomName + "\nMax Players: " +
                        maxPlayers + "\nPlayers joined: " + (numberPlayers) +
                                "\nBet Amount: " + bet,roomHost, SCREEN_HEIGHT - (150 + (i * 300)),bet);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("multiplayer", "unable to show rooms");
                }
            }
        }).on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                Gdx.app.log("multiplayer", "joined");
                try {
                    if (((String) data.getString("playerID")).equals(socket.id())) {
                        joined = true;
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting my ID");
                }
            }
        });
    }



    @Override
    public void dispose() {
        stage.dispose();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {

        float y = Gdx.input.getDeltaY();
        camera.position.add(0, y, 0);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
