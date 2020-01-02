package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class SourceRepositoryComponent extends Component {

    private static final String SOURCE_REPO_NOT_FOUND = "Source repository information not found";

    private final String sourceRepository;

    public SourceRepositoryComponent(ClassLoader classLoader) {
        super("sourcerepository", "Source Repository");
        sourceRepository = getSourceRepository(classLoader);
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, sourceRepository);
    }

    private String getSourceRepository(ClassLoader classLoader) {
        URL manifestUri = classLoader.getResource("META-INF/MANIFEST.MF");
        if (manifestUri == null) {
            return SOURCE_REPO_NOT_FOUND;
        }
        try (InputStream manifestStream = manifestUri.openStream()) {
            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            String gitOrigin = attributes.getValue("Git-Origin");
            if (gitOrigin == null) {
                return SOURCE_REPO_NOT_FOUND;
            }
            return gitOrigin;
        } catch (IOException e) {
            return "Unable to read manifest: " + e.getMessage();
        }
    }
}
