package com.kirilo.java.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/querymp")
public class QueryMultiParamServlet extends HttpServlet {
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

                // Step 3: Execute a SQL SELECT query
                String sqlStr = "select * from books where author = "
                        + "'" + req.getParameter("author") + "'"   // Single-quote SQL string
                        + " and price < " + req.getParameter("price")
                        + " and qty > 0 order by price desc";

                out.println("<h3>Thank you for your query.</h3>");
                out.println("<p>Your SQL statement is: " + sqlStr + "</p>"); // Echo for debugging
                ResultSet rset = stmt.executeQuery(sqlStr);  // Send the query to the server

                // Step 4: Process the query result set
                int count = 0;
                while (rset.next()) {
                    // Print a paragraph <p>...</p> for each record
                    out.println("<p>" + rset.getString("author")
                            + ", " + rset.getString("title")
                            + ", $" + rset.getDouble("price") + "</p>");
                    count++;
                }
                out.println("<p>==== " + count + " records found =====</p>");
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
