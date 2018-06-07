package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import my.evorunner.game.EvoRunner;
import my.evorunner.game.NativePlatform;

import static my.evorunner.game.Constants.CANNOT_CONNECT_TO_WIFI;
import static my.evorunner.game.Constants.GAMEPLAY_MUSIC;
import static my.evorunner.game.Constants.SCREEN_HEIGHT;
import static my.evorunner.game.Constants.SCREEN_WIDTH;
import static my.evorunner.game.EvoRunner.generator;
import static my.evorunner.game.EvoRunner.parameter;
import static my.evorunner.game.EvoRunner.savedData;

/**
 * Created by Peter on 4/9/2018.
 */
// Peter Van & Matthew Phan
// This class is intended to create screen where players can select whether they want to join an
    // existing multiplayer game or create one.

public class MultiplayerScreen implements Screen {
    //Declare private variables for later use
    private EvoRunner parent;
    private SpriteBatch batch;
    private BitmapFont font;
    private Stage stage;
    private ScrollingBackground scrollingBackground;
    private TextButton.TextButtonStyle menuButton;
    private Random random;
    private CreateRoomScreen createRoomScreen;
    private JoinScreen joinScreen;
    private Socket socket;
    private NativePlatform nativePlatform;

    TextField ipaddress;
    String ip;

    //MenuScreen constructor that takes in an EvoRunner
    public MultiplayerScreen(EvoRunner parentIn, NativePlatform nativePlatform) {
        this.parent = parentIn;     //Declares parents as the evorunner brought in
        scrollingBackground = new ScrollingBackground();    //Declares a new scrolling background
        font = generator.generateFont(parameter);       //Sets font to newly generated font with parameter
        menuButton = new TextButton.TextButtonStyle();        //Sets menuButton as a new text button
        font.getData().setScale(5);         //Sets the scale of font. Increase to increase font size
        menuButton.font = font;         //Sets the menuButtons font as the newly changed font scale
        menuButton.fontColor = Color.WHITE;
        this.nativePlatform = nativePlatform;
        random = new Random();
    }

    //Show method for the menu screen
    @Override
    public void show() {
        menuButton.downFontColor = getRandomColor();
        GAMEPLAY_MUSIC.setLooping(true);
        GAMEPLAY_MUSIC.play();
        parent.adHandler.showAds(false);
        batch = new SpriteBatch();
        //Declares batch as a new spriteBatch for drawing textures

        stage = new Stage(new ScreenViewport());        //Initializes stage using a new screenViewport

        final Table table = new Table();      //Initializes table
        table.setFillParent(true);      //Runs setFillParent to match the table with the stage
        stage.addActor(table);          //Adds table to the stage as an actor

        //Declares text buttons using menuButton
        final TextButton createRoom = new TextButton("Create Room", menuButton);
        TextButton joinRoom = new TextButton("Join Room", menuButton);
        TextButton backButton = new TextButton("<=",menuButton);
        TextButton connect = new TextButton("Connect", menuButton);

        AssetManager manager = new AssetManager();
        manager.load("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        manager.finishLoading();
        TextureAtlas atlas = manager.get("Skins/flat-earth-ui.atlas",TextureAtlas.class);
        Skin skinForRoomNameField = new Skin(Gdx.files.internal("Skins/flat-earth-ui.json"),atlas);
        skinForRoomNameField.getFont("font").getData().setScale(4f,4f);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        font.getData().setScale(5);
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.BLACK;

        ipaddress = new TextField("", skinForRoomNameField);
        ipaddress.setColor(Color.BLUE);
        ipaddress.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                ip = textField.getText().toString();
            }
        });

        backButton.getLabel().setFontScale(6);
        backButton.setPosition(70,SCREEN_HEIGHT * 7 / 8);
        backButton.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (socket != null) {
                    if (socket.connected()) {
                        socket.disconnect();
                    }
                }
                dispose();
                parent.changeScreens(EvoRunner.MENU_SCREEN);
            }
        }));
        //Adds a listener to the start button. Changes to gameplay screen if start
        //is pressed
        createRoom.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (socket != null) {
                    if (socket.connected()) {
                        dispose();
                        createRoomScreen = new CreateRoomScreen(parent, nativePlatform);
                        createRoomScreen.socket = socket;
                        parent.setScreen(createRoomScreen);
                    } else {
                        nativePlatform.displayError(CANNOT_CONNECT_TO_WIFI);
                    }
                }
            }
        }));
        joinRoom.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (socket != null) {
                    if (socket.connected()) {
                        dispose();
                        joinScreen = new JoinScreen(parent);
                        joinScreen.socket = socket;
                        parent.setScreen(joinScreen);
                    } else {
                        nativePlatform.displayError(CANNOT_CONNECT_TO_WIFI);
                    }
                }
            }
        }));

        connect.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                connectSocket();
            }
        }));

        stage.addActor(backButton);
        //Adds start and loot box to the table
        table.add(createRoom);
        table.row().pad(10, 0, 10, 0);
        table.add(joinRoom);
        table.row().pad(10, 0, 10, 0);
        table.add(connect);
        table.row().pad(10,0,10,0);
        table.add(ipaddress).width(SCREEN_WIDTH/4).height(SCREEN_HEIGHT/8);
        Gdx.input.setInputProcessor(stage);     //Sets the input processor to the stage (for taking input)
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        batch.begin();
        scrollingBackground.update(delta,null);
        scrollingBackground.render(batch);
        batch.end();
        stage.draw();
    }

    //Resize method, updates the viewport (for camera)
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        batch.dispose();
        stage.dispose();
    }

    private Color getRandomColor() {
        int colorNumber = random.nextInt(10) + 1;
        Color color;
        switch(colorNumber) {
            case 1:
                color = Color.ORANGE;
                break;
            case 2:
                color = Color.OLIVE;
                break;
            case 3:
                color = Color.BLUE;
                break;
            case 4:
                color = Color.BROWN;
                break;
            case 5:
                color = Color.ROYAL;
                break;
            case 6:
                color = Color.FOREST;
                break;
            case 7:
                color = Color.GOLD;
                break;
            case 8:
                color = Color.CYAN;
                break;
            case 9:
                color = Color.MAROON;
                break;
            case 10:
                color = Color.MAGENTA;
                break;
            default:
                color = Color.ORANGE;
        }
        return color;
    }

    //Multiplayer
    public void connectSocket() {
        try {
            socket = IO.socket("http://" + ip + ":8080");

            if (socket != null) {
                if (!socket.connected()) {
                    socket.connect();
                    sendInformation();
                    configSocketEvents();
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
            }
        }).on("connected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                nativePlatform.displayError("Connected");
            }
        });
    }

    public void sendInformation() {
        JSONObject jsonObject = new JSONObject();
        try {
            //add option for changing playername
            jsonObject.put("character", savedData.getInteger("Character", 0));
            Gdx.app.log("multiplayer", "character id: " + savedData.getInteger("Character"));
            jsonObject.put("playerName", savedData.getString("name", "new Player"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("sendPlayerInformation",jsonObject);

    }
}
