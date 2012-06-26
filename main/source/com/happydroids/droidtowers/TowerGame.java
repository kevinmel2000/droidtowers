/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.happydroids.droidtowers.achievements.AchievementEngine;
import com.happydroids.droidtowers.actions.ActionManager;
import com.happydroids.droidtowers.audio.GameSoundController;
import com.happydroids.droidtowers.controllers.PathSearchManager;
import com.happydroids.droidtowers.entities.GameObject;
import com.happydroids.droidtowers.gamestate.server.CloudGameSaveCollection;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.gui.*;
import com.happydroids.droidtowers.input.*;
import com.happydroids.droidtowers.platform.Display;
import com.happydroids.droidtowers.scenes.*;
import com.happydroids.droidtowers.scenes.components.SceneManager;
import com.happydroids.droidtowers.tween.GameObjectAccessor;
import com.happydroids.droidtowers.tween.TweenSystem;
import com.happydroids.droidtowers.types.*;
import com.happydroids.platform.Platform;
import com.happydroids.platform.PlatformConnectionMonitor;
import com.happydroids.utils.BackgroundTask;

import java.net.URI;

import static com.badlogic.gdx.Application.ApplicationType.Android;
import static com.badlogic.gdx.Application.ApplicationType.Desktop;
import static com.happydroids.HappyDroidConsts.DEBUG;
import static com.happydroids.HappyDroidConsts.DISPLAY_DEBUG_INFO;

public class TowerGame implements ApplicationListener, BackgroundTask.PostExecuteManager {
  private static final String TAG = TowerGame.class.getSimpleName();

  private SpriteBatch spriteBatch;
  private BitmapFont menloBitmapFont;
  private static Stage rootUiStage;
  private SpriteBatch spriteBatchFBO;
  private FrameBuffer frameBuffer;
  private static GameSoundController soundController;

  private static CloudGameSaveCollection cloudGameSaves;
  private PlatformConnectionMonitor platformConnectionMonitor;

  public TowerGame() {
    cloudGameSaves = new CloudGameSaveCollection();
  }

  public static CloudGameSaveCollection getCloudGameSaves() {
    return cloudGameSaves;
  }

