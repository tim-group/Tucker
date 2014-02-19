package com.timgroup.tucker.info.component;

import java.io.DataInputStream;
import java.io.InputStream;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports a version extracted from the leading bytes of the specified anchor class. This represents
 * the version of the class's bytecode, and indicates which JVMs it is likely to be compatible with.
 *
 * <p>
 * See {@link http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1}
 */
public class ClassFileVersionComponent  extends Component {

    private final Class<?> anchorClass;

    public ClassFileVersionComponent(Class<?> anchorClass) {
        super("classfileversion", "ClassFile Version");
        this.anchorClass = anchorClass;
    }

    @Override
    public Report getReport() {
        try {
            final String name = anchorClass.getName().replace('.', '/') + ".class";
            final InputStream in = anchorClass.getClassLoader().getResourceAsStream(name);
            final DataInputStream data = new DataInputStream(in);
            if (0xCAFEBABE != data.readInt()) {
                throw new IllegalStateException("invalid header");
            }
            int minor = data.readUnsignedShort();
            int major = data.readUnsignedShort();
            in.close();
            return new Report(Status.INFO, major + "." + minor);
        }
        catch (Exception e) {
            return new Report(Status.WARNING, "Unable to determine class version " + e);
        }
    }

}
