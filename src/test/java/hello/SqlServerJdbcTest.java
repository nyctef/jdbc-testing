package hello;

import static org.junit.Assert.*;

import org.junit.Test;

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

}