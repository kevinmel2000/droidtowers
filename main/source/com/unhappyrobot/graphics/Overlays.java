package com.unhappyrobot.graphics;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.sun.istack.internal.Nullable;
import com.unhappyrobot.entities.CommercialSpace;
import com.unhappyrobot.entities.GridObject;
import com.unhappyrobot.entities.Room;
import com.unhappyrobot.types.CommercialType;
import com.unhappyrobot.types.RoomType;

public enum Overlays {
  NOISE_LEVEL {
    @Override
    public Color getColor(float alpha) {
      return new Color(1, 0, 0, alpha);
    }

    @Override
    public String toString() {
      return "Noise";
    }

    @Override
    public Function<GridObject, Float> getMethod() {
      return null;
    }
  },
  POPULATION_LEVEL {
    @Override
    public Color getColor(float alpha) {
      return new Color(0, 0, 1, alpha);
    }

    @Override
    public String toString() {
      return "Population";
    }

    @Override
    public Function<GridObject, Float> getMethod() {
      return new Function<GridObject, Float>() {
        public Float apply(@Nullable GridObject gridObject) {
          if (gridObject instanceof Room) {
            float populationMax = ((RoomType) gridObject.getGridObjectType()).getPopulationMax();
            if (populationMax > 0f) {
              return ((Room) gridObject).getCurrentResidency() / populationMax;
            }
          }

          return null;
        }
      };
    }
  },
  EMPLOYMENT_LEVEL {
    @Override
    public Color getColor(float alpha) {
      return new Color(0, 1, 0, alpha);
    }

    @Override
    public String toString() {
      return "Employment";
    }

    @Override
    public Function<GridObject, Float> getMethod() {
      return new Function<GridObject, Float>() {
        public Float apply(@Nullable GridObject gridObject) {
          if (gridObject instanceof CommercialSpace) {
            float jobsProvided = ((CommercialType) gridObject.getGridObjectType()).getJobsProvided();
            if (jobsProvided > 0f) {
              return ((CommercialSpace) gridObject).getJobsFilled() / jobsProvided;
            }
          }

          return null;
        }
      };
    }
  },
  DESIRABILITY_LEVEL {
    @Override
    public Color getColor(float alpha) {
      return new Color(0.5f, 0.25f, 0.6f, alpha);
    }

    @Override
    public String toString() {
      return "Desirability";
    }

    @Override
    public Function<GridObject, Float> getMethod() {
      return new Function<GridObject, Float>() {
        public Float apply(@Nullable GridObject gridObject) {
          if (gridObject != null) {
            return gridObject.getDesirability();
          }

          return null;
        }
      };
    }
  };

  public abstract String toString();

  public abstract Color getColor(float alpha);

  public abstract Function<GridObject, Float> getMethod();
}