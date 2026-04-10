package com.example.contact;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/contact")
public class ContactServlet extends HttpServlet {
    private String FILE_PATH;
    private static final String FILE_HEADER = "id,name,email,phone,message";

    @Override
    public void init() throws ServletException {
        FILE_PATH = getServletContext().getRealPath("/WEB-INF/contacts.txt");
        File file = new File(FILE_PATH);

        try {
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                boolean created = file.createNewFile();
                // Write header if new file
                if (created) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                        writer.write(FILE_HEADER);
                        writer.newLine();
                    }
                }
                System.out.println("Contacts file created: " + created);
            }
            if (!file.canWrite()) {
                throw new ServletException("No write permissions for contacts.txt");
            }
        } catch (IOException e) {
            throw new ServletException("Error creating contacts.txt", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String message = request.getParameter("message");

        // Validate all required fields
        if (name == null || email == null || message == null ||
                name.trim().isEmpty() || email.trim().isEmpty() || message.trim().isEmpty()) {
            response.sendRedirect("contact.jsp?error=Required+fields+missing");
            return;
        }

        // Sanitize inputs
        name = name.trim();
        email = email.trim();
        message = message.trim();
        phone = phone == null ? "" : phone.trim();

        try {
            // Read all existing contacts to get the next ID
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            int nextId = 1; // Default if file is empty (except header)

            if (lines.size() > 1) { // More than just the header line
                String lastLine = lines.get(lines.size() - 1);
                String[] lastParts = lastLine.split(",");
                if (lastParts.length > 0) {
                    try {
                        nextId = Integer.parseInt(lastParts[0]) + 1;
                    } catch (NumberFormatException e) {
                        // If ID parsing fails, continue with auto-increment
                        nextId = lines.size(); // Fallback to line count
                    }
                }
            }

            // Append new contact to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                String contactEntry = String.join(",",
                        String.valueOf(nextId),
                        name,
                        email,
                        phone,
                        message.replace(",", ";") // Replace commas in message to avoid CSV issues
                );
                writer.write(contactEntry);
                writer.newLine();
            }

            response.sendRedirect("contact.jsp?success=Thank+you+for+contacting+us");
        } catch (IOException e) {
            e.printStackTrace();
            response.sendRedirect("contact.jsp?error=Failed+to+submit+your+message");
        }
    }
}
