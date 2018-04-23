package com.timgroup.tucker.info.servlet;

import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPage;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static com.timgroup.tucker.info.Health.ALWAYS_HEALTHY;
import static com.timgroup.tucker.info.Status.INFO;
import static com.timgroup.tucker.info.Stoppable.ALWAYS_STOPPABLE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationInformationServletTest {

    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(INFO, "0.0.1");
        }
    };

    @Test
    public void askingForStatusGetsXMLFromStatusPage() throws Exception {
        StatusPageGenerator statusPage = mock(StatusPageGenerator.class);
        StatusPage applicationReport = mock(StatusPage.class);
        when(statusPage.getApplicationReport()).thenReturn(applicationReport);
        doAnswer(new WriteOneCharacter('a')).when(applicationReport).render(any(Writer.class), any(Health.class));
        HttpServletRequest request = mockRequest("/status");
        HttpServletResponse response = mock(HttpServletResponse.class);
        GoldfishServletOutputStream out = new GoldfishServletOutputStream();
        when(response.getOutputStream()).thenReturn(out);

        ApplicationInformationServlet statusPageServlet = new ApplicationInformationServlet(statusPage, ALWAYS_STOPPABLE, ALWAYS_HEALTHY);
        statusPageServlet.service(request, response);

        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/xml"); // must be simply text/xml so Firefox applies CSS; the container will add a charset
        assertEquals('a', out.lastByte);
    }

    @Test
    public void askingForStatusJsonGetsJson() throws Exception {
        StatusPageGenerator statusPage = mock(StatusPageGenerator.class);
        StatusPage applicationReport = mock(StatusPage.class);
        when(statusPage.getApplicationReport()).thenReturn(applicationReport);
        doAnswer(new WriteOneCharacter('a')).when(applicationReport).renderJson(any(Writer.class), eq(Health.State.healthy));
        HttpServletRequest request = mockRequest("/status.json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        GoldfishServletOutputStream out = new GoldfishServletOutputStream();
        when(response.getOutputStream()).thenReturn(out);

        ApplicationInformationServlet statusPageServlet = new ApplicationInformationServlet(statusPage, ALWAYS_STOPPABLE, ALWAYS_HEALTHY);
        statusPageServlet.service(request, response);

        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/json");
        assertEquals('a', out.lastByte);
    }

    @Test
    public void askingForStatusWithCallbackGetsJSONPFromStatusPage() throws Exception {
        String callbackFunction = "test" + System.currentTimeMillis();
        StatusPageGenerator statusPage = mock(StatusPageGenerator.class);
        StatusPage applicationReport = mock(StatusPage.class);
        when(statusPage.getApplicationReport()).thenReturn(applicationReport);
        doAnswer(new WriteOneCharacter('a')).when(applicationReport).renderJson(any(Writer.class), eq(Health.State.healthy));
        HttpServletRequest request = mockRequest("/status", Collections.singletonMap("callback", callbackFunction));
        HttpServletResponse response = mock(HttpServletResponse.class);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletOutputStream out = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                output.write(b);
            }
        };
        when(response.getOutputStream()).thenReturn(out);

        ApplicationInformationServlet statusPageServlet = new ApplicationInformationServlet(statusPage, ALWAYS_STOPPABLE, ALWAYS_HEALTHY);
        statusPageServlet.service(request, response);

        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/javascript");
        assertEquals(callbackFunction + "(a)", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    private final class WriteOneCharacter implements Answer<Void> {
        private final char ch;

        private WriteOneCharacter(char ch) {
            this.ch = ch;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Writer writer = (Writer) invocation.getArguments()[0];
            writer.write(ch);
            writer.close();
            return null;
        }
    }

    private final class GoldfishServletOutputStream extends ServletOutputStream {
        public int lastByte;

        @Override
        public void write(int b) throws IOException {
            lastByte = b;
        }
    }

    @Test
    public void askingForRootResourceRedirectsToStatusResource() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        new ApplicationInformationServlet(new StatusPageGenerator("", version), ALWAYS_STOPPABLE, ALWAYS_HEALTHY).service(request, response);

        verify(response).sendRedirect("/Foo/info/status");
    }

    @Test
    public void askingForDTDGetsDTD() throws Exception {
        HttpServletRequest request = mockRequest("/status-page.dtd");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(newServletOutputStream(buffer));

        new ApplicationInformationServlet(new StatusPageGenerator("", version), ALWAYS_STOPPABLE, ALWAYS_HEALTHY).service(request, response);

        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/xml-dtd");
        assertArrayEquals(readResource("status-page.dtd"), buffer.toByteArray());
    }

    @Test
    public void askingForCSSGetsCSS() throws Exception {
        HttpServletRequest request = mockRequest("/status-page.css");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(newServletOutputStream(buffer));

        new ApplicationInformationServlet(new StatusPageGenerator("", version), ALWAYS_STOPPABLE, ALWAYS_HEALTHY).service(request, response);

        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/css");
        assertArrayEquals(readResource("status-page.css"), buffer.toByteArray());
    }

    private byte[] readResource(String filename) throws IOException {
        InputStream input = StatusPageGenerator.class.getResourceAsStream(filename);
        byte[] bytes = readFully(input);
        return bytes;
    }

    @Test
    public void askingForAnythingElseGets404() throws Exception {
        HttpServletRequest request = mockRequest("/rubbish");
        HttpServletResponse response = mock(HttpServletResponse.class);

        new ApplicationInformationServlet(new StatusPageGenerator("", version), ALWAYS_STOPPABLE, ALWAYS_HEALTHY).service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }

    private ServletOutputStream newServletOutputStream(final OutputStream out) {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
    }

    private byte[] readFully(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    private HttpServletRequest mockRequest(String pathInfo) {
        return mockRequest(pathInfo, Collections.<String, String> emptyMap());
    }

    private HttpServletRequest mockRequest(String pathInfo, final Map<String, String> parameters) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("http");
        when(request.getMethod()).thenReturn("GET");
        when(request.getContextPath()).thenReturn("/Foo");
        when(request.getServletPath()).thenReturn("/info");
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String name = (String) invocation.getArguments()[0];
                return parameters.get(name);
            }
        });
        return request;
    }

}
