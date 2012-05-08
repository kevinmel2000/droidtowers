/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.grid;

import com.badlogic.gdx.math.Vector2;
import com.happydroids.droidtowers.entities.Elevator;
import com.happydroids.droidtowers.entities.GridObject;
import com.happydroids.droidtowers.entities.GuavaSet;
import com.happydroids.droidtowers.entities.Stair;
import com.happydroids.droidtowers.math.GridPoint;

public class GridPosition {
  public final int x;
  public final int y;
  private GuavaSet<GridObject> objects = new GuavaSet<GridObject>();
  public Elevator elevator;
  public Stair stair;
  public boolean connectedToTransit;
  public float distanceFromTransit;
  private float maxNoiseLevel;
  private float noiseLevel;
  public float normalizedDistanceFromTransit;

  public GridPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public GuavaSet<GridObject> getObjects() {
    return objects;
  }

  public void add(GridObject gridObject) {
    if (!objects.contains(gridObject) && objects.add(gridObject)) {
      if (gridObject instanceof Elevator) {
        GridPoint position = gridObject.getPosition();
        GridPoint size = gridObject.getSize();
        if (position.x == x && (position.y == y || position.y + size.y == y)) {
          return;
        }

        elevator = (Elevator) gridObject;
      } else if (gridObject instanceof Stair) {
        stair = (Stair) gridObject;
      }
    } else {
      System.out.println("Could not add: " + gridObject);
    }
  }

  public void remove(GridObject gridObject) {
    if (objects.remove(gridObject)) {
      if (gridObject instanceof Elevator) {
        elevator = null;
      } else if (gridObject instanceof Stair) {
        stair = null;
      }
    } else {
      System.out.println("Could not remove: " + gridObject);
    }
  }

  public int size() {
    return objects.size();
  }

  public boolean isEmpty() {
    return objects.isEmpty();
  }

  public boolean contains(GridObject gridObject) {
    return objects.contains(gridObject);
  }

  public Vector2 toWorldVector2() {
    return new GridPoint(x, y).toWorldVector2();
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GridPosition)) return false;

    GridPosition that = (GridPosition) o;

    if (connectedToTransit != that.connectedToTransit) return false;
    if (x != that.x) return false;
    if (y != that.y) return false;
    if (elevator != null ? !elevator.equals(that.elevator) : that.elevator != null) return false;
    if (objects != null ? !objects.equals(that.objects) : that.objects != null) return false;
    if (stair != null ? !stair.equals(that.stair) : that.stair != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    result = 31 * result + (objects != null ? objects.hashCode() : 0);
    result = 31 * result + (connectedToTransit ? 1 : 0);
    result = 31 * result + (elevator != null ? elevator.hashCode() : 0);
    result = 31 * result + (stair != null ? stair.hashCode() : 0);
    return result;
  }

  public void findMaxNoise() {
    for (GridObject gridObject : objects) {
      maxNoiseLevel = Math.max(gridObject.getNoiseLevel(), maxNoiseLevel);
    }
  }

  public float getMaxNoiseLevel() {
    return maxNoiseLevel;
  }

  public void calculateNoise(GridPosition[][] gridPositions) {
    float totalNoise = 0f;
    int distance = 2;
    for (int xx = x - distance; xx < x + distance; xx++) {
      for (int yy = y - distance; yy < y + distance; yy++) {
        if (xx < 0 || yy < 0 || xx >= gridPositions.length || yy >= gridPositions[xx].length) continue;
        if (xx == x && yy == y) continue;

        totalNoise += gridPositions[xx][yy].getMaxNoiseLevel();
      }
    }

    noiseLevel = Math.min(1f, getMaxNoiseLevel() + totalNoise / 8);
  }

  public float getNoiseLevel() {
    return noiseLevel;
  }
}
