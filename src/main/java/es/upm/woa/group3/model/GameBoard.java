package es.upm.woa.group3.model;

import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;

import java.util.Optional;
import java.util.Random;

import static es.upm.woa.group3.util.GameSettings.DEFAULT_X_SIZE;
import static es.upm.woa.group3.util.GameSettings.DEFAULT_Y_SIZE;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class GameBoard {
  private final Logger logger = Logger.getLogger(GameBoard.class.getSimpleName());
  private static final GameBoard INSTANCE = new GameBoard(DEFAULT_X_SIZE, DEFAULT_Y_SIZE);
  private Cell[][] board;
  private final Random random = new Random();

  private GameBoard(int xSize, int ySize) {
    if (xSize < 0 || ySize < 0) {
      throw new IllegalArgumentException("Wrong size of game board");
    }
    this.board = new Cell[xSize][ySize];
    initCells();
    logger.log(
            String.format(
                    "Game board has been created with size is: [%dx%d]", DEFAULT_X_SIZE, DEFAULT_Y_SIZE));
  }

  public static GameBoard getInstance() {
    return INSTANCE;
  }

  private void initCells() {
    // init cells only in even pairs (2,2), (2, 4), <4,2>
    // and only in odd pairs, <1,1>, <1,3>, <3,1>
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[0].length; j++) {
        if (canCreateCell(i, j)) {
          Cell cell = new Cell();
          cell.setX(i);
          cell.setY(j);
          cell.setContent(new Building()); //TODO: why we set Building in every cell during initialization? Is every content a building?
          this.board[i][j] = cell;
        }
      }
    }
  }

  private boolean canCreateCell(int i, int j) {
    boolean evenPair = i % 2 == 0 && j % 2 == 0;
    boolean oddPair = i % 2 == 1 && j % 2 == 1;
    return evenPair || oddPair;
  }

  public Cell putTribeAndTownHall(Tribe tribe) {
    Cell cell = requireNonNull(getRandomPosition());
    Building building = new Building();
    building.setOwner(tribe.getAid());
    building.setType(BuildingType.TOWN_HALL.getName());
    cell.setContent(building);
    return cell;
  }

  public Cell getRandomPosition() {
    boolean success = false;
    Cell cell = null;
    while (!success) {
      int randomX = random.nextInt(board.length);
      int randomY = random.nextInt(board[0].length);
      cell = board[randomX][randomY];
      if (nonNull(cell)) {
        success = true;
      }
    }
    return cell;
  }

  public Cell getCellByPosition(int x, int y) {
    if (x < 0 || y < 0) {
      throw new IllegalArgumentException("Wrong dimensions. Cannot be negative");
    }
    if (x >= board.length || y >= board[0].length) {
      throw new IllegalArgumentException("Wrong dimensions. To big");
    }
    return Optional.ofNullable(board[x][y])
        .orElseThrow(() -> new RuntimeException("No cell found"));
  }

  public boolean canUnitMove(Unit unit, int targetDirection){
    // TODO: 19.05.19 IMPLEMENT THIS
//    System.out.println("//!! TODO:    Implement logic for GameBoard::canUnitMove");
    return true;
  }

  @Deprecated
  public boolean canUnitMove(Unit unitByAid, Cell target) {
    int xDistance = Math.abs(unitByAid.getX() - target.getX());
    int yDistance = Math.abs(unitByAid.getY() - target.getY());

    if (xDistance == 2 && yDistance == 0) {
      // movement from up to down, for example (3,3) to (1,3), (5,3) - 2 options
      return true;
    } else if (xDistance == 1 && yDistance == 1) {
      // crossed movement; from (3,3) to (2,2), (4,2), (2,4), (4,4) - 4 options
      return true;
    } else if (xDistance == 0 && yDistance == 0) {
      // units wants to stay at the same place
      return true;
    } else {
      // other are false
      return false;
    }
  }
}
