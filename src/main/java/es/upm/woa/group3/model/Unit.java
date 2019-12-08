package es.upm.woa.group3.model;

import jade.core.AID;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

public class Unit {

  private AID aid;
  private String name;
  private Tribe tribe;
  private int x;
  private int y;

  public Unit(AID aid, String name, Tribe tribe, int x, int y) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("Name cannot be null");
    }
    this.name = name;
    this.aid = requireNonNull(aid);
    this.tribe = requireNonNull(tribe);
    this.x = x;
    this.y = y;
  }

  public String getName() {
    return name;
  }

  public AID getAid() {
    return aid;
  }

  public Tribe getTribe() {
    return tribe;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Unit unit = (Unit) o;
    return Objects.equals(name, unit.name);
  }

  @Override
  public int hashCode() {
    return hash(name);
  }
}
