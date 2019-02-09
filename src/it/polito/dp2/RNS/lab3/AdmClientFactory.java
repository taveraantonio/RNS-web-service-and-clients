/**
 * 
 */
package it.polito.dp2.RNS.lab3;

import it.polito.dp2.RNS.FactoryConfigurationError;

/**
 * Defines a factory API that enables applications to obtain one or more objects
 * implementing the {@link AdmClient} interface.
 *
 */
public abstract class AdmClientFactory {

	private static final String propertyName = "it.polito.dp2.RNS.lab3.AdmClientFactory";
	
	protected AdmClientFactory() {}
	
	/**
	 * Obtain a new instance of a <tt>AdmClientFactory</tt>.
	 * 
	 * <p>
	 * This static method creates a new factory instance. This method uses the
	 * <tt>it.polito.dp2.RNS.lab3.AdmClientFactory</tt> system property to
	 * determine the AdmClientFactory implementation class to load.
	 * </p>
	 * <p>
	 * Once an application has obtained a reference to a
	 * <tt>AdmClientFactory</tt> it can use the factory to obtain a new
	 * {@link AdmClient} instance.
	 * </p>
	 * 
	 * @return a new instance of a <tt>AdmClientFactory</tt>.
	 * 
	 * @throws FactoryConfigurationError if the implementation is not available 
	 * or cannot be instantiated.
	 */
	public static AdmClientFactory newInstance() throws FactoryConfigurationError {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		if(loader == null) {
			loader = AdmClientFactory.class.getClassLoader();
		}
		
		String className = System.getProperty(propertyName);
		if (className == null) {
			throw new FactoryConfigurationError("cannot create a new instance of a NfvClientFactory"
												+ "since the system property '" + propertyName + "'"
												+ "is not defined");
		}
		
		try {
			Class<?> c = (loader != null) ? loader.loadClass(className) : Class.forName(className);
			return (AdmClientFactory) c.newInstance();
		} catch (Exception e) {
			throw new FactoryConfigurationError(e, "error instantiatig class '" + className + "'.");
		}
	}
	
	
	/**
	 * Creates a new instance of a {@link NfvClient} implementation.
	 * 
	 * @return a new instance of a {@link NfvClient} implementation.
	 * @throws {@link AdmClientException} if an implementation of {@link NfvClient} cannot be created.
	 */
	public abstract AdmClient newAdmClient() throws AdmClientException;
}