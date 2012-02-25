package com.unhappyrobot.types;

import com.unhappyrobot.entities.GridObject;
import com.unhappyrobot.entities.Stair;
import com.unhappyrobot.grid.GameGrid;

public class StairType extends TransitType {
  @Override
  public GridObject makeGridObject(GameGrid gameGrid) {
    return new Stair(this, gameGrid);
  }

  @Override
  public boolean canBeAt(GridObject gridObject) {
    return checkForOverlap(gridObject);
  }

  @Override
  public int getCoinsEarned() {
    return 0;
  }

  @Override
  public boolean connectsToFloor(GridObject gridObject, float floor) {
    return floor == gridObject.getPosition().y || floor == gridObject.getPosition().y + 1;
  }

  @Override
  public int getZIndex() {
    return 80;
  }
}
