package object;

import java.io.Serializable;

/*
 * blobs in directory "objects"
 * every blob bind with a file content
 * By the way, the SHA-1 hash value, rendered as a 40-character hexadecimal string
 * it makes a convenient file name for storing your data in your .gitlet.
 * It also gives you a convenient way to compare two files (blobs)
 * to see if they have the same contents:
 * if their SHA-1s are the same, we simply assume the files are the same.
 * @author: LMS
 * */
public class Blob implements Serializable {
    private final String blobId;

    private final String content;

    public Blob(String blobId, String content) {
        this.blobId = blobId;
        this.content = content;
    }

    public String getBlobId() {
        return blobId;
    }

    public String getContent() {
        return content;
    }
}
