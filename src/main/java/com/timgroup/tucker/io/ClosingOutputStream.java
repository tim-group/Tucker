package com.timgroup.tucker.io;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClosingOutputStream extends FilterOutputStream {
    
    private final Closeable closeable;
    
    public ClosingOutputStream(OutputStream out, Closeable closeable) {
        super(out);
        this.closeable = closeable;
    }
    
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            closeable.close();
        }
    }
    
}
