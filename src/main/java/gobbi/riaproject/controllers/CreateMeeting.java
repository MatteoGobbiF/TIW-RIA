package gobbi.riaproject.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gobbi.riaproject.beans.*;
import gobbi.riaproject.dao.MeetingDAO;
import gobbi.riaproject.exceptions.*;
import gobbi.riaproject.utils.ConnectionHandler;

@WebServlet("/CreateMeeting")
@MultipartConfig
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null; 
	
    public CreateMeeting() {
        super();
    }

    public void init() throws ServletException {
  		connection = ConnectionHandler.getConnection(getServletContext());
  	}
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.addHeader("loggedIn", "false");
			return;
		}
		
		String title = null;
		String dateTime = null;
		Integer duration = null;
		Integer max = null;
		String invitedUsersJson = null;
		List<String> invitedUsers  = null;
		
		try {
	
			title = request.getParameter("title");
		    dateTime = request.getParameter("dateTime");
		    duration = Integer.parseInt(request.getParameter("duration"));
		    max = Integer.parseInt(request.getParameter("max"));
		    invitedUsersJson = request.getParameter("invitedUsers");
		    invitedUsers = new Gson().fromJson(invitedUsersJson, new TypeToken<List<String>>(){}.getType());
		    
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		    LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
		    if (localDateTime.isBefore(LocalDateTime.now()))
		        throw new InvalidDateTimeException("DateTime is in the past");
			if(title==null || title.isEmpty() || duration<=0 || duration>1440 || max<1 || invitedUsers==null)
				throw new BadParametersException("Missing or wrong value");
			if(invitedUsers.size()>=max)
				throw new TooManyInvitedException("Too many invited users");
		    		    
		} catch (DateTimeException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Bad date");
			return;
		} catch (NumberFormatException | NullPointerException | BadParametersException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or empty credential value");
			return;
		} catch (InvalidDateTimeException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Impossible to create a meeting in the past");
			return;
		} catch (TooManyInvitedException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Too many invited people, unable to create the meeting");
			return;
		}
		
		Meeting meeting = new Meeting(title, dateTime, duration, max);
		
		try {
			//If the number of invited users is below the maximum, create the meeting in the database and redirect to Home
			MeetingDAO meetingDAO = new MeetingDAO(connection);
			meetingDAO.createNewMeeting(meeting, ((User)session.getAttribute("user")).getId(), invitedUsers);
			response.setStatus(HttpServletResponse.SC_OK);
			return;
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Impossible to create meeting");
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
