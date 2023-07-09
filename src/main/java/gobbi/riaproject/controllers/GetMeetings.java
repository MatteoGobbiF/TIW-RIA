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

import gobbi.riaproject.beans.*;
import gobbi.riaproject.dao.MeetingDAO;
import gobbi.riaproject.utils.ConnectionHandler;

@WebServlet("/GetMeetings")
public class GetMeetings extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public GetMeetings() {
        super(); 
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		//If the user is not logged in, say it in the response header
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.addHeader("loggedIn", "false");
			return;
		}
		User user = (User) session.getAttribute("user");
		
		//Retrieve the user's next meetings from the database
		List<Meeting> createdMeetings = new ArrayList<Meeting>();
		List<Meeting> invitedMeetings = new ArrayList<Meeting>();
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		try {
			createdMeetings=meetingDAO.getFutureMeetingsByCreator(user.getId());
			invitedMeetings=meetingDAO.getFutureInvitationsByUser(user.getId());
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover meetings");
			return;
		}
		
		Gson gson = new GsonBuilder().create();
		String createdJson = gson.toJson(createdMeetings);
		String invitedJson = gson.toJson(invitedMeetings);
		String responseJson = "["+createdJson+","+invitedJson+"]";
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseJson);
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
    
