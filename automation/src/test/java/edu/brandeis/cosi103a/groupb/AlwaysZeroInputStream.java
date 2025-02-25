package edu.brandeis.cosi103a.groupb;

import java.io.InputStream;
import java.io.IOException;

public class AlwaysZeroInputStream extends InputStream {
    private final byte[] data;
    private int index = 0;

    public AlwaysZeroInputStream() {
        // "0\n" repeatedly.
        data = "0\n".getBytes();
    }

    @Override
    public int read() throws IOException {
        int val = data[index];
        index = (index + 1) % data.length;
        return val;
    }
}