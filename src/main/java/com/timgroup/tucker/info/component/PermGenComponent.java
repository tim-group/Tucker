package com.timgroup.tucker.info.component;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public class PermGenComponent extends Component {
    private PermGenProvider permGenProvider;

    public PermGenComponent() {
        this(new PermGenProvider());
    }
    
    public PermGenComponent(PermGenProvider permGenProvider) {
        super("PermGenComponent", "PermGenComponent");
        this.permGenProvider = permGenProvider;
    }
    
    public static class PermGenProvider {
        private MemoryPoolMXBean permGenPool;

        public PermGenProvider() {
            permGenPool = permGenPool();   
        }
        
        private MemoryPoolMXBean permGenPool() {
            List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean pool : memoryPools) {
                if (pool.getName().contains("Perm")) {
                    return pool;
                }
            }    
            throw new RuntimeException("Failed to instantiate component. PermGen memory pool not found: " + poolNames(memoryPools));
        }
        
        private List<String> poolNames(List<MemoryPoolMXBean> memoryPools) {
            List<String> pools = new ArrayList<String>();
            for (MemoryPoolMXBean pool : memoryPools) {
                pools.add(pool.getName());
            }
            return pools;
        }
        
        public long total() {
            return permGenPool.getUsage().getMax() == -1 
                    ? permGenPool.getUsage().getCommitted() 
                            : permGenPool.getUsage().getMax();
        }

        public long used() {
            return permGenPool.getUsage().getUsed();
        }
    }

    @Override
    public Report getReport() {
        double usedFraction = (double)permGenProvider.used() / permGenProvider.total();
        
        String message = format("%s%% used", (int)(usedFraction * 100));
        if (usedFraction > 0.95) {
            return new Report(CRITICAL, message);
        } else if (usedFraction > 0.8) {
            return new Report(WARNING, message);
        } else {
            return new Report(OK, message);
        }
        
    }
}
