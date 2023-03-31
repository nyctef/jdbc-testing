package hello;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
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

  @Test
  public void CannotConnectToBlah() throws Exception {
    Exception ex = assertThrows(
        Exception.class,
        () -> new Driver().connect("jdbc:mysql://blah", null));
    assertEquals(
        "Communications link failure\n\nThe last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.",
        ex.getMessage());
  }
}