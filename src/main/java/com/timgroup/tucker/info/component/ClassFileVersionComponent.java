package com.timgroup.tucker.info.component;

import java.io.DataInputStream;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports a version extracted from the leading bytes of the specified anchor class.
 * <p>
 * This represents the version of the class's bytecode, and indicates which JVMs it is likely to be compatible with.
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1">JavaSE reference</a>
 */
public class ClassFileVersionComponent  extends Component {

    private final Class<?> anchorClass;

    public ClassFileVersionComponent(Class<?> anchorClass) {
        super("classfileversion", "ClassFile Version");
        this.anchorClass = anchorClass;
    }

    @Override
    public Report getReport() {
        String name = anchorClass.getName().replace('.', '/') + ".class";
        try (DataInputStream data = new DataInputStream(anchorClass.getClassLoader().getResourceAsStream(name))) {
            if (0xCAFEBABE != data.readInt()) {
                throw new IllegalStateException("invalid header");
            }
            int minor = data.readUnsignedShort();
            int major = data.readUnsignedShort();
            return new Report(Status.INFO, major + "." + minor);
        }
        catch (Exception e) {
            return new Report(Status.WARNING, "Unable to determine class version " + e);
        }
    }

}
