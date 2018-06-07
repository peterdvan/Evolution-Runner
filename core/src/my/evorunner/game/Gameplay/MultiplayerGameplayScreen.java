package my.evorunner.game.Gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import my.evorunner.game.EvoRunner;
import my.evorunner.game.Gameplay.Enemies.Enemies;
import my.evorunner.game.Gameplay.Obstacles.Obstacles;
import my.evorunner.game.Gameplay.Skill_Buttons.ButtonTable;
import my.evorunner.game.Gameplay.Skill_Buttons.GameTime;

import static my.evorunner.game.Constants.COIN_PICTURE;
import static my.evorunner.game.Constants.DEFAULT_ENVIRONENT_VELOCITY;
import static my.evorunner.game.Constants.GAMEPLAY_MUSIC;
import static my.evorunner.game.Constants.SCREEN_HEIGHT;
import static my.evorunner.game.Constants.SCREEN_WIDTH;
import static my.evorunner.game.Constants.STARTING_POSITION;
import static my.evorunner.game.EvoRunner.generator;
import static my.evorunner.game.EvoRunner.parameter;
import static my.evorunner.game.EvoRunner.savedData;

/**
 * Version 1.1
 * Last Edited: Matthew Phan
 * Changelog 1.1
 * Removed multi input processor and replaced it with button table
 * button table now holds a button for jump and attack
 * second render method added to change the player sprite when attacking
 *
 */

// Peter Van & Matthew Phan
// This class is intended to create a multiplayer environment screen

public class MultiplayerGameplayScreen implements Screen{
    //UI Elements
    ButtonTable buttonTable;
    public Stage stage;
    public Stage buttonTableStage;
    public Socket socket;
    private TextButton.TextButtonStyle menuButton;
    private BitmapFont font;
    //Other
    private EvoRunner parent;
    private ScrollingBackground scrollingBackground;
    private SpriteBatch batch;
    private ScreenViewport viewport;
    private Player player;

    private Enemies enemies;
    private Obstacles obstacles;
    private Coins coins;
    private IntroductionNPC introductionNPC;

    //Multiplayer Stuff
    HashMap<String, MultiplayerPlayer> friendlyPlayers;
    public final float UPDATE_TIME = 1/30f;
    public float timer;
    public String roomHost = "";
    public int playerCount = 0;
    /////////

    OrthographicCamera camera;
    ScreenViewport view;

    private int width;
    private int height;
    private Vector2 position;
    private TextButton.TextButtonStyle wordFont;
    private TextButton text;
    private TextButton text2;
    private ImageButton textBubble;

    private Texture myTexture;
    private TextureRegion myTextureRegion;
    private TextureRegionDrawable myTextureRegionDrawable;

    public MultiplayerGameplayScreen(EvoRunner evoParent) {
        this.parent = evoParent;
        scrollingBackground = new ScrollingBackground();
        this.parent.adHandler.showAds(true);
        batch = new SpriteBatch();
        friendlyPlayers = new HashMap<String, MultiplayerPlayer>();
        font = generator.generateFont(parameter);       //Sets font to newly generated font with parameter
        menuButton = new TextButton.TextButtonStyle();        //Sets menuButton as a new text button
        font.getData().setScale(5);         //Sets the scale of font. Increase to increase font size
        menuButton.font = font;         //Sets the menuButtons font as the newly changed font scale
        menuButton.fontColor = Color.WHITE;
    }

    public float getMeters() {
        return player.getMetersRan();
    }
    @Override
    public void show() {
        DEFAULT_ENVIRONENT_VELOCITY = 200;
        AssetManager am = new AssetManager();
        Assets.ourInstance.init(am);
        batch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(0, -200, SCREEN_WIDTH, SCREEN_HEIGHT);

        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);

        view = new ScreenViewport(camera);
        camera.position.add(0, -200, 0);
        viewport = new ScreenViewport();
        obstacles = new Obstacles(viewport);

        player = new Player(STARTING_POSITION, savedData.getInteger("Character", 0));

