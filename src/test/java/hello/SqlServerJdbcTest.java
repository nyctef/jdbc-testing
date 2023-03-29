package hello;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.sql.DriverPropertyInfo;
import java.util.stream.Stream;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class SqlServerJdbcTest {
  @Test
  public void acceptsBasicJdbcUrl() throws Exception {
    assertTrue(new SQLServerDriver().acceptsURL("jdbc:sqlserver://localhost;databaseName=database"));
  }

  @Test
  public void rejectsBasicUnrecognisedUrl() throws Exception {
    assertFalse(new SQLServerDriver().acceptsURL("jdbc:somethingelse:asdfasdf"));
  }

  @Test
  public void trustServerCertificateRequiresValue() throws Exception {
    DriverPropertyInfo[] props = new SQLServerDriver()
        .getPropertyInfo("jdbc:sqlserver://localhost;trustServerCertificate=", null);
    DriverPropertyInfo prop = Stream.of(props).filter(x -> x.name == "trustServerCertificate").findFirst().get();
    assertArrayEquals(new String[] { "true", "false" }, prop.choices);

    Exception ex = assertThrows(
        Exception.class,
        () -> new SQLServerDriver().connect("jdbc:sqlserver://localhost;trustServerCertificate=", null));
    assertTrue(ex.getMessage().contains("trustServerCertificate"));
  }
}