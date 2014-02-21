package com.timgroup.tucker.info.component;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;


public class FileDescriptorComponent extends Component {
    private FileDescriptorProvider fileDescriptorProvider;

    public FileDescriptorComponent() {
        this(new FileDescriptorProvider());
    }
    
    public FileDescriptorComponent(FileDescriptorProvider fileDescriptorProvider) {
        super("FileDescriptorComponent", "Used file descriptors");
        this.fileDescriptorProvider = fileDescriptorProvider;
    }

    public static class FileDescriptorProvider {
        private OperatingSystemMXBean mxBean;
        private Method getOpenFileDescriptorCount;
        private Method getMaxFileDescriptorCount;

        public FileDescriptorProvider() {
            mxBean = ManagementFactory.getOperatingSystemMXBean();
            try {
                getOpenFileDescriptorCount = mxBean.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
                getMaxFileDescriptorCount = mxBean.getClass().getDeclaredMethod("getMaxFileDescriptorCount");
                getOpenFileDescriptorCount.setAccessible(true);
                getMaxFileDescriptorCount.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public Long used() {
            try {
                return (Long) getOpenFileDescriptorCount.invoke(mxBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public Long total() {
            try {
                return (Long) getMaxFileDescriptorCount.invoke(mxBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }       
        }
    }

    @Override
    public Report getReport() {
        Long used = fileDescriptorProvider.used();
        Long total = fileDescriptorProvider.total();
        double ratio = used.doubleValue() / total.doubleValue();
        
        String message = format("%s/%s used", used, total);
        
        if (ratio > 0.9) {
            return new Report(CRITICAL, message);
        } else if (ratio > 0.5) {
            return new Report(WARNING, message);
        } else {
            return new Report(OK, message);
        }
    }
}
