package com.aicas.edp.api_version;

import java.util.Objects;

public class ApiVersionService {
    private <T extends ApiParent> String getApiVersion(T apiParent) {
        return apiParent.getClass().getPackage().getAnnotation(EdpApiVersion.class).version();
    }

    public <T extends ApiParent> void checkVersion(T apiParent) throws UnsupportedVersionException {
        String supportedVersion = getApiVersion(apiParent);
        String actualVersion = apiParent.getApiVersion();
        if (Objects.isNull(actualVersion)) {
            throw new UnsupportedVersionException(supportedVersion, apiParent.getClass());
        }
        String[] actualVersionNumbers = actualVersion.split("\\.");
        String[] supportedVersionNumbers = supportedVersion.split("\\.");
        for (int i = 0; i < 1; i ++ ) {
            if (Integer.parseInt(actualVersionNumbers[i]) > Integer.parseInt(supportedVersionNumbers[i])) {
                throw new UnsupportedVersionException(actualVersion, supportedVersion, apiParent.getClass());
            }
        }
    }
}