        enemies = new Enemies(viewport);
        parameter.size = 24;
        coins = new Coins(viewport);

        GAMEPLAY_MUSIC.setLooping(true);
        GAMEPLAY_MUSIC.play();
        setUpUI();

        width = SCREEN_WIDTH / 18;
        height = SCREEN_HEIGHT / 10;

        font = generator.generateFont(parameter);
        wordFont = new TextButton.TextButtonStyle();
        wordFont.font = font;

        position = new Vector2(SCREEN_WIDTH * 1.5f,0);

        text = new TextButton("The last guy to escape ran " ,wordFont);
        text2 = new TextButton(savedData.getFloat("HighScore", 0) + " meters. Can you escape?", wordFont);
        text.getLabel().setFontScale(1);
        text2.getLabel().setFontScale(1);

        myTexture = new Texture(Gdx.files.internal("Items/TextBubble.png"));
        myTextureRegion = new TextureRegion(myTexture);
        myTextureRegionDrawable = new TextureRegionDrawable(myTextureRegion);
        textBubble = new ImageButton(myTextureRegionDrawable); //Set the button up

        configSocketEvents();
        requestPlayerInformation();
    }

    private void setUpUI() {
        stage = new Stage(view);
        buttonTableStage = new Stage();
        buttonTable = new ButtonTable(player, buttonTableStage);

        introductionNPC = new IntroductionNPC();
        TextButton backButton = new TextButton("<=",menuButton);
        backButton.getLabel().setFontScale(3);
        backButton.setPosition(100,SCREEN_HEIGHT * 7 / 8);
        backButton.addListener((new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (socket.connected()) {
                    socket.disconnect();
                }
                dispose();
                parent.changeScreens(EvoRunner.MENU_SCREEN);
                parent.multiplayerGameplayScreen = new MultiplayerGameplayScreen(parent);
            }
        }));
        buttonTableStage.addActor(backButton);
        Gdx.input.setInputProcessor(buttonTableStage);
    }
    @Override
    public void hide() {
        GAMEPLAY_MUSIC.stop();
    }

    @Override
    public void render(float delta) {
        camera.update();
        position.x -= DEFAULT_ENVIRONENT_VELOCITY * delta;
        updateServer(Gdx.graphics.getDeltaTime());
        text.setPosition(position.x + (width / 2) + 5,position.y + height + 20 + 200);
        text2.setPosition(position.x + (width / 2) + 5,position.y + height + 5 + 200);
        textBubble.setPosition(position.x + (width / 2), position.y + height + 200);
        stage.addActor(textBubble);
        stage.addActor(text);
        stage.addActor(text2);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateComponents(delta);

        GameTime.updateCurrentTime(Gdx.graphics.getDeltaTime());


        batch.begin();
        scrollingBackground.render(batch);
        obstacles.render(batch);
        coins.render(batch);
        enemies.render(batch);


        for (HashMap.Entry<String, MultiplayerPlayer> entry : friendlyPlayers.entrySet()) {
            entry.getValue().render(batch);
        }
        player.render(batch);

        drawInformation(batch);
        introductionNPC.render(batch,delta);
        batch.end();
        buttonTableStage.draw();

        stage.draw();
    }

    private void updateComponents(float delta) {
        endGame();
        increaseDifficulty();
        scrollingBackground.update(delta,player);
        enemies.update(delta,player);
        coins.update(delta, obstacles.getObstacleList());

        obstacles.update(delta,player);
        player.update(delta,obstacles.getObstacleList(),coins.getCoinsList(),enemies.getEnemiesList());
        //  buttonTable.update();
    }

    public void updateServer(float deltaTime) {
        timer += deltaTime;
        if (timer >= UPDATE_TIME && player != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", player.position.x);
                data.put("y", player.position.y);
                data.put("roomHost", roomHost);
                socket.emit("updateServer", data);
            } catch(JSONException e) {
                Gdx.app.log("multiplayer", "Error sending update data");
            }
        }
    }

    public void playerDied() {
        JSONObject data = new JSONObject();
        try {
            data.put("death", socket.id());
            socket.emit("playerDied", data);
        } catch (JSONException e) {

        }
    }

    public void requestPlayerInformation() {
        JSONObject data = new JSONObject();
        try {
            data.put("roomHost", roomHost);
            socket.emit("requestPlayerInformation", data);
        } catch (JSONException e) {

        }
    }

    @Override
    public void resize(int width, int height) {
        scrollingBackground.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public void dispose() {
        batch.dispose();
        Gdx.input.setInputProcessor(null);
        stage.dispose();
    }

    private void endGame () {
        if(player.checkLives() <= 0 ) {
            if (player.isFinished()) {
                playerDied();


                socket.disconnect();
                dispose();
                //Change to lose screen
                if(friendlyPlayers.isEmpty()) {
                    int coins = savedData.getInteger("coins");
                    int bet = savedData.getInteger("bet") * playerCount;
                    savedData.putInteger("coins", coins + bet);
                    parent.changeScreens(EvoRunner.WIN_SCREEN);
                } else {
                    parent.changeScreens(EvoRunner.LOSE_SCREEN);
                }
                friendlyPlayers = new HashMap<String, MultiplayerPlayer>();
                DEFAULT_ENVIRONENT_VELOCITY = 200;
            }
        }
    }
    private void drawInformation(SpriteBatch batch) {
        font.draw(batch, player.getMetersRan() + "m",0,SCREEN_HEIGHT - parent.adHandler.getBannerHeight());
        batch.draw(
                COIN_PICTURE.getTexture(),
                SCREEN_WIDTH - SCREEN_WIDTH/7 - SCREEN_WIDTH/12,
                SCREEN_HEIGHT - SCREEN_HEIGHT/14 - parent.adHandler.getBannerHeight(),
                0,
                0,
                SCREEN_WIDTH/12,
                SCREEN_HEIGHT/7,
                1,
                1,
                0,
                COIN_PICTURE.getRegionX(),
                COIN_PICTURE.getRegionY(),
                COIN_PICTURE.getRegionWidth(),
                COIN_PICTURE.getRegionHeight(),
                false,
                false);
        font.draw(batch, Integer.toString(player.getCoins()),SCREEN_WIDTH - SCREEN_WIDTH/7,SCREEN_HEIGHT - parent.adHandler.getBannerHeight());
    }

    private void increaseDifficulty() {
        if(player.getMetersRan() % 500 == 0 &&  player.getMetersRan() <= 3000) {
            enemies.generateMonster();
        }
        if(player.getMetersRan() == 500 || player.getMetersRan() == 1500 ) {
            enemies.addBat();
        }
    }

    public void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
            }
        }).on("playerDisconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    friendlyPlayers.remove(id);
                    playerCount++;
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error removing player");
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {

                        JSONObject temp = objects.getJSONObject(i);
                        if (!socket.id().equals((String) temp.getString("playerID"))) {
                            //USE THIS TO DETERMINE WHICH TEXTURE TO USE
                            //integer representing character image
                            MultiplayerPlayer coopPlayer = new MultiplayerPlayer(STARTING_POSITION, (int)temp.getInt("character"), player.currentChar);
                            coopPlayer.position.x = 0;
                            coopPlayer.position.y = 0;
                            friendlyPlayers.put((String)temp.getString("playerID"), coopPlayer);
                        }
                    }
                } catch(JSONException e) {

                }
            }
        }).on("updateClient", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < friendlyPlayers.size() + 1; i++) {
                        JSONObject temp = objects.getJSONObject(i);



                        if (!socket.id().equals((String) temp.getString("playerID"))) {
                            Gdx.app.log("multiplayer","updating");
                            if (friendlyPlayers.get(temp.getString("playerID")) != null) {

                                friendlyPlayers.get(temp.getString("playerID")).position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                                friendlyPlayers.get(temp.getString("playerID")).position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                            }
                        }
                    }

                } catch(JSONException e) {

                }
            }
        });

    }

}
