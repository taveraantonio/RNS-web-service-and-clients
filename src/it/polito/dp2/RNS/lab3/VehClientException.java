/*
 * File: NFFGClientException.java
 * 
 * Copyright (C) 2005-2008 - Politecnico di Torino
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Author(s): Paolo Maggi <paolo.maggi@polito.it>
 *  
 * Created on Jan 16, 2008
 *
 */

/**
 * An error occurred creating a WorkflowMonitor instance.
 */
package it.polito.dp2.RNS.lab3;

/**
 * Thrown if an implementation of {@link NfvClient} cannot be created.
 */
@SuppressWarnings("serial")
public class VehClientException extends Exception {

	/**
	 * The underlying cause of this exception, if any.
	 */
	private Exception exception;

	/**
	 * Constructor with no detail message.
	 */
	public VehClientException() {
		super();
	}

	/**
	 * Constructor with the specified detail message.
	 * 
	 * @param msg the detail message
	 */
	public VehClientException(String msg) {
		super(msg);
	}

	/**
	 * Constructor with the specified underlying cause.
	 * 
	 * @param e the underlying cause of this exception
	 */
	public VehClientException(Exception e) {
		super(e);
		exception = e;
	}

	/**
	 * Constructor with the specified underlying cause and detail message.
	 * 
	 * @param e the underlying cause of this exception
	 * @param msg the detail message
	 */
	public VehClientException(Exception e, String msg) {
		super(msg, e);
		exception = e;
	}

	/**
	 * Returns the message for this exception, if any.
	 */
	public String getMessage() {
		String message = super.getMessage();
		if (message == null && exception != null) {
			message = exception.getMessage();
		}
		return message;
	}

	/**
	 * Returns the underlying cause of this error, if any.
	 * 
	 * @return Returns the underlying cause of this error, if any.
	 */
	public Exception getException() {
		return exception;
	}
}