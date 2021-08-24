package com.github.fmjsjx.entrepot.server.conf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Properties class for HTTP route.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class HttpRouteProperties {

    /**
     * The path pattern of the HTTP service.
     */
    private String path;
    /**
     * The HTTP method.
     */
    private String method;
    /**
     * The name of the path variable for the hangar name.
     */
    private String pathVar;
    /**
     * The name of the query variable for the hangar data. Only effective on
     * {@code GET} method.
     */
    private String queryVar;
    /**
     * The type of the route.
     * <p>
     * The default value is {@code raw}.
     */
    private WharfType type;
    /**
     * The processor. Only effective when type is {@code cook}
     */
    private String processor;
    /**
     * The allowed content types. Only effective when content data is persistent.
     */
    private List<String> allowedContentTypes;
    /**
     * The field definitions. Only effective when processor is
     * {@code merge-json-fields}.
     */
    private Map<String, String> fields;

    @JsonCreator
    public HttpRouteProperties(@JsonProperty("path") String path, @JsonProperty("method") String method,
            @JsonProperty("path-var") String pathVar, @JsonProperty("query-var") String queryVar,
            @JsonProperty("type") String type, @JsonProperty("processor") String processor,
            @JsonProperty("allowed-content-types") List<String> allowdContentTypes,
            @JsonProperty("fields") @JsonDeserialize(as = LinkedHashMap.class) Map<String, String> fields) {
        this.path = path;
        this.method = method;
        this.pathVar = pathVar;
        this.queryVar = queryVar;
        this.type = WharfType.of(type);
        this.processor = processor;
        this.allowedContentTypes = allowdContentTypes == null ? List.of()
                : List.of(allowdContentTypes.stream().map(String::intern).toArray(String[]::new));
        this.fields = fields;
    }

    public void validate() {
        // TODO
    }

}
