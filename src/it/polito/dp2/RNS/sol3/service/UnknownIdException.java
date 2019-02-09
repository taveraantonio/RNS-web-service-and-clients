package it.polito.dp2.RNS.sol3.service;

public class UnknownIdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownIdException() {
	}

	public UnknownIdException(String message) {
		super(message);
	}

	public UnknownIdException(Throwable cause) {
		super(cause);
	}

	public UnknownIdException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownIdException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
