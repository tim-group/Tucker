package servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.status.StatusPage;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusPageServletTest {
    
    @Test
    public void askingForStatusGetsXMLFromStatusPage() throws Exception {
        StatusPage statusPage = mock(StatusPage.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        
        when(request.getProtocol()).thenReturn("http");
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn("/status");
        when(response.getWriter()).thenReturn(writer);
        
        StatusPageServlet statusPageServlet = new StatusPageServlet();
        statusPageServlet.setStatusPage(statusPage);
        statusPageServlet.service(request, response);
        
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/xml+status"); // we won't need a charset here, because the container will add it
        verify(statusPage).render(writer);
    }
    
}
