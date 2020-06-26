package com.kirilo.java.servlets.eshop;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/eshoporder")
public class EshopOrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<html>");
            out.println("<head><title>Query Response</title></head>");
            out.println("<body>");

            // Step 1: Allocate a database 'Connection' object
//            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());

//            https://stackoverflow.com/a/8106090/9586230
//            https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "admin_db", "password");
                 // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"

                 Statement stmt = conn.createStatement()) {
                // Step 3 & 4: Execute a SQL SELECT query and Process the query result
                // Retrieve the books' id. Can order more than one books.
                String[] ids = req.getParameterValues("id");
                if (ids != null) {
                    String sqlStr;
                    int count;

                    // Process each of the books
                    for (String id : ids) {
                        // Update the qty of the table books
                        sqlStr = "UPDATE books SET qty = qty - 1 WHERE id = " + id;
                        out.println("<p>" + sqlStr + "</p>");  // for debugging
                        count = stmt.executeUpdate(sqlStr);
                        out.println("<p>" + count + " record updated.</p>");

                        // Create a transaction record
/*                        sqlStr = "INSERT INTO order_records (id, qty_ordered) VALUES ("
                                + id + ", 1)";*/
                        sqlStr = "INSERT INTO order_records VALUES (" + id + ", 1, '"
                                + req.getParameter("cust_name") + "', '"
                                + req.getParameter("cust_email") + "', '"
                                + req.getParameter("cust_phone")+"')";
                        out.println("<p>" + sqlStr + "</p>");  // for debugging
                        count = stmt.executeUpdate(sqlStr);
                        out.println("<p>" + count + " record inserted.</p>");
                        out.println("<h3>Your order for book id=" + id
                                + " has been confirmed.</h3>");
                    }
                    out.println("<h3>Thank you.<h3>");
                } else { // No book selected
                    out.println("<h3>Please go back and select a book...</h3>");
                }


            } catch (SQLException throwables) {
                out.println("<p>Error: " + throwables.getMessage() + "</p>");
                out.println("<p>Check Tomcat console for details.</p>");
                throwables.printStackTrace();
            }

            out.println("</body></html>");
        } // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK 7)
        catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getMultipleValueParamQuery(HttpServletRequest req) {
        return "select * from books where author in ("
                + getAuthors(req)   // Multi-value-quote SQL string
                + ") and price < " + req.getParameter("price")
                + " and qty > 0 order by price desc, author asc, title asc";
    }

    private String getAuthors(HttpServletRequest req) {
        final String[] authors = req.getParameterValues("author");
        if (authors == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder("'");

        builder.append(authors[0]);
        final int length = authors.length;
        if (length > 1) {
            for (int i = 1; i < length; i++) {
                builder.append("', '");
                builder.append(authors[i]);
            }
        }
        builder.append("'");

        return builder.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
