package com.timgroup.tucker.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ClosingOutputStreamTest {
    
    OutputStream out = mock(OutputStream.class);
    Closeable c = Mockito.mock(Closeable.class);
    
    @Test
    public void closingStreamClosesUnderlyingStream() throws Exception {
        new ClosingOutputStream(out, c).close();
        
        verify(out).close();
    }
    
    @Test
    public void closingStreamClosesAuxiliaryCloseable() throws Exception {
        new ClosingOutputStream(out, c).close();
        
        verify(c).close();
    }
    
    @Test
    public void closingStreamClosesAuxiliaryCloseableEvenIfUnderlyingStreamThrowsAnException() throws Exception {
        Mockito.doThrow(new IOException()).when(out).close();
        
        try {
            new ClosingOutputStream(out, c).close();
        } catch (IOException e) {
        }
        
        verify(c).close();
    }
    
    @Test
    public void closingStreamClosesUnderlyingStreamEvenIfAuxiliaryCloseableThrowsAnException() throws Exception {
        Mockito.doThrow(new IOException()).when(c).close();
        
        try {
            new ClosingOutputStream(out, c).close();
        } catch (IOException e) {
        }
        
        verify(out).close();
    }
    
}
