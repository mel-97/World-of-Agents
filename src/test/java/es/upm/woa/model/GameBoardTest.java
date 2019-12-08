package es.upm.woa.model;

import es.upm.woa.group3.model.GameBoard;
import org.junit.Test;

public class GameBoardTest {
  final int defaultX = 10;
  final int defaultY = 10;
  GameBoard gameBoard;

  @Test
  public void should_create_default_game_board() {
    gameBoard = GameBoard.getInstance();
  }


}
