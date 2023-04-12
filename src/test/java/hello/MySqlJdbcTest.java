package hello;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mysql.cj.jdbc.Driver;

public class MySqlJdbcTest {
  @Test
  public void acceptsBasicJdbcUrl() throws Exception {
    assertTrue(new Driver().acceptsURL("jdbc:mysql://blah"));
  }

  @Test
  public void rejectsBasicUnrecognisedUrl() throws Exception {
    assertFalse(new Driver().acceptsURL("jdbc:somethingelse:asdfasdf"));
  }

  // TODO: is there a nicer API we can use that doesn't involve actually trying to
  // connect to a database?

  @Test
  public void cannotConnectToBlah() throws Exception {
    Exception ex = assertThrows(
        Exception.class,
        () -> new Driver().connect("jdbc:mysql://blah", null));
    assertEquals(
        "Communications link failure\n\nThe last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.",
        ex.getMessage());
  }

  @Test
  public void invalidSslModeValue() throws Exception {
    Exception ex = assertThrows(
        Exception.class,
        () -> new Driver().connect("jdbc:mysql://blah?sslMode=asdf", null));
    assertEquals(
        "The connection property 'sslMode' acceptable values are: 'DISABLED', 'PREFERRED', 'REQUIRED', 'VERIFY_CA' or 'VERIFY_IDENTITY'. The value 'asdf' is not acceptable.",
        ex.getMessage());
  }

  @Test
  @ParameterizedTest
  @ValueSource(strings = { "=asdf", "=", "", "=1", "=0"})
  public void boolValuesThrowErrorIfInvalid(String paranoidValue) throws Exception {
    Exception ex = assertThrows(
        Exception.class,
        () -> new Driver().connect("jdbc:mysql://blah?paranoid" + paranoidValue, null));
    assertTrue(
        ex.getMessage().contains("The connection property 'paranoid' acceptable values are: 'TRUE', 'FALSE', 'YES' or 'NO'. The value '"));
  }

  @Test
  @ParameterizedTest
  @ValueSource(strings = { "=true", "=TRUE", "=false", "=yes", "=no"})
  public void boolValuesTriesToConnectIfValid(String paranoidValue) throws Exception {
    Exception ex = assertThrows(
        Exception.class,
        () -> new Driver().connect("jdbc:mysql://blah?paranoid" + paranoidValue, null));
    assertTrue(
        ex.getMessage().contains("Communications link failure"));
  }
}