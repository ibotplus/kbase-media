package com.eastrobot.converter.model;

public enum FFmpegFileType {
    PCM("pcm", ".pcm");

    String extension;
    String extensionWithPoint;

    FFmpegFileType(String extension, String extensionWithPoint) {
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
