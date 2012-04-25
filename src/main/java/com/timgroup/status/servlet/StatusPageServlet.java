package com.timgroup.status.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.timgroup.status.StatusPage;

@SuppressWarnings("serial")
public class StatusPageServlet extends HttpServlet {
    
    private StatusPage statusPage;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) {
            response.sendRedirect(request.getContextPath() + request.getServletPath() + "/");
        } else if (path.equals("/")) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/xml");
            statusPage.getApplicationReport().render(response.getWriter());
        } else if (path.equals("/" + StatusPage.DTD_FILENAME)) {
            sendResource(response, "application/xml-dtd", StatusPage.DTD_FILENAME);
        } else if (path.equals("/" + StatusPage.CSS_FILENAME)) {
            sendResource(response, "text/css", StatusPage.CSS_FILENAME);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "try asking for .../status/");
        }
    }
    
    private void sendResource(HttpServletResponse response, String contentType, String filename) throws IOException {
        InputStream resource = StatusPage.class.getResourceAsStream(filename);
        if (resource == null) throw new FileNotFoundException(filename);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(contentType);
        OutputStream output = response.getOutputStream();
        IOUtils.copy(resource, output);
    }
    
    public void setStatusPage(StatusPage statusPage) {
        this.statusPage = statusPage;
    }
    
}
