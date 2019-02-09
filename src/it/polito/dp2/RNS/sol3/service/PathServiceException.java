package it.polito.dp2.RNS.sol3.service;


public class PathServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PathServiceException() {
	}

	public PathServiceException(String message) {
		super(message);
	}

	public PathServiceException(Throwable cause) {
		super(cause);
	}

	public PathServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
