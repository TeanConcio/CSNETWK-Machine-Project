package server;

import java.io.*;

public class UserClass {
    
    public String userHandle;
    public DataInputStream disReader;
    public DataOutputStream dosWriter;

    public UserClass(DataInputStream disReader, DataOutputStream dosWriter) {
        this.disReader = disReader;
        this.dosWriter = dosWriter;
    }
}
