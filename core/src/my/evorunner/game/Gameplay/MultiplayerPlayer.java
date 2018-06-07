package my.evorunner.game.Gameplay;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.TimeUtils;

import my.evorunner.game.Constants;
import my.evorunner.game.Gameplay.Enemies.Enemy;
import my.evorunner.game.Gameplay.Obstacles.Obstacle;
import my.evorunner.game.Gameplay.Weapons.Weapons;

import static my.evorunner.game.Constants.SCREEN_HEIGHT;

/**
 * Created by Matthew on 5/19/2018.
 */
// Peter Van & Matthew Phan
// This class is intended to create a multiplayer player
public class MultiplayerPlayer {
    public Vector2 position;
    private Vector2 velocity;
    private Vector2 lastFramePosition;
    private Player.Jumpstate jumpstate;

    private Vector2 spawnLocation;
    private float metersRan;
    int lives;
    public TextureRegion region;
    public Rectangle bounds;

    long walkingStartTime;

    private Player.LifeState lifeState;

    public static float HALF_EVO_RUNNER_WIDTH;
    public static float HALF_EVO_RUNNER_HEIGHT;

    public AvailableCharacters currentChar;
    //Identifiers
    private Animation firstWalkingAnimation;
    private Animation secondWalkingAnimation;
    private Animation thirdWalkingAnimation;

    private boolean firstEvo;
    private boolean secondEvo;
    private boolean thirdEvo;

    private int characterID;
    private boolean evolutionFlag;
    //Weapon
    private Weapons weapons;

    public MultiplayerPlayer() {

    }
    //Added attack animations to constructor
    public MultiplayerPlayer(Vector2 spawnLocation,int characterID, AvailableCharacters currentChar) {
        this.currentChar = currentChar;
        firstWalkingAnimation = currentChar.getFirstWalk(characterID);
        secondWalkingAnimation = currentChar.getSecondWalk(characterID);
        thirdWalkingAnimation = currentChar.getThirdWalk(characterID);
        metersRan = 0;
        evolutionFlag = false;
        this.spawnLocation = spawnLocation;
        this.position = new Vector2();
        this.velocity = new Vector2();
        lastFramePosition = new Vector2();
        lives = 2;      //Lives starts at 2 and loses one in initialize. Perhaps just start at 1?
        lifeState = Player.LifeState.ALIVE;
        region = currentChar.getCharacter(characterID);
        walkingStartTime = TimeUtils.nanoTime();
        HALF_EVO_RUNNER_WIDTH = Constants.SCREEN_WIDTH/50;
        HALF_EVO_RUNNER_HEIGHT = (int)(SCREEN_HEIGHT/50 * Constants.CHARACTER_HEIGHT_TO_WIDTH_RATIO);
        bounds = new Rectangle(
                position.x,
                position.y,
                HALF_EVO_RUNNER_WIDTH * 2,
                HALF_EVO_RUNNER_HEIGHT * 2);

        this.characterID = characterID;
        weapons = new Weapons();
        init();
    }

    public void init() {
        lives -= 1;
        position.set(spawnLocation);

        lastFramePosition.set(spawnLocation);

        velocity.setZero();

        jumpstate = Player.Jumpstate.FALLING;
    }


    public void render(SpriteBatch batch) {
        float walkTimeKeeper = MathUtils.nanoToSec * (TimeUtils.nanoTime() - walkingStartTime);
            if(metersRan <= 250) {
                region = (TextureRegion) firstWalkingAnimation.getKeyFrame(walkTimeKeeper);
            } else if (metersRan <= 350) {
                region = (TextureRegion)Assets.ourInstance.evolutionAssets.evolutionAnimation.getKeyFrame(walkTimeKeeper);
            }else if (metersRan <= 1000) {
                Constants.DEFAULT_ENVIRONENT_VELOCITY = 300;
                region = (TextureRegion) secondWalkingAnimation.getKeyFrame(walkTimeKeeper);
            } else if (metersRan <= 1050) {
                region = (TextureRegion)Assets.ourInstance.evolutionAssets.evolutionAnimation.getKeyFrame(walkTimeKeeper);
            }else if (metersRan > 1200){
                Constants.DEFAULT_ENVIRONENT_VELOCITY = 400;
                region = (TextureRegion) thirdWalkingAnimation.getKeyFrame(walkTimeKeeper);
            }
        batch.draw(
                region.getTexture(),
                position.x,
                position.y,
                0,
                0,
                Constants.SCREEN_WIDTH/24,
                SCREEN_HEIGHT/10,
                1,
                1,
                0,
                region.getRegionX(),
                region.getRegionY(),
                region.getRegionWidth(),
                region.getRegionHeight(),
                false,
                false);
    }

    public void update(float delta, DelayedRemovalArray<Obstacle> obstacles, DelayedRemovalArray<Coin> coins, DelayedRemovalArray<Enemy> enemies) {
        metersRan += .5;
    }

    private boolean landedOnPlatform(Obstacle obstacle) {
        float obstacleHeight = obstacle.getHeight() + obstacle.getPositionY();
        if(lastFramePosition.y  >= obstacleHeight && position.y <  obstacleHeight) {
            float leftFoot = position.x;
            float rightFoot = position.x + HALF_EVO_RUNNER_WIDTH;
            if(obstacle.getPositionX() < leftFoot && obstacle.getPositionX() + obstacle.getWidth() > leftFoot) {
                return true;
            } else if (obstacle.getPositionX() < rightFoot && obstacle.getPositionX() + obstacle.getWidth() > rightFoot) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public float getPositionX() {
        return position.x;
    }
    public float getPositionY() {
        return position.y;
    }

    public float getRegionWidth() {
        return region.getRegionWidth();
    }

    public float getRegionHeight() {
        return region.getRegionHeight();
    }
}
