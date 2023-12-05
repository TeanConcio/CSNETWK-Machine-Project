package server;

import java.io.*;

public class UserClass {
    
    public String userHandle = null;
    public DataInputStream disReader;
    public DataOutputStream dosWriter;
    public DataOutputStream dosWriterMessage;

    public UserClass(DataInputStream disReader, DataOutputStream dosWriter, DataOutputStream dosWriterMessage) {
        this.disReader = disReader;
        this.dosWriter = dosWriter;
        this.dosWriterMessage = dosWriterMessage;
    }
}
