package com.ernesto.uploaderserver;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "uploadServlet", value = "/upload_file")
public class UploadServlet extends HttpServlet {

    private boolean isMultipart;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + "Esperando a que se cargue el archivo..." + "</h1>");
        out.println("</body></html>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        isMultipart = ServletFileUpload.isMultipartContent(request);
        PrintWriter pw = response.getWriter();
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        if(!isMultipart) {
            pw.println("No obtuve ningún archivo");
        } else {
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            String path = getServletContext().getRealPath("/") + "/";
            fileItemFactory.setRepository(new File(path));
            // Process an upload
            ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
            try {
                List<FileItem> fileItems = upload.parseRequest(request);
                for(FileItem fi : fileItems) {
                    String fileName = "";
                    // Process normal form field
                    if(fi.isFormField()) {
                        if(fi.getFieldName().equals("filename")) {
                            if(!fi.getString().isEmpty()) fileName = fi.getString("UTF-8");
                        }
                    } // Process names of uploading files
                    else if (!fi.getName().isEmpty()) {
                        // Intercept the file name from the upload file path sent by the client
                        fileName = fi.getName().substring(fi.getName().lastIndexOf(File.separator) + 1);
                    }
                    // Start to save uploaded files
                    if(!fileName.isEmpty()) {
                        String newPath = System.getProperty("user.home") + File.separator + fileName;
                        File file = new File(newPath);
                        fi.write(file);
                        pw.println("El archivo se ha escrito correctamente en: " + fileName);
                        System.out.println("Saved to " + newPath);
                    }
                }
            } catch (Exception ex) {
                pw.println("Error al recibir el archivo cargado: " + ex.getMessage());
            }
        }
    }

}