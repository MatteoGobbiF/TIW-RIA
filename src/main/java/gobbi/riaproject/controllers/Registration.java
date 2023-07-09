package gobbi.riaproject.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gobbi.riaproject.dao.UserDAO;
import gobbi.riaproject.utils.ConnectionHandler;
import gobbi.riaproject.exceptions.*;


@WebServlet("/Registration")
@MultipartConfig
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public Registration() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String username = null;
		String name = null;
		String surname=null;
		String email=null;
		String password=null;
		String re_password=null;
		
		try {
			//Get data from the form
			username=StringEscapeUtils.escapeJava(request.getParameter("username"));
			name=StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname=StringEscapeUtils.escapeJava(request.getParameter("surname"));
			email=StringEscapeUtils.escapeJava(request.getParameter("email"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			re_password=StringEscapeUtils.escapeJava(request.getParameter("re_password"));
			
			//Check validity
			if (username == null || name == null || surname == null || email == null || password == null
				|| re_password == null || username.isEmpty() || name.isEmpty() || surname.isEmpty()
				|| email.isEmpty() || password.isEmpty() || re_password.isEmpty()) 
				throw new BadParametersException("Missing or empty credential value");
		} catch (BadParametersException e) {
			//In case something is missing send error
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or empty credential value");
			return;
		}
			
		UserDAO userDao = new UserDAO(connection);
			
		//Check if the two passwords match, the email syntax and if the username and the email aren't already taken 
		try {
			
			if(!password.equals(re_password))
				throw new PasswordsNotMatchingException("The two passwords are different");
			String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(email);
			if(!matcher.matches())
				throw new InvalidEmailSyntaxException("Invalid email");
			if(userDao.isUsernameTaken(username))
				throw new UsernameTakenException("Username taken");
			if(userDao.isEmailTaken(email))
				throw new EmailTakenException("Email taken");
			
			//If something is wrong, throw an exception, otherwise register the new user in the database
			
			userDao.registerNewUser(username, name, surname, email, password);
			
			//If everything goes well prepare a message that confirms the registration has been successful
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("Registration Succesful");
			
			//If an Exception is thrown, send the correct error message to the client
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Registration failed");
			return;
		} catch (PasswordsNotMatchingException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The two passwords don't match");
			return;
		} catch (InvalidEmailSyntaxException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid email");
			return;
		} catch (UsernameTakenException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Username taken");
			return;
		} catch (EmailTakenException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("There is already an account with that email");
			return;
		}				
    }
    
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}