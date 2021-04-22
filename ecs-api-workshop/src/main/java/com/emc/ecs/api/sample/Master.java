package com.emc.ecs.api.sample;

import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master {
    private static final Logger logger = LoggerFactory.getLogger(Master.class);
    private String name;
    private URI masterId;
    private String resource;
    private String address;
    private Map<String, String> headers;

    public Master() {
        this.masterId = URI.create("invalidId");
        this.resource = "//master";
        this.address = Resource.getPath() + resource;
        this.headers = Resource.getHeadersWithAuth();
        this.name = null;
    }

    public String create(String name) throws Exception {
        this.name = name;
        String parameter = "{\"master_type\": \"GEMALTO\",";
        parameter += "\"name\": \"" + name + "\"";
        parameter += "}";
        String id = null;
        String response = Resource.getResponse(address, headers, parameter,  "POST");
        int start = response.indexOf("\"id\": \"");
        if ( start != -1 ) {
            start += 7;
            int end = response.indexOf("\"", start);
            if ( end != -1  && end > start) {
                id = response.substring(start, end);
            } else {
                logger.info("master id not found in response.");
            }
        } else {
            logger.info("id key not found in response.");
        }
        if (id != null) {
            this.masterId = URI.create(id);
        }
        return id;
    }

    public String updateMaster(String vdcId, String primaryServerId, String secondaryServerId) throws Exception  {
        String parameter = "{";
        parameter += "\"name\": \""+ this.name + "\",";
        parameter += "\"_mapping_set\":[";
        parameter += "\"_mapping\":{";
        parameter += "\"_slaves_list\":[";
        parameter += "\"_slave\": \""+  primaryServerId + "\",";
        parameter += "\"_slave\": \""+  secondaryServerId + "\",";
        parameter += "],";
        parameter += "\"vdc_id\": \"" + vdcId + "\"";
        parameter += "}";
        parameter += "]}";
        String response = Resource.getResponse(address, headers, parameter,  "PUT");
        return  response;
    }


    public String getMaster() throws Exception  {
        String response = Resource.getResponse(address + "/" + masterId.toString(), headers, "", "GET");
        return response;
    }

    public String listMaster() throws Exception  {
        String response = Resource.getResponse(address, headers, "", "GET");
        return response;
    }

    public String deleteMaster() throws Exception  {
        String response = Resource.getResponse(address + "/" + masterId.toString(), headers, "", "DELETE");
        return response;
    }

    public String activateMaster(String vdcId, String primaryServerId, String secondaryServerId)  throws Exception  {
        String response = Resource.getResponse(address + "/" + masterId.toString() + "/activate", headers, "", "PUT");
        return response;
    }
}
