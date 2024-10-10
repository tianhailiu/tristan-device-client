package com.aicas.edp.api_version;

import lombok.Getter;

@Getter
public class UnsupportedVersionException extends Exception{
    private String actualVersion;
    private final String supportedVersion;
    private final Class<? extends ApiParent> targetClass;

    public UnsupportedVersionException(String actualVersion, String supportedVersion, Class<? extends ApiParent> targetClass) {
        this.actualVersion = actualVersion;
        this.supportedVersion = supportedVersion;
        this.targetClass = targetClass;
    }

    public UnsupportedVersionException(String supportedVersion, Class<? extends ApiParent> targetClass) {
        this.supportedVersion = supportedVersion;
        this.targetClass = targetClass;
    }
}
