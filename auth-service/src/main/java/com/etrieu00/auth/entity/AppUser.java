package com.etrieu00.auth.entity;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Table("app_user")
public class AppUser {
  @Id
  private Long id;
  private UUID uuid;
  private String userEmail;
  private String userPassword;
  private String userRoles;
  private String firstName;
  private String lastName;
  @CreatedBy
  private UUID ufc;
  @LastModifiedBy
  private UUID ulm;
  @CreatedDate
  private LocalDateTime dtc;
  @LastModifiedDate
  private LocalDateTime dtm;

  public AppUser() {
  }

  public static AppUser builder(UnaryOperator<AppUser> builder) {
    return builder.apply(new AppUser());
  }

  public Long getId() {
    return id;
  }

  public AppUser setId(Long id) {
    this.id = id;
    return this;
  }

  public UUID getUuid() {
    return uuid;
  }

  public AppUser setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public AppUser setUserEmail(String userEmail) {
    this.userEmail = userEmail;
    return this;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public AppUser setUserPassword(String userPassword) {
    this.userPassword = userPassword;
    return this;
  }

  public String getUserRoles() {
    return userRoles;
  }

  public AppUser setUserRoles(String userRoles) {
    this.userRoles = userRoles;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public AppUser setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public AppUser setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public UUID getUfc() {
    return ufc;
  }

  public AppUser setUfc(UUID ufc) {
    this.ufc = ufc;
    return this;
  }

  public UUID getUlm() {
    return ulm;
  }

  public AppUser setUlm(UUID ulm) {
    this.ulm = ulm;
    return this;
  }

  public LocalDateTime getDtc() {
    return dtc;
  }

  public AppUser setDtc(LocalDateTime dtc) {
    this.dtc = dtc;
    return this;
  }

  public LocalDateTime getDtm() {
    return dtm;
  }

  public AppUser setDtm(LocalDateTime dtm) {
    this.dtm = dtm;
    return this;
  }
}
