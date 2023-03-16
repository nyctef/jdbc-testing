package hello;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.sql.DriverPropertyInfo;
import java.util.stream.Stream;

import com.mysql.cj.jdbc.*;

public class MySqlJdbcTest {
  @Test
  public void acceptsBasicJdbcUrl() throws Exception {
    assertTrue(new Driver().acceptsURL("jdbc:mysql://blah"));
  }

  @Test
  public void rejectsBasicUnrecognisedUrl() throws Exception {
    assertFalse(new Driver().acceptsURL("jdbc:somethingelse:asdfasdf"));
  }
}