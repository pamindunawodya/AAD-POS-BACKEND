package lk.ijse.gdse.aad.jndijsonservletproject.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.gdse.aad.jndijsonservletproject.dto.ItemDTO;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;

@WebServlet(urlPatterns ="/ItemHandle" )

public class ItemHandle extends HttpServlet {
    Connection connection;

    @Override
    public void init() throws ServletException {
        try {
            Class.forName(getServletContext().getInitParameter("mysql-driver"));
            String username = getServletContext().getInitParameter("db-user");
            String password = getServletContext().getInitParameter("db-pw");
            String url = getServletContext().getInitParameter("db-url");
            this.connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            DataSource pool = (DataSource) ctx.lookup("java:comp/env/jdbc/ItemHandle");
            this.connection = pool.getConnection();
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().toLowerCase().startsWith("application/json")) {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        try {
            Jsonb jsonb = JsonbBuilder.create();
            ItemDTO itemObj = jsonb.fromJson(req.getReader(), ItemDTO.class);
            //validation
            if (itemObj.getCode() == null || !itemObj.getCode().matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new RuntimeException("Invalid ID");
            } else if (itemObj.getDescription() == null || !itemObj.getDescription().matches("[A-Za-z '-]+")) {
                throw new RuntimeException("Invalid Name");
            } else if (itemObj.getQty() <= 0) {
                throw new RuntimeException("Invalid Quantity");
            } else if (itemObj.getPrice() <= 0) {
                throw new RemoteException("Invalid Price");
            }
            PreparedStatement ps =
                    connection.prepareStatement("INSERT INTO item(code,description,qty,price) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, itemObj.getCode());
            ps.setString(2, itemObj.getDescription());
            ps.setInt(3, itemObj.getQty());
            ps.setDouble(4, itemObj.getPrice());


            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("Save Failed");
            }
            ResultSet rst = ps.getGeneratedKeys();
            rst.next();

            rsp.setStatus(HttpServletResponse.SC_CREATED);
            //the created json is sent to frontend
            rsp.setContentType("application/json");
            jsonb.toJson(itemObj, rsp.getWriter());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo:Exception Handle
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().toLowerCase().startsWith("application/json")) {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        try {
            Jsonb jsonb = JsonbBuilder.create();
            ItemDTO itemObj = jsonb.fromJson(req.getReader(), ItemDTO.class);
            if (itemObj.getCode() == null || !itemObj.getCode().matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new RuntimeException("Invalid itemcode ");
            } else if (itemObj.getDescription() == null || !itemObj.getDescription().matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new RuntimeException("invalid Description Entered");
            } else if (itemObj.getQty() <= 0) {
                throw new RemoteException("invalid qty");
            } else if (itemObj.getPrice() <= 0) {
                throw new RemoteException("invalid price");
            }

            PreparedStatement ps =
                    connection.prepareStatement("UPDATE item SET description = ?, qty = ?, price = ?  WHERE code = ?");

            ps.setString(4, itemObj.getCode());
            ps.setString(1, itemObj.getDescription());
            ps.setInt(2, itemObj.getQty());
            ps.setDouble(3, itemObj.getPrice());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("Save Failed");
            }
            rsp.setStatus(HttpServletResponse.SC_CREATED);
            rsp.setContentType("application/json");
            jsonb.toJson(itemObj, rsp.getWriter());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getContentType()==null||!req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        try {
            Jsonb jsonb = JsonbBuilder.create();
            ItemDTO itemObj = jsonb.fromJson(req.getReader(), ItemDTO.class);
            //validation
            if (itemObj.getCode() == null || !itemObj.getCode().matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new RuntimeException("Invalid ID");
            }
            PreparedStatement ps =
                    connection.prepareStatement("delete from item where code=?");


            ps.setString(1, itemObj.getCode()); // Set the "id" parameter


            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("Delete Failed");

            }

//            ResultSet rst = ps.getGeneratedKeys();
//            rst.next();

            resp.setStatus(HttpServletResponse.SC_CREATED);
            //the created json is sent to frontend
            resp.setContentType("application/json");
            jsonb.toJson(itemObj, resp.getWriter());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo:Exception Handle
    }
    }


