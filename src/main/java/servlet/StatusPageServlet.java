package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.status.StatusPage;

@SuppressWarnings("serial")
public class StatusPageServlet extends HttpServlet {
    
    private StatusPage statusPage;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml+status");
        statusPage.render(response.getWriter());
    }
    
    public void setStatusPage(StatusPage statusPage) {
        this.statusPage = statusPage;
    }
    
}
