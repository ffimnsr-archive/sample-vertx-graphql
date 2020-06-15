package org.vastorigins.sesame.graph.models;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

public class UserClue {
  public Numeric getId() {
    return id;
  }

  public Numeric getGlobalId() {
    return globalId;
  }

  public String getUsername() {
    return username;
  }

  public String getAvatar() {
    return avatar;
  }

  private final Numeric id;
  private final Numeric globalId;
  private final String username;
  private final String avatar;

  public UserClue(final Numeric id, final Numeric globalId, final String username, final String avatar) {
    this.id = id;
    this.globalId = globalId;
    this.username = username;
    this.avatar = avatar;
  }

  public static UserClue fromRow(final Row aRow) {
    return new UserClue(aRow.get(Numeric.class, 0), aRow.get(Numeric.class, 1), aRow.getString("name"),
        aRow.getString("avatar"));
  }

  public static List<UserClue> fromRowSet(final RowSet<Row> aRows) {
    return Lists.newArrayList(aRows).stream().map(UserClue::fromRow).collect(Collectors.toList());
  }
}
