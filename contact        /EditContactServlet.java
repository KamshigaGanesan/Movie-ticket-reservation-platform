package com.example.contact;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/admin/admin_edit_contact")
public class EditContactServlet extends HttpServlet {
    private String getFilePath() {
        return getServletContext().getRealPath("/WEB-INF/contacts.txt");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            response.sendRedirect("contacts.jsp?error=Invalid+contact+ID");
            return;
        }

        List<String> lines = Files.readAllLines(Paths.get(getFilePath()), StandardCharsets.UTF_8);
        // Skip header line
        if (lines.size() > 0) lines = lines.subList(1, lines.size());

        for (String line : lines) {
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 5 && parts[0].equals(id)) {
                request.setAttribute("contact", Map.of(
                        "id", parts[0],
                        "name", parts[1],
                        "email", parts[2],
                        "phone", parts.length > 3 ? parts[3] : "",
                        "message", parts.length > 4 ? parts[4].replace("\"", "") : "",
                        "date", parts.length > 5 ? parts[5] : ""
                ));
                request.getRequestDispatcher("/admin/edit-contact.jsp").forward(request, response);
                return;
            }
        }
        response.sendRedirect("contacts.jsp?error=Contact+not+found");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String message = request.getParameter("message");

        if (id == null || name == null || email == null || message == null ||
                id.trim().isEmpty() || name.trim().isEmpty() || email.trim().isEmpty() || message.trim().isEmpty()) {
            response.sendRedirect("contacts.jsp?error=Invalid+input");
            return;
        }

        List<String> lines = Files.readAllLines(Paths.get(getFilePath()), StandardCharsets.UTF_8);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        // Keep header line
        if (!lines.isEmpty()) {
            updatedLines.add(lines.get(0));
            lines = lines.subList(1, lines.size());
        }

        for (String line : lines) {
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 5 && parts[0].equals(id)) {
                // Update the contact entry
                String updatedLine = String.join(",",
                        id,
                        name,
                        email,
                        phone != null ? phone : "",
                        "\"" + message.replace("\"", "'") + "\"",  // Wrap message in quotes
                        parts.length > 5 ? parts[5] : ""  // Keep original date
                );
                updatedLines.add(updatedLine);
                found = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            Files.write(Paths.get(getFilePath()), updatedLines, StandardCharsets.UTF_8);
            response.sendRedirect("contacts.jsp?success=Contact+updated");
        } else {
            response.sendRedirect("contacts.jsp?error=Contact+not+found");
        }
    }
}
