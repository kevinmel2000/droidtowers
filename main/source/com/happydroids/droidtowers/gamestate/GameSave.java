/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gamestate;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.happydroids.droidtowers.DifficultyLevel;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.achievements.Achievement;
import com.happydroids.droidtowers.achievements.AchievementEngine;
import com.happydroids.droidtowers.entities.GridObject;
import com.happydroids.droidtowers.entities.GridObjectPlacementState;
import com.happydroids.droidtowers.entities.Player;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.grid.GridObjectState;
import com.happydroids.droidtowers.grid.GridPositionCache;
import com.happydroids.droidtowers.input.CameraController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "class")
public class GameSave {
  protected int fileGeneration;
  protected String cloudSaveUri;
  protected String towerName;
  protected DifficultyLevel difficultyLevel;
  protected Player player;
  protected Vector3 cameraPosition;
  protected float cameraZoom;
  protected Vector2 gridSize;
  protected List<GridObjectState> gridObjects;
  protected HashMap<String, Integer> objectCounts;
  protected ArrayList<String> completedAchievements;
  private String baseFilename;
  private boolean newGame;
  private GameGrid gameGrid;
  private OrthographicCamera camera;

  public GameSave() {
    newGame = false;
  }

  public GameSave(String towerName, DifficultyLevel difficultyLevel) {
    newGame = true;

    this.towerName = towerName;
    this.difficultyLevel = difficultyLevel;
    baseFilename = generateFilename();
    player = new Player(difficultyLevel.getStartingMoney());
    gridSize = new Vector2(TowerConsts.GAME_GRID_START_SIZE, TowerConsts.GAME_GRID_START_SIZE);
  }

  public void attachToGame(GameGrid gameGrid, OrthographicCamera camera) {
    this.gameGrid = gameGrid;
    this.camera = camera;

    GridPositionCache.reset(gameGrid);
    gameGrid.clearObjects();
    gameGrid.setGridSize(gridSize.x, gridSize.y);
    gameGrid.updateWorldSize();
    System.out.println("gridSize = " + gridSize);

    Player.setInstance(player);

    if (cameraPosition != null) {
      camera.zoom = cameraZoom;
      CameraController.instance().panTo(cameraPosition, false);
      CameraController.instance().checkBounds();
    }

    AchievementEngine.instance().loadCompletedAchievements(completedAchievements);

    if (gridObjects != null) {
      for (GridObjectState gridObjectState : gridObjects) {
        gridObjectState.materialize(gameGrid);
      }
    }
  }

  public void update() {
    gridSize = gameGrid.getGridSize();
    gridObjects = Lists.newArrayList();
    objectCounts = Maps.newHashMap();

    for (GridObject gridObject : gameGrid.getObjects()) {
      if (gridObject.getPlacementState().equals(GridObjectPlacementState.PLACED)) {
        gridObjects.add(new GridObjectState(gridObject));

        String objectName = gridObject.getGridObjectType().getName();
        if (!objectCounts.containsKey(objectName)) {
          objectCounts.put(objectName, 0);
        }

        objectCounts.put(objectName, objectCounts.get(objectName) + 1);
      }
    }
    completedAchievements = Lists.newArrayList();

    for (Achievement achievement : AchievementEngine.instance().getCompletedAchievements()) {
      completedAchievements.add(achievement.getId());
    }

    cameraPosition = camera.position;
    cameraZoom = camera.zoom;

    player = Player.instance();

    fileGeneration += 1;
  }

  public void save(FileHandle gameFile) throws IOException {
    TowerGameService.instance().getObjectMapper().writeValue(gameFile.file(), this);
  }

  public String getCloudSaveUri() {
    return cloudSaveUri;
  }

  private static String generateFilename() {
    return UUID.randomUUID().toString().replaceAll("-", "") + ".json";
  }

  public String getBaseFilename() {
    return baseFilename;
  }

  public static GameSave readFile(FileHandle fileHandle) throws IOException {
//    try {
    GameSave gameSave = TowerGameService.instance().getObjectMapper().readValue(fileHandle.read(), GameSave.class);
    if (gameSave != null) {
      gameSave.baseFilename = fileHandle.name();
    }

    return gameSave;
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
  }

  public boolean isNewGame() {
    return newGame;
  }

  public void setCloudSaveUri(String cloudSaveUri) {
    this.cloudSaveUri = cloudSaveUri;
  }

  public int getFileGeneration() {
    return fileGeneration;
  }
}