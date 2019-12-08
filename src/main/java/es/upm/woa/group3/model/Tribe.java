package es.upm.woa.group3.model;

import es.upm.woa.ontology.Cell;
import jade.core.AID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static es.upm.woa.group3.util.GameSettings.INITIAL_FOOD_NUMBER;
import static es.upm.woa.group3.util.GameSettings.INITIAL_GOLD_NUMBER;
import static es.upm.woa.group3.util.GameSettings.INITIAL_STONE_NUMBER;
import static es.upm.woa.group3.util.GameSettings.INITIAL_WOOD_NUMBER;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Tribe {

  private AID aid;
  private String name;
  private List<Unit> units = new ArrayList<>();
  private List<Cell> discoveredCells = new ArrayList<>();
  // todo: check this test default values
  private int gold = INITIAL_GOLD_NUMBER;
  private int stone = INITIAL_STONE_NUMBER;
  private int food = INITIAL_FOOD_NUMBER;
  private int wood = INITIAL_WOOD_NUMBER;

  public Tribe(AID aid, String name) {
    if (isBlank(name)) {
      throw new IllegalArgumentException("Tribe name cannot be empty");
    }
    this.name = name.trim().toLowerCase();
    this.aid = Objects.requireNonNull(aid);
  }

  public boolean containsUnit(Unit unit) {
    return units.contains(unit);
  }

  public boolean containsUnit(AID unitAid) {
    return units.stream().anyMatch(unit -> unit.getAid().equals(unitAid));
  }

  public List<Unit> getUnits() {
    return new ArrayList<>(units);
  }

  public Optional<Unit> getUnitByAid(AID aid) {
    return units.stream().filter(unit -> unit.getAid().equals(aid)).findFirst();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getGold() {
    return gold;
  }

  public void setGold(int gold) {
    this.gold = gold;
  }

  public int getStone() {
    return stone;
  }

  public void setStone(int stone) {
    this.stone = stone;
  }

  public int getFood() {
    return food;
  }

  public void setFood(int food) {
    this.food = food;
  }

  public int getWood() {
    return wood;
  }

  public void setWood(int wood) {
    this.wood = wood;
  }

  public void addUnit(Unit unit) {
    this.units.add(Objects.requireNonNull(unit));
  }

  public AID getAid() {
    return aid;
  }

  public void setAid(AID aid) {
    this.aid = aid;
  }

  public List<Cell> getDiscoveredCells() {
    return discoveredCells;
  }

  public void addDiscoveredCell(Cell cell) {
    discoveredCells.add(cell);
  }

  public boolean isCellDiscovered(Cell cell){
    return discoveredCells.contains(cell);
  }
}
