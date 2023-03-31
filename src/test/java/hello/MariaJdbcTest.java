package hello;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.*;
import org.mariadb.jdbc.export.HaMode;

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
        assertFalse(Configuration.parse("jdbc:mariadb://localhost/db?useServerPrepStmts=false").useServerPrepStmts());

        assertThrows(SQLException.class, () -> Configuration
                .parse("jdbc:mariadb://localhost/db?useServerPrepStmts=not_a_bool").useServerPrepStmts());
    }

    @Test
    public void acceptsUrlsWithMysqlSubprotocolIffExtraPropertySpecified() throws Exception {
        assertNotNull(Configuration.parse("jdbc:mysql://localhost/db?permitMysqlScheme"));
        assertNotNull(Configuration.parse("jdbc:mysql://localhost/db?permitMysqlScheme=true"));
        // not 100% sure why we get a null rather than an exception in this case - seems
        // like something to do with the jdbc framework using the first registered driver
        // that returns a non-null connection?
        assertNull(Configuration.parse("jdbc:mysql://localhost/db?"));
    }

    @Test
    public void acceptsMultipleProperties() throws Exception {
        Configuration config = Configuration
                .parse("jdbc:mariadb://127.0.0.1?allowLocalInfile=true&useServerPrepStmts&timezone=london");
        assertEquals(true, config.allowLocalInfile());
        assertEquals(true, config.useServerPrepStmts());
        assertEquals("london", config.timezone());
    }

    @Test
    public void acceptsFailoverOrLoadBalancingMode() throws Exception {
        assertEquals(HaMode.NONE, Configuration.parse("jdbc:mariadb://localhost").haMode());
        assertEquals(HaMode.REPLICATION, Configuration.parse("jdbc:mariadb:replication//localhost").haMode());
        assertEquals(HaMode.LOADBALANCE, Configuration.parse("jdbc:mariadb:loadbalance//localhost").haMode());
        assertEquals(HaMode.SEQUENTIAL, Configuration.parse("jdbc:mariadb:sequential//localhost").haMode());
    }

    @Test
    public void acceptsIpv6AddressInHostDescription() throws Exception {
        Configuration config = Configuration.parse("jdbc:mariadb://[2001:0660:7401:0200:0000:0000:0edf:bdd7]:3306");
        assertEquals("2001:0660:7401:0200:0000:0000:0edf:bdd7", config.addresses().get(0).host);
    }

    @Test
    public void acceptsAdressEqualsFormsInHostDescription() throws Exception {
        Configuration config = Configuration.parse("jdbc:mariadb://address=(host=hostname)(port=1234)(type=slave)");
        HostAddress addr = config.addresses().get(0);
        assertEquals("hostname", addr.host);
        assertEquals(1234, addr.port);
        assertFalse(addr.primary);
    }

    @Test
    public void acceptsMultipleHostDescriptions() throws Exception {
        Configuration config = Configuration
                .parse("jdbc:mariadb://host1,host2:1234,address=(host=host3)(type=replica)");

        assertEquals("host1", config.addresses().get(0).host);
        assertEquals(3306, config.addresses().get(0).port);
        assertTrue(config.addresses().get(0).primary);

        assertEquals("host2", config.addresses().get(1).host);
        assertEquals(1234, config.addresses().get(1).port);
        assertTrue(config.addresses().get(1).primary);

        assertEquals("host3", config.addresses().get(2).host);
        assertEquals(3306, config.addresses().get(2).port);
        assertFalse(config.addresses().get(2).primary);
    }
}
