package com.mycompany.graphicaldisplay;

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
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author Alok
 */
public class RestClient {

    private WebTarget target;
    private String baseUrl, data;
    private List<String> paths;
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
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080/BasicRESTService/";
        }
        return UriBuilder.fromUri(baseUrl).build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
