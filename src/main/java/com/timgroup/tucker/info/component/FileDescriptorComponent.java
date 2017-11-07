package com.timgroup.tucker.info.component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Optional;

import com.sun.management.UnixOperatingSystemMXBean;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.INFO;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static java.lang.String.format;

public final class FileDescriptorComponent extends Component {
    /*nullable*/ private final FileDescriptorProvider fileDescriptorProvider;

    /**
     * @deprecated Use {@link #create()}
     */
    @Deprecated
    public FileDescriptorComponent() {
        this(FileDescriptorProvider.getDefault().orElse(null));
    }
    
    public FileDescriptorComponent(/*nullable*/ FileDescriptorProvider fileDescriptorProvider) {
        super("FileDescriptorComponent", "Used file descriptors");
        this.fileDescriptorProvider = fileDescriptorProvider;
    }

    public static Component create() {
        return create("FileDescriptorComponent", "Used file descriptors");
    }

    public static Component create(String id, String label) {
        return FileDescriptorProvider.getDefault()
                .map(provider -> Component.supplyReport(id, label, () -> report(provider)))
                .orElseGet(() -> Component.supplyInfo(id, label, () -> "File descriptor usage not applicable on this platform"));
    }

    public interface FileDescriptorProvider {
        Long used();
        Long total();

        static Optional<FileDescriptorProvider> getDefault() {
            OperatingSystemMXBean systemMXBean = ManagementFactory.getOperatingSystemMXBean();
            if (systemMXBean instanceof UnixOperatingSystemMXBean) {
                return Optional.of(new UnixOperationSystemProviderImpl((UnixOperatingSystemMXBean) systemMXBean));
            }
            else {
                return Optional.empty();
            }
        }
    }

    private static final class UnixOperationSystemProviderImpl implements FileDescriptorProvider {
        private final UnixOperatingSystemMXBean unixOperatingSystemMXBean;

        public UnixOperationSystemProviderImpl(UnixOperatingSystemMXBean unixOperatingSystemMXBean) {
            this.unixOperatingSystemMXBean = unixOperatingSystemMXBean;
        }

        @Override
        public Long used() {
            return unixOperatingSystemMXBean.getOpenFileDescriptorCount();
        }

        @Override
        public Long total() {
            return unixOperatingSystemMXBean.getMaxFileDescriptorCount();
        }
    }

    @Override
    public Report getReport() {
        if (fileDescriptorProvider == null) {
            return new Report(INFO, "File descriptor usage not applicable on this platform");
        }

        return report(fileDescriptorProvider);
    }

    private static Report report(FileDescriptorProvider fileDescriptorProvider) {
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
