package com.kirilo.java.servlets.eshop;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/eshopquery")
public class EshopQueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html");  // HTML 5
            out.println("<html>");
            out.println("<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Query Response</title>\n" +
                    "    <link rel=\"stylesheet\" href=\"css/hw.css\">\n" +
                    "</head>");
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
                // Step 3: Execute a SQL SELECT query
                String sqlStr = getAuthorsQuery(req);

                out.println("<h3>Thank you for your query.</h3>");
                out.println("<p>Your SQL statement is: " + sqlStr + "</p>"); // Echo for debugging
                ResultSet rset = stmt.executeQuery(sqlStr);  // Send the query to the server

                // Step 4: Process the query result set
                int count = 0;
                out.println("<form method='get' action='eshoporder'>");
                out.println("<table>\n" +
                        "  <tr>\n" +
                        "    <th></th>\n" +
                        "    <th>AUTHOR</th>\n" +
                        "    <th>TITLE</th>\n" +
                        "    <th>PRICE</th>\n" +
                        "  </tr>"
                );
                while (rset.next()) {
                    // Print a paragraph <p>...</p> for each record
                    out.println("<tr>" +
                            "<td><input type='checkbox' name='id' value='"
                            + rset.getInt("id") + "' /></td><td>"
                            + rset.getString("author")
                            + "</td><td>" + rset.getString("title")
                            + "</td><td> $" + rset.getDouble("price") + "</td></tr>");
                    count++;
                }
                out.println("</table>");

                out.println("<p>==== " + count + " records found =====</p>");

                out.println("<p>Enter your Name: <input type='text' name='cust_name' /></p>");
                out.println("<p>Enter your Email: <input type='email' name='cust_email' /></p>");
                out.println("<p>Enter your Phone Number: <input type='tel' name='cust_phone' /></p>");

                out.println("    <input type='submit' value='ORDER' />");
                out.println("</form>");


            } catch (SQLException throwables) {
                out.println("<p>Error: " + throwables.getMessage() + "</p>");
                out.println("<p>Check Tomcat console for details.</p>");
                throwables.printStackTrace();
            }

            out.println("</body>\n" +
                    "</html>");
        } // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK 7)
        catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getAuthorsQuery(HttpServletRequest req) {
        return "select * from books where author in ("
                + getAuthors(req)   // Multi-value-quote SQL string
                + ") and qty > 0 order by price desc, author asc, title asc";
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
