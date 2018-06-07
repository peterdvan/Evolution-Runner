package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import my.evorunner.game.EvoRunner;

import static my.evorunner.game.Constants.SCREEN_HEIGHT;
import static my.evorunner.game.Constants.SCREEN_WIDTH;
import static my.evorunner.game.EvoRunner.generator;
import static my.evorunner.game.EvoRunner.parameter;

/**
 * Created by Peter on 4/9/2018.
 */
    // Peter Van & Matthew Phan
    // This class is intended to create a multiplayer lobby screen where players can wait
    // before starting their multiplayer game.
public class MultiplayerLobby implements Screen {
    public final String EMPTY = "Empty";
    //Networking Components
    public Socket socket;
    //UI Components
    Table players;
    Stage stage;
    Stage names;
    AssetManager manager;
    private EvoRunner parent;
    Skin skin;
    String roomName;
    String[] playerNames;
    int maxPlayers;
    int roomNumber;
    TextButton[] textButtons;

    private BitmapFont font;
    private TextButton.TextButtonStyle wordFont;

    public Skin skinForRoom;

    public String roomHost;
    public boolean mainmenu = false;

    boolean startGame = false;

    public MultiplayerLobby(EvoRunner parent){
        this.parent = parent;
        this.roomName = null;
        this.maxPlayers = 4;
    }


    public void updateLobby() {
        JSONObject data = new JSONObject();
        try {
            data.put("roomHost", roomHost);
        } catch (JSONException e) {
            Gdx.app.log("multiplayer", "couldn't send room host");
        }
        socket.emit("updateLobby", data);
    }

    @Override
    public void show() {

        font = generator.generateFont(parameter);
        wordFont = new TextButton.TextButtonStyle();
        wordFont.font = font;

        playerNames = new String[maxPlayers];
        for (int i = 0; i < playerNames.length; i++) {
            playerNames[i] = "Empty";
        }
        textButtons = new TextButton[maxPlayers];

        roomNumber = -1;
        manager = new AssetManager();
        players = new Table();
        players.setFillParent(true);
        stage = new Stage(new ScreenViewport());
        names = new Stage();
        manager.load("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        manager.finishLoading();
        TextureAtlas atlas = manager.get("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        skin = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        skin.getFont("font").getData().setScale(4f,4f);

        skinForRoom = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        skinForRoom.getFont("font").getData().setScale(2f,2f);

        addUI();
        updateLobby();

        Gdx.input.setInputProcessor(stage);
        configureSocketEvents();
    }

    public void addUI() {
        TextButton start = new TextButton("Start", wordFont);
        start.getLabel().setFontScale(5);
        start.setPosition(SCREEN_WIDTH/2,10);
        start.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (socket.id().equals(roomHost)) {
                    signalStartOfGame();
                }

            }
        }));
        TextButton backButton = new TextButton("<=",wordFont);
        backButton.getLabel().setFontScale(5);
        backButton.setPosition(70,SCREEN_HEIGHT * 7 / 8);
        backButton.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                socket.emit("leavingLobby", roomHost);
                socket.disconnect();
                dispose();
                parent.changeScreens(EvoRunner.MENU_SCREEN);
                parent.multiplayerLobby = new MultiplayerLobby(parent);
            }
        }));

        TextButton refresh = new TextButton("Refresh", wordFont);
        refresh.getLabel().setFontScale(5);
        refresh.setPosition(400, SCREEN_HEIGHT * 7 / 8);
        refresh.addListener((new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                updateLobby();
            }
        }));

        stage.addActor(refresh);
        stage.addActor(backButton);
        stage.addActor(start);
    }

    public void updateNames() {
        players = new Table();
        players.setFillParent(true);

        Gdx.app.log("multiplayer", "updating names");
        for(int i = 0; i < maxPlayers ;i++) {
            while (playerNames[i].length() < 10) {
                playerNames[i] = " " + playerNames[i] + " ";
            }

            textButtons[i] = new TextButton(playerNames[i],skinForRoom);
            textButtons[i].getLabel().setFontScale(4);
            players.add(textButtons[i]);
            players.row().pad(10, 0, 10, 0);

        }
        names.addActor(players);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
        names.draw();
        if (startGame) {
            startGame = false;
            parent.multiplayerGameplayScreen.socket = socket;
            parent.multiplayerGameplayScreen.roomHost = roomHost;
            dispose();
            int bet = EvoRunner.savedData.getInteger("bet");
            int coins = EvoRunner.savedData.getInteger("coins");
            EvoRunner.savedData.putInteger("coins", coins - bet);
            parent.changeScreens(EvoRunner.MULTIPLAYERGAMEPLAY_SCREEN);
            parent.multiplayerLobby = new MultiplayerLobby(parent);
        }

        if (mainmenu) {
            mainmenu = false;
            socket.disconnect();
            dispose();
            parent.changeScreens(EvoRunner.MENU_SCREEN);
            parent.multiplayerLobby = new MultiplayerLobby(parent);
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

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        stage.dispose();
        manager.dispose();
    }

    // Sets up socket listeners
    public void configureSocketEvents() {
        socket.on("startGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                try {
                    String start = (String) data.getString("room");
                    if (start.equals(roomHost)) {
                        startGame = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("playerNames", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray data = (JSONArray) args[0];
                try {

                    for (int i = 0; i < data.length(); i++) {
                        playerNames[i] = (String) data.getString(i);
                    }
                    for (int i = data.length(); i < maxPlayers; i++) {
                        playerNames[i] = "Empty";
                    }
                    updateNames();
                } catch (JSONException e) {
                    Gdx.app.log("multiplayer", "unable to update names");
                }
            }
        }).on("maxPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    maxPlayers = (int) data.getInt("maxPlayers");
                } catch (JSONException e) {
                    Gdx.app.log("multiplayer", "unable to get max players");
                }
            }
        }).on("lobbyUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String room = (String) data.getString("id");
                    if (room.equals(roomHost)) {
                        updateLobby();
                    }
                } catch (JSONException e) {
                    Gdx.app.log("multiplayer", "unable to get max players");
                }
            }
        }).on("roomCanceled", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String room = (String) data.getString("roomid");
                    if (room.equals(roomHost)) {
                        mainmenu = true;
                    }
                } catch (JSONException e) {
                    Gdx.app.log("multiplayer", "unable to get max players");
                }
            }
        });
    }

    public void signalStartOfGame() {

        socket.emit("startOfGame",roomHost);
    }
}
