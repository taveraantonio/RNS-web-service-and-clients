package it.polito.dp2.RNS.sol3.service;

public class BadStateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BadStateException() {
	}

	public BadStateException(String message) {
		super(message);
	}

	public BadStateException(Throwable cause) {
		super(cause);
	}

	public BadStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