  public void create() {
    Gdx.app.error("lifecycle", "create");
    if (Gdx.app.getType().equals(Desktop)) {
      Preferences displayPrefs = Gdx.app.getPreferences("DISPLAY");
      if (displayPrefs.contains("width") && displayPrefs.contains("height")) {
        Gdx.graphics.setDisplayMode(displayPrefs.getInteger("width"), displayPrefs.getInteger("height"), false);
      }
    }

    BackgroundTask.setPostExecuteManager(this);
    BackgroundTask.setUncaughtExceptionHandler(Platform.uncaughtExceptionHandler);
    Thread.currentThread().setUncaughtExceptionHandler(Platform.uncaughtExceptionHandler);

    TowerGameService.setInstance(new TowerGameService());
    new RegisterDeviceTask().run();

    if (Gdx.graphics.isGL20Available() && Gdx.app.getType().equals(Android) && Display.isHDPIMode()) {
      float displayScalar = 0.75f;
      frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int) (Gdx.graphics.getWidth() * displayScalar), (int) (Gdx.graphics.getHeight() * displayScalar), true);
      spriteBatchFBO = new SpriteBatch();
    }

    soundController = new GameSoundController();

    if (DEBUG) {
      Gdx.app.error("DEBUG", "Debug mode is enabled!");
      Gdx.app.setLogLevel(Application.LOG_DEBUG);
    } else {
      Gdx.app.setLogLevel(Application.LOG_ERROR);
    }

    Thread.currentThread().setUncaughtExceptionHandler(Platform.uncaughtExceptionHandler);
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

    TowerAssetManager.assetManager();

    TowerGameService.instance().afterAuthentication(new Runnable() {
      @Override
      public void run() {
        if (!TowerGameService.instance().isAuthenticated()) {
          return;
        }

        new BackgroundTask() {
          @Override
          protected void execute() throws Exception {
            cloudGameSaves.fetch();
          }
        }.run();
      }
    });

    RoomTypeFactory.instance();
    CommercialTypeFactory.instance();
    ServiceRoomTypeFactory.instance();
    ElevatorTypeFactory.instance();
    StairTypeFactory.instance();

    AchievementEngine.instance();
    Tween.setCombinedAttributesLimit(4);
    Tween.registerAccessor(CameraController.class, new CameraControllerAccessor());
    Tween.registerAccessor(GameObject.class, new GameObjectAccessor());
    Tween.registerAccessor(Actor.class, new WidgetAccessor());

    menloBitmapFont = new BitmapFont(Gdx.files.internal("fonts/menlo_14_bold_white.fnt"), false);
    spriteBatch = new SpriteBatch();
    rootUiStage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, spriteBatch);

    Gdx.input.setInputProcessor(InputSystem.instance());
    InputSystem.instance().addInputProcessor(rootUiStage, 0);

    if (DEBUG) {
      InputSystem.instance().addInputProcessor(new DebugInputAdapter(), 1000);
    }

    InputSystem.instance().bind(new int[]{InputSystem.Keys.BACK, InputSystem.Keys.ESCAPE}, new InputCallback() {
      public boolean run(float timeDelta) {
        final boolean mainMenuIsActive = SceneManager.getActiveScene() instanceof MainMenuScene;

        new Dialog(rootUiStage)
                .setTitle("Awe, don't leave me.")
                .setMessage("Are you sure you want to exit " + (mainMenuIsActive ? "the game?" : "to the Main Menu?"))
                .addButton(ResponseType.POSITIVE, "Yes", new OnClickCallback() {
                  @Override
                  public void onClick(Dialog dialog) {
                    dialog.dismiss();
                    if (mainMenuIsActive) {
                      Gdx.app.exit();
                    } else {
                      SceneManager.changeScene(MainMenuScene.class);
                    }
                  }
                })
                .addButton(ResponseType.NEGATIVE, "No way!", new OnClickCallback() {
                  @Override
                  public void onClick(Dialog dialog) {
                    dialog.dismiss();
                  }
                })
                .show();

        return true;
      }
    });

    Scene.setSpriteBatch(spriteBatch);


    if (Platform.protocolHandler != null && Platform.protocolHandler.hasUri()) {
      SceneManager.changeScene(LaunchUriScene.class, Platform.protocolHandler.consumeUri());
    } else {
      SceneManager.changeScene(MainMenuScene.class);
    }
  }

  public void render() {
    Gdx.gl.glClearColor(0.48f, 0.729f, 0.870f, 1.0f);
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);

    float deltaTime = Gdx.graphics.getDeltaTime();

    SceneManager.getActiveScene().getCamera().update();
    ActionManager.instance().update(deltaTime);
    InputSystem.instance().update(deltaTime);
    PathSearchManager.instance().update(deltaTime);
    TweenSystem.manager().update((int) (deltaTime * 1000 * SceneManager.getActiveScene().getTimeMultiplier()));
    soundController.update(deltaTime);

    spriteBatch.setProjectionMatrix(SceneManager.getActiveScene().getCamera().combined);

    if (frameBuffer != null) {
      frameBuffer.begin();
      Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
      SceneManager.getActiveScene().render(deltaTime);
      frameBuffer.end();

      spriteBatchFBO.begin();
      spriteBatchFBO.draw(frameBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
      spriteBatchFBO.end();
    } else {
      SceneManager.getActiveScene().render(deltaTime);
    }

    SceneManager.getActiveScene().getStage().act(deltaTime);
    SceneManager.getActiveScene().getStage().draw();

    rootUiStage.act(deltaTime);
    rootUiStage.draw();

    //noinspection PointlessBooleanExpression
    if (DEBUG && DISPLAY_DEBUG_INFO) {
      Table.drawDebug(SceneManager.getActiveScene().getStage());
      Table.drawDebug(rootUiStage);

      float javaHeapInBytes = Gdx.app.getJavaHeap() / TowerConsts.ONE_MEGABYTE;
      float nativeHeapInBytes = Gdx.app.getNativeHeap() / TowerConsts.ONE_MEGABYTE;

      String infoText = String.format("fps: %02d, camera(%.1f, %.1f, %.1f)\nmem: (java %.1f Mb, native %.1f Mb, gpu %.1f Mb)",
                                             Gdx.graphics.getFramesPerSecond(),
                                             SceneManager.getActiveScene().getCamera().position.x,
                                             SceneManager.getActiveScene().getCamera().position.y,
                                             SceneManager.getActiveScene().getCamera().zoom,
                                             javaHeapInBytes,
                                             nativeHeapInBytes,
                                             TowerAssetManager.assetManager().getMemoryInMegabytes());
      spriteBatch.begin();
      menloBitmapFont.drawMultiLine(spriteBatch, infoText, 5, 35);
      spriteBatch.end();
    }
  }

  public void resize(int width, int height) {
    Gdx.app.log("lifecycle", "resizing!");
    SceneManager.getActiveScene().getCamera().viewportWidth = width;
    SceneManager.getActiveScene().getCamera().viewportHeight = height;
    spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    SceneManager.getActiveScene().getSpriteBatch().getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    Gdx.gl.glViewport(0, 0, width, height);
  }


  public void pause() {
    Gdx.app.error("lifecycle", "pausing!");
    SceneManager.getActiveScene().pause();
  }

  public void resume() {
    Gdx.app.error("lifecycle", "resuming!");

    if (!(SceneManager.getActiveScene() instanceof SplashScene)) {
      SceneManager.pushScene(ApplicationResumeScene.class);
    }

    if (Platform.protocolHandler != null && Platform.protocolHandler.hasUri()) {
      URI launchUri = Platform.protocolHandler.consumeUri();
      SceneManager.changeScene(LaunchUriScene.class, launchUri);
    }
  }

  public void dispose() {
    Gdx.app.error("lifecycle", "dispose");
    SceneManager.getActiveScene().dispose();

    rootUiStage = null;
    Platform.dispose();

    SceneManager.dispose();
    spriteBatch.dispose();
    BackgroundTask.dispose();
    TowerAssetManager.dispose();
    FontManager.resetAll();

    System.exit(0);
  }

  public static Stage getRootUiStage() {
    return rootUiStage;
  }


  public static GameSoundController getSoundController() {
    return soundController;
  }

  public void postRunnable(Runnable runnable) {
    Gdx.app.postRunnable(runnable);
  }
}
