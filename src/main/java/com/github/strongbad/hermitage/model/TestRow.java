package com.github.strongbad.hermitage.model;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import org.springframework.core.style.ToStringCreator;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.util.Objects;

@EverythingIsNonnullByDefault
public class TestRow {

  private final int id;
  private final int value;

  public static TestRow row(int id, int value) {
    return new TestRow(id, value);
  }

  private TestRow(int id, int value) {
    this.id = id;
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof TestRow) && typedEquals((TestRow) other);
  }

  private boolean typedEquals(TestRow other) {
    return Objects.equals(getId(), other.getId())
      && Objects.equals(getValue(), other.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, value);
  }

  @Override
  public String toString() {
    return new ToStringCreator(this)
      .append("id", getId())
      .append("value", getValue())
      .toString();
  }

  public static RowMapper<TestRow> mapper() {
    return new TestRowMapper();
  }

  private static final class TestRowMapper implements RowMapper<TestRow> {

    @Override
    public TestRow mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new TestRow(rs.getInt(1), rs.getInt(2));
    }

  }

}
