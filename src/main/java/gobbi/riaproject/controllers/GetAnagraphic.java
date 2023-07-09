package gobbi.riaproject.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gobbi.riaproject.dao.UserDAO;
import gobbi.riaproject.beans.User;
import gobbi.riaproject.utils.ConnectionHandler;

@WebServlet("/GetAnagraphic")
public class GetAnagraphic extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    public GetAnagraphic() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.addHeader("loggedIn", "false");
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		
				try {
					users = userDAO.listAllUsersButOne(((User)session.getAttribute("user")).getId());
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Not possible to recover users");
					return;
				}		
				Gson gson = new GsonBuilder().create();
				String userJson = gson.toJson(users);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(userJson);
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
