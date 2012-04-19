package com.timgroup.status.servlet;

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
            response.setContentType("text/xml+status");
            statusPage.render(response.getWriter());
        } else if (path.equals("/" + StatusPage.DTD_FILENAME)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/xml-dtd");
            InputStream dtdStream = StatusPage.class.getResourceAsStream(StatusPage.DTD_FILENAME);
            OutputStream output = response.getOutputStream();
            IOUtils.copy(dtdStream, output);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "try asking for .../status/");
        }
    }
    
    public void setStatusPage(StatusPage statusPage) {
        this.statusPage = statusPage;
    }
    
}
