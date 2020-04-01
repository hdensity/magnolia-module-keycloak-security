package it.schm.magnolia.keycloak.security;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultMapper implements Mapper {

    private Map<String, String> mappings = new HashMap<>();
    private boolean mapUnmappedAsIs = true;

    @Override
    public String map(String value) {
        String mapped = mappings.get(value);

        if (StringUtils.isEmpty(mapped)) {
            return mapUnmappedAsIs ? value : null;
        } else {
            return mapped;
        }
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public void addMapping(String source, String target) {
        this.mappings.put(source, target);
    }

    public void setMapUnmappedAsIs(boolean mapUnmappedAsIs) {
        this.mapUnmappedAsIs = mapUnmappedAsIs;
    }

}
