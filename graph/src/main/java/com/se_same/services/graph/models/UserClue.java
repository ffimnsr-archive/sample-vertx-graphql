package com.se_same.services.graph.models;

import java.util.UUID;

public class UserClue {
  public UUID getId() {
    return id;
  }

  public UUID getGlobalId() {
    return globalId;
  }

  public String getUsername() {
    return username;
  }

  public String getAvatar() {
    return avatar;
  }

  private final UUID id;
  private final UUID globalId;
  private final String username;
  private final String avatar;

  public UserClue(UUID globalId, String username, String avatar) {
    this(UUID.randomUUID(), globalId, username, avatar);
  }

  public UserClue(UUID id, UUID globalId, String username, String avatar) {
    this.id = id;
    this.globalId = globalId;
    this.username = username;
    this.avatar = avatar;
  }
}
