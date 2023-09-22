package hello;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.*;
import org.mariadb.jdbc.internal.util.constant.HaMode;

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
        assertFalse(UrlParser.parse("jdbc:mariadb://localhost/db").getOptions().useServerPrepStmts);
        assertTrue(UrlParser.parse("jdbc:mariadb://localhost/db?useServerPrepStmts=").getOptions().useServerPrepStmts);
        assertTrue(UrlParser.parse("jdbc:mariadb://localhost/db?useServerPrepStmts").getOptions().useServerPrepStmts);
        assertFalse(UrlParser.parse("jdbc:mariadb://localhost/db?useServerPrepStmts=false").getOptions().useServerPrepStmts);

        assertThrows(SQLException.class, () -> UrlParser
                .parse("jdbc:mariadb://localhost/db?useServerPrepStmts=not_a_bool"));
    }

    @Test
    public void acceptsUrlsWithMysqlSubprotocolIffExtraPropertySpecified() throws Exception {
        // This is the behavior as of mariadb driver v3:
        //
        // assertNotNull(UrlParser.parse("jdbc:mysql://localhost/db?permitMysqlScheme"));
        // assertNotNull(UrlParser.parse("jdbc:mysql://localhost/db?permitMysqlScheme=true"));
        // // not 100% sure why we get a null rather than an exception in this case - seems
        // // like something to do with the jdbc framework using the first registered
        // // driver
        // // that returns a non-null connection?
        // assertNull(UrlParser.parse("jdbc:mysql://localhost/db?"));
        //
        // This is the v2 behavior: mysql subprotocol is accepted by default
        assertNotNull(UrlParser.parse("jdbc:mysql://localhost/db?"));
    }

    @Test
    public void acceptsMultipleProperties() throws Exception {
        UrlParser config = UrlParser
                .parse("jdbc:mariadb://127.0.0.1?allowLocalInfile=true&useServerPrepStmts");
        assertEquals(true, config.getOptions().allowLocalInfile);
        assertEquals(true, config.getOptions().useServerPrepStmts);
    }

    @Test
    public void acceptsFailoverOrLoadBalancingMode() throws Exception {
        assertEquals(HaMode.NONE, UrlParser.parse("jdbc:mariadb://localhost").getHaMode());
        assertEquals(HaMode.REPLICATION, UrlParser.parse("jdbc:mariadb:replication//localhost").getHaMode());
        assertEquals(HaMode.LOADBALANCE, UrlParser.parse("jdbc:mariadb:loadbalance//localhost").getHaMode());
        assertEquals(HaMode.SEQUENTIAL, UrlParser.parse("jdbc:mariadb:sequential//localhost").getHaMode());
    }

    @Test
    public void acceptsIpv6AddressInHostDescription() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://[2001:0660:7401:0200:0000:0000:0edf:bdd7]:3306");
        assertEquals("2001:0660:7401:0200:0000:0000:0edf:bdd7", config.getHostAddresses().get(0).host);
    }

    @Test
    public void acceptsAdressEqualsFormsInHostDescription() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://address=(host=hostname)(port=1234)(type=slave)");
        HostAddress addr = config.getHostAddresses().get(0);
        assertEquals("hostname", addr.host);
        assertEquals(1234, addr.port);
        assertEquals("replica", addr.type);
    }

    @Test
    public void addressEqualsFormCanBeOrderedArbitrarily() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://address=(type=replica)(port=1234)(host=hostname)");
        HostAddress addr = config.getHostAddresses().get(0);
        assertEquals("hostname", addr.host);
        assertEquals(1234, addr.port);
        assertEquals("replica", addr.type);
    }

    @Test
    public void addressEqualsHostIsNotRequired() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://address=");
        HostAddress addr = config.getHostAddresses().get(0);
        assertNull(addr.host);
    }

    @Test
    public void acceptsMultipleHostDescriptions() throws Exception {
        UrlParser config = UrlParser
                .parse("jdbc:mariadb://host1,host2:1234,address=(host=host3)(type=replica)");

        assertEquals("host1", config.getHostAddresses().get(0).host);
        assertEquals(3306, config.getHostAddresses().get(0).port);
        assertEquals("master", config.getHostAddresses().get(0).type);

        assertEquals("host2", config.getHostAddresses().get(1).host);
        assertEquals(1234, config.getHostAddresses().get(1).port);
        assertEquals("master", config.getHostAddresses().get(1).type);

        assertEquals("host3", config.getHostAddresses().get(2).host);
        assertEquals("replica", config.getHostAddresses().get(2).type);
    }

    @Test
    public void doesNotSupportUrlEscaping() throws Exception {
        // as far as we can tell the mariadb driver doesn't support any kind of escaping
        UrlParser config = UrlParser
                .parse("jdbc:mariadb://localhost/db?password=password%20with%20ampersand%20%26");
        assertEquals("password%20with%20ampersand%20%26", config.getPassword());
    }

    @Test
    public void acceptsEmptyDatabase() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://localhost/?sslmode=verify-full");
        // empty is treated as not specified in this case
        assertNull(config.getDatabase());
    }

    @Test
    public void acceptsMissingDatabase() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://localhost?");
        // the properties are also optional
        assertNull(config.getDatabase());
    }

    @Test
    public void acceptsMissingDatabaseAndProperties() throws Exception {
        UrlParser config = UrlParser.parse("jdbc:mariadb://localhost/?");
        // the properties are also optional
        assertNull(config.getDatabase());
    }

    @Test
    public void rejectsIncorrectValuesForRecognisedProperties() throws Exception {
        assertThrows(SQLException.class, () -> UrlParser.parse("jdbc:mariadb://localhost/?connectTimeout=fasdfasdf"));
    }

    @Test
    public void ignoresUnrecognisedProperties() throws Exception {
        assertDoesNotThrow(() -> UrlParser.parse("jdbc:mariadb://localhost/?foo=fasdfasdf"));
    }
}
