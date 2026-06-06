package com.pfp.desktop.foundation.content;

import java.io.IOException;
import java.util.List;

public interface ExtraContentSource {
    List<ContentNode> load(ContentSection section) throws IOException;
}
