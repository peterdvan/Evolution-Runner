package my.evorunner.game;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import my.evorunner.game.Gameplay.GameplayScreen;
import my.evorunner.game.Gameplay.MultiplayerGameplayScreen;
import my.evorunner.game.Gameplay.MultiplayerLobby;
import my.evorunner.game.Gameplay.MultiplayerScreen;
import my.evorunner.game.Shop.OwnedSkinScreen;
import my.evorunner.game.Shop.ShopScreen;

/**
 * Main game class (EvoRunner.java)
 *
 * Version 1.1
 * Last Edited: Matthew Phan
 * Changelog 1.1
 * Added owned skin screen
 *
 * Changelog 1.0
 * Changed path slkscr.ttf from Skins to Fonts
 */

//This class holds all objects and initializes them and givs them
	// a way to reference each other
public class EvoRunner extends Game{
	//Declare integer constants for the different screens
	public final static int MENU_SCREEN = 0;
	public final static int GAMEPLAY_SCREEN = 1;
	public final static int SETTINGS_SCREEN = 2;
	public final static int END_SCREEN = 3;
	public final static int SHOP_SCREEN = 4;
	public final static int CHARACTERS_SCREEN = 5;
	public final static int MULTIPLAYER_SCREEN = 6;
	public final static int MULTIPLAYER_LOBBY = 7;
	public final static int MULTIPLAYERGAMEPLAY_SCREEN = 8;
	public final static int LOSE_SCREEN = 9;
	public final static int WIN_SCREEN = 10;
	//Declare screens for menu, game play, app preferences, end screen, and shop
	private MenuScreen menuScreen;
	private GameplayScreen gameplayScreen;
	private AppPreferences appPreferences;
	private EndScreen endScreen;
	private ShopScreen shopScreen;
	private OwnedSkinScreen characterScreen;
	private MultiplayerScreen multiplayerScreen;
	public MultiplayerLobby multiplayerLobby;
	public MultiplayerGameplayScreen multiplayerGameplayScreen;
	public LoseScreen loseScreen;
	public WinScreen winScreen;
	//Declare adHandler
	public AdHandler adHandler;
	public NativePlatform nativePlatform;
	//Declare FTF generators for font
	public static FreeTypeFontGenerator generator;
	public static FreeTypeFontGenerator.FreeTypeFontParameter parameter;

	//Declare preferences
	public static Preferences savedData;

	//Declare int for coins
	public static int COINS;

	//EvoRunner constructor with adHandler
	public EvoRunner(AdHandler handler,NativePlatform nativePlatform) {
		this.adHandler = handler;
		this.nativePlatform = nativePlatform;
		adHandler.showAds(true);
	}

	//Create function. Runs loadData and initializes the generator, parameter
	//Screens and appPreferences
	@Override
	public void create() {
		loadData();
		generator = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/slkscr.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		gameplayScreen = new GameplayScreen(this);
		menuScreen = new MenuScreen(this);
		endScreen = new EndScreen(this);
		shopScreen = new ShopScreen(this);
		appPreferences = new AppPreferences();
		characterScreen = new OwnedSkinScreen(this);
		multiplayerScreen = new MultiplayerScreen(this,nativePlatform);
		multiplayerLobby = new MultiplayerLobby(this);
		multiplayerGameplayScreen = new MultiplayerGameplayScreen(this);
		winScreen= new WinScreen(this);
		loseScreen = new LoseScreen(this);
		setScreen(menuScreen);		//Sets the screen to the menu
	}

	//Loads the users save data
	private void loadData() {
		savedData = Gdx.app.getPreferences("Saved Data");
		COINS = savedData.getInteger("coins",100);
	}

	//Changes screens based on the integer brought in as parameter
	public void changeScreens(int screen) {
		switch (screen) {
			case MENU_SCREEN:
				this.setScreen(menuScreen);
				break;
			case GAMEPLAY_SCREEN:
				this.setScreen(gameplayScreen);
				break;
			case END_SCREEN:
				this.setScreen(endScreen);
				break;
			case SHOP_SCREEN:
				this.setScreen(shopScreen);
				break;
			case CHARACTERS_SCREEN:
				this.setScreen(characterScreen);
				break;
			case MULTIPLAYER_SCREEN:
				this.setScreen(multiplayerScreen);
				break;
			case MULTIPLAYER_LOBBY:
				this.setScreen(multiplayerLobby);
				break;
			case MULTIPLAYERGAMEPLAY_SCREEN:
				this.setScreen(multiplayerGameplayScreen);
				break;
			case WIN_SCREEN:
				this.setScreen(winScreen);
				break;
			case LOSE_SCREEN:
				this.setScreen(loseScreen);
				break;
		}
	}

	public AppPreferences getPreferences() {
		return appPreferences;
	}
	public float getMeters() {
		return  gameplayScreen.getMeters();
	}

	//Uses parent class render function
	@Override
	public void render() {
		super.render();
	}
}
