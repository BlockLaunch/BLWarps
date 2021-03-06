package com.blocklaunch.blwarps.managers.storage;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.WarpBase;
import com.google.common.collect.Lists;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestManager<T extends WarpBase> implements StorageManager<T> {

    private Class<T> type;
    private BLWarps plugin;
    WebTarget webTarget;

    public RestManager(Class<T> type, BLWarps plugin) {
        this.type = type;
        this.plugin = plugin;

        Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
        HttpAuthenticationFeature auth =
                HttpAuthenticationFeature.basicBuilder().nonPreemptive()
                        .credentials(plugin.getConfig().getRestConfig().getRESTUsername(), plugin.getConfig().getRestConfig().getRESTPassword())
                        .build();

        client.register(auth);
        this.webTarget = client.target(plugin.getConfig().getRestConfig().getRESTURI());
        this.webTarget = this.webTarget.path(type.getName().toLowerCase());
    }

    /**
     * Send a GET request to the REST API Attempt to map the received entity to
     * a List<Warp>
     */
    @Override
    public List<T> load() {
        Response response = this.webTarget.request(MediaType.APPLICATION_JSON_TYPE).get();
        if (response.getStatus() != 200) {
            this.plugin.getLogger().error("There was an error loading from the {} storage. Error code: {}", this.plugin.getConfig().getStorageType(),
                    response.getStatus());
            return Lists.newArrayList();
        }
        return response.readEntity(new GenericType<List<T>>() {
        });
    }

    /**
     * Send a POST request to the REST API with a new Warp to save
     */
    @Override
    public void saveNew(T t) {
        Response response = this.webTarget.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(t, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() != 201) {
            this.plugin.getLogger().error("There was an error saving the warps to the {} storage. Error code: {}",
                    this.plugin.getConfig().getStorageType(),
                    response.getStatus());
        }
    }

    /**
     * Send a DELETE request to the REST API with the name of the warp to delete
     * in the path
     */
    @Override
    public void delete(T t) {
        Response response = this.webTarget.path(t.getName()).request(MediaType.APPLICATION_JSON_TYPE).delete();

        if (response.getStatus() != 200) {
            this.plugin.getLogger().error("There was an error saving the warps to the {} storage. Error code: {}",
                    this.plugin.getConfig().getStorageType(),
                    response.getStatus());
        }
    }

    /**
     * Send a PUT request to the REST API with the warp to replace the existing
     * one with the same name
     */
    @Override
    public void update(T t) {
        Response response = this.webTarget.request(MediaType.APPLICATION_JSON_TYPE).put(Entity.entity(t, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() != 200) {
            this.plugin.getLogger().error("There was an error saving the warps to the {} storage. Error code: {}",
                    this.plugin.getConfig().getStorageType(),
                    response.getStatus());
        }

    }

}
