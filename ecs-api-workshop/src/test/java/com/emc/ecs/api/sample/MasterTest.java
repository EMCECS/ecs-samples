package com.emc.ecs.api.sample;

import com.emc.ecs.api.sample.Master;
import com.emc.ecs.api.sample.Slave;
import com.emc.ecs.api.sample.Resource;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.*;

public class MasterTest {

    @Test
    public void MasterSlaveTest() {
        Master m = new Master();
        Slave s = new Slave();
        String masterId = null;
        String slaveId = null;
        try {
             masterId = m.create("master");
             slaveId = s.create(URI.create(masterId),
                         "slave",
                         "username",
                         "password",
                         "certificateAuthority",
                         "certificateRevocationList",
                         "identityStore",
                         "identityStorePassword");
             assertTrue(masterId != null);
             assertTrue(slaveId != null);
       } catch (Exception e) {
             e.printStackTrace();
       }
             assertTrue( m != null);
             assertTrue( s != null);
    }
}
