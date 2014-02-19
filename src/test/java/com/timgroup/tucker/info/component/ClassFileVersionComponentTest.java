package com.timgroup.tucker.info.component;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.timgroup.tucker.info.Status;

public final class ClassFileVersionComponentTest {

    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("classfileversion", new ClassFileVersionComponent(ClassFileVersionComponentTest.class).getId());
        assertEquals("ClassFile Version", new ClassFileVersionComponent(ClassFileVersionComponentTest.class).getLabel());
    }

    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new ClassFileVersionComponent(ClassFileVersionComponentTest.class).getReport().getStatus());
    }

    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        final InputStream in = getClass().getClassLoader().getResourceAsStream("com/timgroup/tucker/info/component/ClassFileVersionComponentTest.class");
        final DataInputStream data = new DataInputStream(in);
        data.skipBytes(4);
        final String expected = String.format("%2$s.%1$s", data.readUnsignedShort(), data.readUnsignedShort());
        assertEquals(expected, new ClassFileVersionComponent(ClassFileVersionComponentTest.class).getReport().getValue());
    }
}
