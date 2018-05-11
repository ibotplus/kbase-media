package com.eastrobot.converter.model;

/**
 * 文件类型
 *
 * @author Yogurt_lei
 * @date 2018-04-18 17:57
 */
public enum FileType {
    PCM("pcm", ".pcm"),
    JPG("jpg", ".jpg"),
    TXT("txt", ".txt"),
    AAC("aac", ".aac"),
    ZIP("zip", ".zip"),
    /**
     * parse result file extension
     */
    RS("rs", ".rs");

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
