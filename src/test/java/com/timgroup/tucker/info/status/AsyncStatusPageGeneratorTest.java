package com.timgroup.tucker.info.status;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.ClassFileVersionComponent;
import com.timgroup.tucker.info.component.JvmVersionComponent;
import com.timgroup.tucker.info.component.PermGenComponent;
import com.timgroup.tucker.info.component.VersionComponent;

public class AsyncStatusPageGeneratorTest {

    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "1.0.0");
        }
    };

    
    @Test
    public void meh() {
        List<Component> components = Arrays.asList(
             new ClassFileVersionComponent(this.getClass()),
             new JvmVersionComponent(),
             new PermGenComponent());
        
        AsyncStatusPageGenerator asyncGenerator = new AsyncStatusPageGenerator(
          "my-application-id",
          version,
          components);
        
        StatusPage status = asyncGenerator.getApplicationReport();
    }

}
