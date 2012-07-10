/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.pathfinding;

import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.grid.GridPosition;

import java.util.LinkedList;
import java.util.List;

import static com.happydroids.droidtowers.types.ProviderType.SERVICE_ELEVATOR;

public class TransitPathFinder extends AStar<GridPosition> {
  protected final GameGrid gameGrid;
  private final boolean canUseServiceRoutes;

  public TransitPathFinder(GameGrid gameGrid, GridPosition start, GridPosition goal, boolean canUseServiceRoutes) {
    super(start, goal);
    this.gameGrid = gameGrid;
    this.canUseServiceRoutes = canUseServiceRoutes;
  }

  @Override
  protected boolean isGoal(GridPosition gridPosition) {
    return goal.x == gridPosition.x && goal.y == gridPosition.y;
  }

  @Override
  protected Double g(GridPosition from, GridPosition to) {
    if (from.equals(to)) {
      return 0.0;
    }

    if (to != null) {
      if (to.elevator != null) {
        if (canUseServiceRoutes && to.elevator.provides(SERVICE_ELEVATOR)) {
          return 0.05;
        }

        return 0.25;
      } else if (to.stair != null) {
        return 0.75;
      } else if (to.connectedToTransit) {
        return 1.0;
      }
    }

    if (to.y == TowerConsts.LOBBY_FLOOR) {
      // fail safe that an avatar that ran away will come back!
      return 2.0;
    }

    return Double.MAX_VALUE;
  }

  @Override
  protected Double h(GridPosition from, GridPosition to) {
    /* Use the Manhattan distance heuristic.  */
//    return (double) Math.abs(goal.x - to.x) + Math.abs(goal.y - to.y);
    return Math.pow(goal.x - to.x, 2) + Math.pow(goal.y - to.y, 2);
  }

  @Override
  protected List<GridPosition> generateSuccessors(GridPosition point) {
    List<GridPosition> successors = new LinkedList<GridPosition>();

    int x = point.x;
    int y = point.y;

    if (point.elevator != null && point.elevator.getNumElevatorCars() > 0) {
      if ((canUseServiceRoutes && point.elevator.provides(SERVICE_ELEVATOR) || !canUseServiceRoutes && !point.elevator.provides(SERVICE_ELEVATOR))) {
        checkGridPositionY(successors, x, y + 1);
        checkGridPositionY(successors, x, y - 1);
      }
    } else if (point.stair != null) {
      checkGridPositionY(successors, x, y + 1);
      checkGridPositionY(successors, x, y - 1);
    }

    checkGridPositionX(successors, x + 1, y);
    checkGridPositionX(successors, x - 1, y);

    return successors;
  }

  private void checkGridPositionX(List<GridPosition> successors, int x, int y) {
    GridPosition position = gameGrid.positionCache().getPosition(x, y);
    if (position != null && (!position.isEmpty() || y == TowerConsts.LOBBY_FLOOR)) {
      successors.add(position);
    }
  }

  private void checkGridPositionY(List<GridPosition> successors, int x, int y) {
    GridPosition position = gameGrid.positionCache().getPosition(x, y);
    if (position != null && (position.connectedToTransit || y == TowerConsts.LOBBY_FLOOR)) {
      if (position.elevator != null && position.elevator.servicesFloor(y)) {
        successors.add(position);
      } else if (position.stair != null) {
        successors.add(position);
      }
    }
  }
}
