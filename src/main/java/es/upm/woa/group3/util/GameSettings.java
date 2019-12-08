package es.upm.woa.group3.util;

import java.time.Duration;

public class GameSettings {

  public static final int DEFAULT_X_SIZE = 10;
  public static final int DEFAULT_Y_SIZE = 10;

  public static final String FIRST_TRIBE_NAME = "GROUP3_TRIBE";
  public static final int INIT_NUMBER_OF_UNITS = 1; // todo: change to 3
  public static final int INIT_NUMBER_OF_TRIBES = 1; // todo: change to 3

  public static final int DURATION_OF_UNIT_CREATION = 1500;
  public static final int DURATION_OF_TOWN_HALL_CREATION = 2400;
  public static final Duration DURATION_OF_THE_GAME = Duration.ofSeconds(10000);
  public static final int UNIT_GOLD_PRICE = 150;
  public static final int UNIT_FOOD_PRICE = 50;
  public static final int TOWN_HALL_GOLD_PRICE = 250;
  public static final int TOWN_HALL_STONE_PRICE = 150;
  public static final int TOWN_HALL_WOOD_PRICE = 200;

  public static final int INITIAL_GOLD_NUMBER = 1600; // todo change resources to appropriate
  public static final int INITIAL_STONE_NUMBER = 1600;
  public static final int INITIAL_FOOD_NUMBER = 1600;
  public static final int INITIAL_WOOD_NUMBER = 1600;

  public static final int DURATION_OF_MOVE_TO_CELL = 10000; // todo: change to appropriate number
  public static final String GROUP3_NAME = "GROUP3";
}
