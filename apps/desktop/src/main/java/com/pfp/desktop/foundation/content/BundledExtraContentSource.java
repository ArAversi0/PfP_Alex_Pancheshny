package com.pfp.desktop.foundation.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class BundledExtraContentSource implements ExtraContentSource {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ContentNode> load(ContentSection section) throws IOException {
        String resource = "/com/pfp/desktop/content/" + section.id() + ".json";
        try (InputStream stream = BundledExtraContentSource.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Bundled content resource is missing: " + resource);
            }
            return objectMapper.readValue(stream, new TypeReference<>() {
            });
        }
    }
}
