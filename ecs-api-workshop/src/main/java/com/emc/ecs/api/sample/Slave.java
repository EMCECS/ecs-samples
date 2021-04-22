package com.emc.ecs.api.sample;

import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slave {
    private static final Logger logger = LoggerFactory.getLogger(Slave.class);
    private String name;
    private URI masterId;
    private URI slaveId;
    private String resource;
    private String address;
    private String username;
    private String password;
    private String certificateAuthority;
    private String certificateRevocationList;
    private String identityStore;
    private String identityStorePassword;

    private Map<String, String> headers;
    private static String parameterFormat = String.join("{",
            "  \"username\": \"%s\",",
            "  \"password\": \"%s\"," ,
            "  \"port\": 5696,\n",
            "  \"master_id\": \"%s\",\n",
            "  \"fqdn_ip\": \"%s\",\n",
            "  \"slave_hostname\": \"%s\",\n",
            "  \"certificate_authority\": \"%s\"",
            "  \"certificate_revocation_list\": \"%s\"", // -----BEGIN CERTIFICATE-----
            "  \"identity_store\": \"%s\"", //LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t
            "  \"identity_store_password\": \"%s\"",
            "}");

    public Slave() {
        this.masterId = URI.create("invalidId");
        this.name = "invalidName";
        this.slaveId = URI.create("invalidId");
        this.resource = "/slave";
        this.address = Resource.getPath() + resource;
        this.headers = Resource.getHeadersWithAuth();
    }

    public String create(URI masterId,
                         String name,
                         String username,
                         String password,
                         String certificateAuthority,
                         String certificateRevocationList,
                         String identityStore,
                         String identityStorePassword) throws Exception  {
        this.name = name;
        this.masterId = masterId;
        this.username = username;
        this.password = password;
        this.certificateAuthority = certificateAuthority;
        this.certificateRevocationList = certificateRevocationList;
        this.identityStore = identityStore;
        this.identityStorePassword = identityStorePassword;

        String parameter = String.format(parameterFormat, username, password, masterId.toString(), certificateAuthority, certificateRevocationList, identityStore, identityStorePassword);
        String id = null;
        String response = Resource.getResponse(address, headers, parameter,  "POST");
        int start = response.indexOf("slave_id\": \"");
        if ( start != -1 ) {
            start += 13;
            int end = response.indexOf("\"", start);
            if ( end != -1  && end > start) {
                id = response.substring(start, end);
            } else {
                logger.info("slave id not found in response.");
            }
        } else {
            logger.info("id key not found in response.");
        }
        if (id != null) {
            this.slaveId = URI.create(id);
        }
        return id;
    }

    public String updateSlave(String vdcId, String primarySlaveId, String secondarySlaveId) throws Exception  {
        String parameter = String.format(parameterFormat, username, password, masterId.toString(), certificateAuthority, certificateRevocationList, identityStore, identityStorePassword);
        String response = Resource.getResponse(address + "/" + slaveId.toString(), headers, parameter,  "PUT");
        return  response;
    }


    public String getSlave() throws Exception  {
        String response = Resource.getResponse(address + "/" + slaveId.toString(), headers, "", "GET");
        return response;
    }

    public String listSlave() throws Exception {
        String response = Resource.getResponse(address, headers, "", "GET");
        return response;
    }

    public String deleteSlave() throws Exception {
        String response = Resource.getResponse(address + "/" + slaveId.toString(), headers, "", "DELETE");
        return response;
    }
}
