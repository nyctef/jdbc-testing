package hello;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.*;

public class MariaJdbcTest {

    @Test
    public void acceptsBasicJdbcUrl() throws Exception {
        assertTrue(new Driver().acceptsURL("jdbc:mariadb://localhost/db?user=foo"));
    }

    @Test
    public void rejectsSqlServerUrl() throws Exception {
        assertFalse(new Driver().acceptsURL("jdbc:sqlserver://localhost/db?user=foo"));
    }

    // fortunately mariadb appears to give us a nice interface for inspecting the
    // parse results directly

    @Test
    public void emptyValuesTreatedAsTrue() throws Exception {
        assertFalse(Configuration.parse("jdbc:mariadb://localhost/db").useServerPrepStmts());
        assertTrue(Configuration.parse("jdbc:mariadb://localhost/db?useServerPrepStmts=").useServerPrepStmts());
        assertTrue(Configuration.parse("jdbc:mariadb://localhost/db?useServerPrepStmts").useServerPrepStmts());
    }
}
