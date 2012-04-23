/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.achievements;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import com.happydroids.droidtowers.events.ElevatorHeightChangeEvent;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.gui.TutorialStepNotification;

import java.io.IOException;
import java.util.ArrayList;

public class TutorialEngine extends AchievementEngine {
  private static TutorialEngine instance;
  private boolean enabled;

  public static TutorialEngine instance() {
    if (instance == null) {
      instance = new TutorialEngine();
    }

    return instance;
  }

  protected TutorialEngine() {
    try {
      ObjectMapper mapper = TowerGameService.instance().getObjectMapper();
      achievements = mapper.readValue(Gdx.files.internal("params/tutorial-steps.json").reader(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, TutorialStep.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void checkAchievements() {
    if (enabled) {
      super.checkAchievements();
    }
  }

  public void moveToStepWhenReady(String stepId) {
    complete(stepId);
  }

  @Override
  protected void displayNotification(Achievement achievement) {
    if (enabled) {
      new TutorialStepNotification((TutorialStep) achievement).show();
    }
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    resetState();
  }

  @Subscribe
  public void Elevator_onHeightChange(ElevatorHeightChangeEvent event) {
    moveToStepWhenReady("tutorial-pan");
  }
}
