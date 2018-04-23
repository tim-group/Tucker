package com.timgroup.tucker.info.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Runbook;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.VersionComponent;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatusPageGeneratorTest {
    
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setValidating(true);
    }
    
    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "0.0.1");
        }
    };
    
    @Test
    public void unconfiguredStatusPageRendersBasicXMLStructure() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version, Clock.fixed(Instant.parse("2016-05-25T00:47:33.651Z"), ZoneOffset.UTC));
        
        Document document = render(statusPage);
        
        Element root = document.getDocumentElement();
        assertEquals("myapp", root.getAttribute("id"));
        assertEquals(probeHostname(), root.getAttribute("host"));
        assertEquals("ok", root.getAttribute("class"));
        assertEquals(Status.OK, statusPage.getApplicationReport().getApplicationStatus());
        
        assertEquals(2, root.getElementsByTagName("component").getLength());
        
        Element timestamp = getSingleElementByTagName(root, "timestamp");
        
        assertEquals("2016-05-25T00:47:33Z", timestamp.getTextContent());
    }

    @Test
    public void unconfiguredStatusPageRendersBasicJSONStructure() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version, Clock.fixed(Instant.parse("2016-05-25T00:47:33.651Z"), ZoneOffset.UTC));

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("myapp", object.at("/id").asText());
        assertEquals(probeHostname(), object.at("/host").asText());
        assertEquals("ok", object.at("/status").asText());
        assertEquals("healthy", object.at("/health").asText());

        assertEquals(1, object.at("/components").size());
        assertEquals("2016-05-25T00:47:33Z", object.at("/timestamp").asText());
    }

    @Test
    public void healthExposedInJSON() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version, Clock.fixed(Instant.parse("2016-05-25T00:47:33.651Z"), ZoneOffset.UTC));

        ObjectNode object = renderJson(statusPage, () -> Health.State.ill);

        assertEquals("ill", object.at("/health").asText());
    }

    @Test
    public void healthExposedInStatusPage() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version, Clock.fixed(Instant.parse("2016-05-25T00:47:33.651Z"), ZoneOffset.UTC));

        Document document = render(statusPage, () -> Health.State.ill);

        Element root = document.getDocumentElement();
        Element component = getElementByIndex(root, "component", 0, 2);

        assertEquals("health", component.getAttribute("id"));
        assertEquals("info", component.getAttribute("class"));
        assertEquals("Health: ill", component.getTextContent());
        assertEquals("ill", getSingleElementByTagName(component, "value").getTextContent());
    }

    @Test
    public void canAddAnInformativeComponentStatus() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
            @Override
            public Report getReport() {
                return new Report(Status.INFO, 23);
            }
        });
        
        Document document = render(statusPage);
        
        Element root = document.getDocumentElement();
        assertEquals("ok", root.getAttribute("class"));
        assertEquals(Status.OK, statusPage.getApplicationReport().getApplicationStatus());
        
        Element component = getLastElementByTagName(root, "component");
        assertEquals("mycomponent", component.getAttribute("id"));
        assertEquals("info", component.getAttribute("class"));
        assertEquals("Number of coincidences today: 23", component.getTextContent());
        assertEquals("23", getSingleElementByTagName(component, "value").getTextContent());
    }

    @Test
    public void canAddAnInformativeComponentStatusToJSON() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
            @Override
            public Report getReport() {
                return new Report(Status.INFO, 23);
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("ok", object.at("/status").asText());

        assertEquals(2, object.at("/components").size());

        assertEquals("mycomponent", object.at("/components/1/id").asText());
        assertEquals("info", object.at("/components/1/status").asText());
        assertEquals("Number of coincidences today", object.at("/components/1/label").asText());
        assertEquals("23", object.at("/components/1/value").asText());
    }

    @Test
    public void canAddANormativeComponentStatus() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, 23);
            }
        });
        
        Document document = render(statusPage);
        
        Element root = document.getDocumentElement();
        assertEquals("critical", root.getAttribute("class"));
        assertEquals(Status.CRITICAL, statusPage.getApplicationReport().getApplicationStatus());
        
        Element component = getLastElementByTagName(root, "component");
        assertEquals("mycomponent", component.getAttribute("id"));
        assertEquals("critical", component.getAttribute("class"));
        assertEquals("Number of coincidences today: 23", component.getTextContent());
        assertEquals("23", getSingleElementByTagName(component, "value").getTextContent());
    }

    @Test
    public void canAddANormativeComponentStatusToJSON() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, 23);
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("critical", object.at("/status").asText());

        assertEquals(2, object.at("/components").size());

        assertEquals("mycomponent", object.at("/components/1/id").asText());
        assertEquals("critical", object.at("/components/1/status").asText());
        assertEquals("Number of coincidences today", object.at("/components/1/label").asText());
        assertEquals("23", object.at("/components/1/value").asText());
    }

    @Test
    public void canAddANormativeComponentStatusWithoutAValue() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Eschatological immanency") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL);
            }
        });
        
        Document document = render(statusPage);
        
        Element root = document.getDocumentElement();
        assertEquals("critical", root.getAttribute("class"));
        assertEquals(Status.CRITICAL, statusPage.getApplicationReport().getApplicationStatus());
        
        Element component = getLastElementByTagName(root, "component");
        assertEquals("mycomponent", component.getAttribute("id"));
        assertEquals("critical", component.getAttribute("class"));
        assertEquals("Eschatological immanency", component.getTextContent());
        assertEquals(0, component.getElementsByTagName("value").getLength());
    }

    @Test
    public void canAddANormativeComponentStatusWithoutAValueToJSON() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Eschatological immanency") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL);
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("critical", object.at("/status").asText());

        assertEquals(2, object.at("/components").size());

        assertEquals("mycomponent", object.at("/components/1/id").asText());
        assertEquals("critical", object.at("/components/1/status").asText());
        assertEquals("Eschatological immanency", object.at("/components/1/label").asText());
        assertTrue(object.at("/components/1/value").isMissingNode());
    }

    @Test
    public void failedReportLeadsToCriticalStatusAndExceptionOnPage() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                throw new Error("wrong wire");
            }
        });
        
        Document document = render(statusPage);
        
        Element root = document.getDocumentElement();
        assertEquals("critical", root.getAttribute("class"));
        assertEquals(Status.CRITICAL, statusPage.getApplicationReport().getApplicationStatus());
        
        Element component = getLastElementByTagName(root, "component");
        assertEquals("critical", component.getAttribute("class"));
        assertEquals("Red wire or green wire: wrong wire", component.getTextContent());
        assertEquals(0, component.getElementsByTagName("value").getLength());
        assertEquals("wrong wire", getSingleElementByTagName(component, "exception").getTextContent());
    }

    @Test
    public void failedReportLeadsToCriticalStatusAndExceptionInJSON() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                throw new Error("wrong wire");
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("critical", object.at("/status").asText());

        assertEquals(2, object.at("/components").size());

        assertEquals("mycomponent", object.at("/components/1/id").asText());
        assertEquals("critical", object.at("/components/1/status").asText());
        assertEquals("Red wire or green wire", object.at("/components/1/label").asText());
        assertEquals("wrong wire", object.at("/components/1/exception").asText());
    }

    @Test
    public void canAddAnOptionalRunbookToComponentAndLocationWillBePrintedInComponentStatusOnError() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                throw new Error("wrong wire");
            }
        }.withRunbook(new Runbook("http://the-solution-is-described-here.com")));

        Document document = render(statusPage);

        Element root = document.getDocumentElement();
        Element component = getLastElementByTagName(root, "component");
        String expectedRunbookText = "Runbook: http://the-solution-is-described-here.com";
        assertTrue("Expected component with text [" + component.getTextContent() + "] to contain [" + expectedRunbookText + "]",
                component.getTextContent().contains(expectedRunbookText));
    }

    @Test
    public void canAddAnOptionalRunbookToReportAndLocationWillBePrintedInComponentStatus() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, "brown wire?", new Runbook("http://the-solution-is-described-here.com"));
            }
        });

        Document document = render(statusPage);

        Element root = document.getDocumentElement();
        Element component = getLastElementByTagName(root, "component");
        String expectedRunbookText = "Runbook: http://the-solution-is-described-here.com";
        assertTrue("Expected component with text [" + component.getTextContent() + "] to contain [" + expectedRunbookText + "]",
                component.getTextContent().contains(expectedRunbookText));
    }

    @Test
    public void canAddAnOptionalRunbookToReportAndItWillOverrideUncaughtExceptionRunbook() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, "brown wire?", new Runbook("http://the-solution-is-described-here.com"));
            }
        }.withRunbook(new Runbook("http://uncaught-exception-runbook.com")));

        Document document = render(statusPage);

        Element root = document.getDocumentElement();
        Element component = getLastElementByTagName(root, "component");

        String reportSpecificRunbookLocation = "http://the-solution-is-described-here.com";
        String componentSpecificRunbookLocation = "http://uncaught-exception-runbook.com";

        assertTrue("Expected component with text [" + component.getTextContent() + "] to contain [" + reportSpecificRunbookLocation + "]",
                component.getTextContent().contains(reportSpecificRunbookLocation));
        assertFalse("Expected component with text [" + component.getTextContent() + "] not to contain [" + componentSpecificRunbookLocation + "]",
                component.getTextContent().contains(componentSpecificRunbookLocation));
    }

    @Test
    public void optionalRunbookFromComponentIsDisplayedIfReportDoesNotProvideOne() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Red wire or green wire") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, "brown wire?");
            }
        }.withRunbook(new Runbook("http://uncaught-exception-runbook.com")));

        Document document = render(statusPage);

        Element root = document.getDocumentElement();
        Element component = getLastElementByTagName(root, "component");

        String componentSpecificRunbookLocation = "Runbook: http://uncaught-exception-runbook.com";

        assertTrue("Expected component with text [" + component.getTextContent() + "] to contain [" + componentSpecificRunbookLocation + "]",
                component.getTextContent().contains(componentSpecificRunbookLocation));
    }

    @Test
    public void canAddOptionalRunbookToReportAndItWillBeIncludedInJson() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Eschatological immanency") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL).withRunbook(new Runbook("http://the-solution-is-described-here.com"));
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("http://the-solution-is-described-here.com", object.at("/components/1/runbook/locationUrl").asText());
    }

    @Test
    public void whenOptionalRunbookIsNotIncludedItIsNullInJson() throws Exception {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("mycomponent", "Eschatological immanency") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL);
            }
        });

        ObjectNode object = renderJson(statusPage, Health.ALWAYS_HEALTHY);

        assertEquals("null", object.at("/components/1/runbook/locationUrl").asText());
    }

    private Element getSingleElementByTagName(Element element, String name) {
        return getElementByIndex(element, name, 0, 1);
    }
    
    private Element getLastElementByTagName(Element element, String name) {
        return getElementByIndex(element, name, 2, 3);
    }

    private Element getElementByIndex(Element element, String name, int index, int expectedNumberOfElements) {
        NodeList elementsByTagName = element.getElementsByTagName(name);
        assertEquals(expectedNumberOfElements, elementsByTagName.getLength());
        return (Element) elementsByTagName.item(index);
    }

    private Document render(StatusPageGenerator statusPage) throws IOException, SAXException, ParserConfigurationException {
        return render(statusPage, Health.ALWAYS_HEALTHY);
    }
    private Document render(StatusPageGenerator statusPage, Health health) throws ParserConfigurationException, SAXException, IOException {
        StringWriter writer = new StringWriter();
        
        statusPage.getApplicationReport().render(writer, health);
        
        InputSource source = new InputSource(new StringReader(writer.toString()));
        Document document = parse(source);
        DocumentType doctype = document.getDoctype();
        String doctypeSystemId = doctype.getSystemId();
        assertEquals("status-page.dtd", doctypeSystemId);
        return document;
    }

    private Document parse(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        
        final List<SAXParseException> problems = new ArrayList<>();
        
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                String filename;
                try {
                    filename = new File(new URI(systemId).getPath()).getName();
                } catch (URISyntaxException e) {
                    throw new SAXException(e);
                }
                InputStream stream = StatusPageGenerator.class.getResourceAsStream(filename);
                return (stream != null) ? new InputSource(stream) : null;
            }
        });
        
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                problems.add(exception);
            }
            
            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                problems.add(exception);
            }
            
            @Override
            public void error(SAXParseException exception) throws SAXException {
                problems.add(exception);
            }
        });
        
        Document document = builder.parse(source);
        
        assertEquals(Collections.emptyList(), problems);
        
        return document;
    }

    private ObjectNode renderJson(StatusPageGenerator statusPage, Health health) throws JsonProcessingException, IOException {
        Writer writer = new StringWriter();
        statusPage.getApplicationReport().renderJson(writer, health.get());
        String string = writer.toString();
        return new ObjectMapper().readerFor(ObjectNode.class).readValue(string);
    }

    private static String probeHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
