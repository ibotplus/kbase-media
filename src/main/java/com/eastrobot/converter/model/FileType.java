package com.eastrobot.converter.model;

public enum FileType {
    PCM("pcm", ".pcm"),
    JPG("jpg", ".jpg");

    String extension;
    String extensionWithPoint;

    FileType(String extension, String extensionWithPoint) {
        this.extension = extension;
        this.extensionWithPoint = extensionWithPoint;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtensionWithPoint() {
        return extensionWithPoint;
    }

    public void setExtensionWithPoint(String extensionWithPoint) {
        this.extensionWithPoint = extensionWithPoint;
    }
}
