package com.example.contact;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/admin/admin_contacts")
public class ContactListServlet extends HttpServlet {
    private String getFilePath() {
        return getServletContext().getRealPath("/WEB-INF/contacts.txt");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Map<String, String>> contacts = new ArrayList<>();
        File file = new File(getFilePath());

        // Create file if it doesn't exist
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            // Write header if new file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("id,name,email,phone,message,submission_date");
                writer.newLine();
            }
        }

        // Read contacts from file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Skip header line
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle commas in message
                if (parts.length >= 5) {
                    Map<String, String> contact = new LinkedHashMap<>();
                    contact.put("id", parts[0]);
                    contact.put("name", parts[1]);
                    contact.put("email", parts[2]);
                    contact.put("phone", parts.length > 3 ? parts[3] : "");
                    contact.put("message", parts.length > 4 ? parts[4].replace("\"", "") : "");
                    contact.put("date", parts.length > 5 ? parts[5] : "");
                    contacts.add(contact);
                }
            }
        }

        request.setAttribute("contacts", contacts);
        request.getRequestDispatcher("/admin/contacts.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle contact deletion if needed
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String idToDelete = request.getParameter("id");
            if (idToDelete != null) {
                deleteContact(idToDelete);
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/contacts");
    }

    private void deleteContact(String idToDelete) throws IOException {
        File inputFile = new File(getFilePath());
        File tempFile = new File(getFilePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            // Copy header
            writer.write(reader.readLine());
            writer.newLine();

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] parts = currentLine.split(",", 2);
                if (!parts[0].equals(idToDelete)) {
                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        }

        // Replace original file with temp file
        if (!inputFile.delete()) {
            throw new IOException("Could not delete original file");
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temp file");
        }
    }
}
