package com.se_same.services.graph.models;

import com.google.common.collect.Lists;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Organization {
  public UUID getId() {
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

  private final UUID id;
  private final String name;
  private final String description;
  private final boolean isPremium;
  private final int type;
  private final int manageById;
  private final int insertedById;

  public Organization(UUID id, String name, String description) {
    this(id, name, description, false, 0, 0, 0);
  }

  public Organization(UUID id, String name, String description, boolean isPremium, int type, int manageById, int insertedById) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isPremium = isPremium;
    this.type = type;
    this.manageById = manageById;
    this.insertedById = insertedById;
  }

  public static Organization fromRow(Row aRow) {
    return new Organization(
      aRow.getUUID("id"),
      aRow.getString("name"),
      aRow.getString("description")
     );
  }

  public static List<Organization> fromRowSet(RowSet<Row> aRows) {
    return Lists.newArrayList(aRows)
      .stream()
      .map(Organization::fromRow)
      .collect(Collectors.toList());
  }
}
