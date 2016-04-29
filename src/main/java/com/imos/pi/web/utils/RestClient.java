package com.imos.pi.web.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author Alok
 */
public class RestClient {

    private WebTarget target;
    @Setter
    @Getter
    private String baseUrl, data;
    @Setter
    @Getter
    private List<String> paths;
    @Setter
    @Getter
    private HttpMethod httpMethod;

    public RestClient() {
        paths = new ArrayList<>();
    }

    public RestClient setUrlPath() {
        paths.stream().forEach((path) -> {
            target = target.path(path);
        });
        return this;
    }

    public String execute() {
        switch (httpMethod) {
            case GET:
                break;
            case POST:
                Response response = target.request(MediaType.APPLICATION_JSON).
                        accept(MediaType.APPLICATION_JSON).
                        post(Entity.entity(data, MediaType.APPLICATION_JSON));

                return response.readEntity(String.class);
            case PUT:
                break;
            case DELETE:
                break;
        }
        return "";
    }

    public String executePOST(String data) {
        Response response = target.request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                post(Entity.entity(data, MediaType.APPLICATION_JSON));

        return response.readEntity(String.class);
    }

    public RestClient configure() {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        target = client.target(getBaseURI());

        return this;
    }

    private URI getBaseURI() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080/BasicRESTService/";
        }
        return UriBuilder.fromUri(baseUrl).build();
    }
}
