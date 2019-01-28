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

    String parameter = String.format(parameterFormat, username, password, masterId.toString(), certificateAuthority, certificateRevocationList, identityStore, identityStorePassword);

    private Map<String, String> headers;
    private static String parameterFormat = String.join("{",
            "  \"username\": \"%s\",",
            "  \"password\": \"%s\"," ,
            "  \"port\": 5696,\n",
            "  \"master_id\": \"%s\",\n",
            "  \"fqdn_ip\": \"%s\",\n",
            "  \"slave_hostname\": \"%s\",\n",
            "  \"certificate_authority\": \"%s\"",
            "  \"certificate_revocation_list\": \"%s\"", // -----BEGIN CERTIFICATE-----\\r\\nMIIDcTCCAlmgAwIBAgIJAJpaK/LOEFhaMA0GCSqGSIb3DQEBCwUAME8xCzAJBgNV\\r\\nBAYTAlVTMQswCQYDVQQIDAJXQTEQMA4GA1UEBwwHUmVkbW9uZDEhMB8GA1UECgwY\\r\\nSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMB4XDTE4MTEyNzIwMDI1MFoXDTE4MTIy\\r\\nODIwMDI1MFowTzELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAldBMRAwDgYDVQQHDAdS\\r\\nZWRtb25kMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwggEiMA0G\\r\\nCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCoM4akzg6b/ixUlCVkhmClKGgePiPK\\r\\nOJRT1snwbnDMAQWVLoeKgg37SV5KrVRCaz+lXrUJYLYmRs4KoiEXxHCIS/uXZXt7\\r\\n3QvBbRrLLg9ibcBtpDrWLORe+8z0a9oE6EbpHqNZMsc121KjJaMApJiwcWYyaFW7\\r\\nW03ISMdyI1cHOn9Ab3DGFvXdb22pMPo9Bn1pYw76UwxEo1p4BHtFXo1c8ltyQ/xi\\r\\n+oILPJDy99B/YtMv4LnZfCbsch8PKw0O82bfGsImcWVMc6i/B1xY0Icilq9Q/xPS\\r\\nWxogOHGg/8JcvV8YZwSgzZormBgX+yWtcvkBweHf+FulwckZJ8xzvzXbAgMBAAGj\\r\\nUDBOMB0GA1UdDgQWBBQJIyg2jpQ1CVEcUJniznxCgnN6mzAfBgNVHSMEGDAWgBQJ\\r\\nIyg2jpQ1CVEcUJniznxCgnN6mzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUA\\r\\nA4IBAQAYkAONQY2sLHkHXdpJZ4lF8/JQlon+VWIcHNsVprRJ61GmPr5z791jgISs\\r\\nzerb1W6rp7NXg7O3REhictMJxDe6euEevknR7BcfaiahgiQRI8r2QiOs0V6msj/6\\r\\nyVDXKXlk+VNcxIqVHrO8j+CicnyGw7l5NT2+CU7bvAGg++VtWIS1l6a1EUZJY1mC\\r\\na/A6CMJOdwZ5rAZmDTr66awjAqKufpo+NUvIKk6mEYDsgFSEmNpFcDDksqdadtOO\\r\\ns1g01WflF2qcO6oVhB/wMvnfzgfgyPASa3INHD6AdNQbEkblomaNxJo2ZfhVL24N\\r\\nrR6OW/1Nmto3621EhWcf9/Ub2iNp\\r\\n-----END CERTIFICATE-----\\r\\n\",\n" +
            "  \"identity_store\": \"%s\"", //LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlEY1RDQ0FsbWdBd0lCQWdJSkFKcGFLL0xPRUZoYU1BMEdDU3FHU0liM0RRRUJDd1VBTUU4eEN6QUpCZ05WDQpCQVlUQWxWVE1Rc3dDUVlEVlFRSURBSlhRVEVRTUE0R0ExVUVCd3dIVW1Wa2JXOXVaREVoTUI4R0ExVUVDZ3dZDQpTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01CNFhEVEU0TVRFeU56SXdNREkxTUZvWERURTRNVEl5DQpPREl3TURJMU1Gb3dUekVMTUFrR0ExVUVCaE1DVlZNeEN6QUpCZ05WQkFnTUFsZEJNUkF3RGdZRFZRUUhEQWRTDQpaV1J0YjI1a01TRXdId1lEVlFRS0RCaEpiblJsY201bGRDQlhhV1JuYVhSeklGQjBlU0JNZEdRd2dnRWlNQTBHDQpDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ29NNGFremc2Yi9peFVsQ1ZraG1DbEtHZ2VQaVBLDQpPSlJUMXNud2JuRE1BUVdWTG9lS2dnMzdTVjVLclZSQ2F6K2xYclVKWUxZbVJzNEtvaUVYeEhDSVMvdVhaWHQ3DQozUXZCYlJyTExnOWliY0J0cERyV0xPUmUrOHowYTlvRTZFYnBIcU5aTXNjMTIxS2pKYU1BcEppd2NXWXlhRlc3DQpXMDNJU01keUkxY0hPbjlBYjNER0Z2WGRiMjJwTVBvOUJuMXBZdzc2VXd4RW8xcDRCSHRGWG8xYzhsdHlRL3hpDQorb0lMUEpEeTk5Qi9ZdE12NExuWmZDYnNjaDhQS3cwTzgyYmZHc0ltY1dWTWM2aS9CMXhZMEljaWxxOVEveFBTDQpXeG9nT0hHZy84SmN2VjhZWndTZ3pab3JtQmdYK3lXdGN2a0J3ZUhmK0Z1bHdja1pKOHh6dnpYYkFnTUJBQUdqDQpVREJPTUIwR0ExVWREZ1FXQkJRSkl5ZzJqcFExQ1ZFY1VKbml6bnhDZ25ONm16QWZCZ05WSFNNRUdEQVdnQlFKDQpJeWcyanBRMUNWRWNVSm5pem54Q2duTjZtekFNQmdOVkhSTUVCVEFEQVFIL01BMEdDU3FHU0liM0RRRUJDd1VBDQpBNElCQVFBWWtBT05RWTJzTEhrSFhkcEpaNGxGOC9KUWxvbitWV0ljSE5zVnByUko2MUdtUHI1ejc5MWpnSVNzDQp6ZXJiMVc2cnA3TlhnN08zUkVoaWN0TUp4RGU2ZXVFZXZrblI3QmNmYWlhaGdpUVJJOHIyUWlPczBWNm1zai82DQp5VkRYS1hsaytWTmN4SXFWSHJPOGorQ2ljbnlHdzdsNU5UMitDVTdidkFHZysrVnRXSVMxbDZhMUVVWkpZMW1DDQphL0E2Q01KT2R3WjVyQVptRFRyNjZhd2pBcUt1ZnBvK05VdklLazZtRVlEc2dGU0VtTnBGY0REa3NxZGFkdE9PDQpzMWcwMVdmbEYycWNPNm9WaEIvd012bmZ6Z2ZneVBBU2EzSU5IRDZBZE5RYkVrYmxvbWFOeEpvMlpmaFZMMjRODQpyUjZPVy8xTm10bzM2MjFFaFdjZjkvVWIyaU5wDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo=\",\n" +
            "  \"identity_store_password\": \"%s\"",
            "}");

    public Slave() {
        this.masterId = null;
        this.name = null;
        this.slaveId = URI.create("invalidId");
        this.resource = "//slave";
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
