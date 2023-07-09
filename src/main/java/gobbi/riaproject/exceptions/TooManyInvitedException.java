package gobbi.riaproject.exceptions;

public class TooManyInvitedException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public TooManyInvitedException(String message) {
		super(message);
	}
}
