package server;

public class FileClass
{
    public String filename;
    public byte[] data;
    public int length;
    public String sender;


    public FileClass(String filename, byte[] data, int length, String sender)
    {
        this.filename = filename;
        this.data = data;
        this.length = length;
        this.sender = sender;
    }
}