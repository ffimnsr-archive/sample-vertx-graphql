package org.vastorigins.sesame.graph.models;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

public class Organization {
  public Numeric getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isPremium() {
    return isPremium;
  }

  public int getType() {
    return type;
  }

  public int getManageById() {
    return manageById;
  }

  public int getInsertedById() {
    return insertedById;
  }

  private final Numeric id;
  private final String name;
  private final String description;
  private final boolean isPremium;
  private final int type;
  private final int manageById;
  private final int insertedById;

  public Organization(final Numeric id, final String name, final String description) {
    this(id, name, description, false, 0, 0, 0);
  }

  public Organization(final Numeric id, final String name, final String description, final boolean isPremium,
      final int type, final int manageById, final int insertedById) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isPremium = isPremium;
    this.type = type;
    this.manageById = manageById;
    this.insertedById = insertedById;
  }

  public static Organization fromRow(final Row aRow) {
    return new Organization(aRow.get(Numeric.class, 0), aRow.getString("name"), aRow.getString("description"));
  }

  public static List<Organization> fromRowSet(final RowSet<Row> aRows) {
    return Lists.newArrayList(aRows)
      .stream()
      .map(Organization::fromRow)
      .collect(Collectors.toList());
  }
}
