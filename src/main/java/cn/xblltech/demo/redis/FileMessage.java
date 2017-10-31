package cn.xblltech.demo.redis;

import java.io.Serializable;

public class FileMessage implements Serializable{

    private String name;
    private String identifier;
    private Long totalChunks;
    private Long chunkNumber;
    private String chunkSize;

    public Long getChunkNumber() {
        return chunkNumber;
    }

    public Long getTotalChunks() {
        return totalChunks;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public void setChunkNumber(Long chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalChunks(Long totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "name='" + name + '\'' +
                ", identifier='" + identifier + '\'' +
                ", totalChunks=" + totalChunks +
                ", chunkNumber=" + chunkNumber +
                ", chunkSize='" + chunkSize + '\'' +
                '}';
    }
}
