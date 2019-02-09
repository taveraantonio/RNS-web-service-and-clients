/**
 * 
 */
package it.polito.dp2.RNS.lab3;

import it.polito.dp2.RNS.FactoryConfigurationError;

/**
 * Defines a factory API that enables applications to obtain one or more objects
 * implementing the {@link VehClient} interface.
 *
 */
public abstract class VehClientFactory {

	private static final String propertyName = "it.polito.dp2.RNS.lab3.VehClientFactory";
	
	protected VehClientFactory() {}
	
	/**
	 * Obtain a new instance of a <tt>VehClientFactory</tt>.
	 * 
	 * <p>
	 * This static method creates a new factory instance. This method uses the
	 * <tt>it.polito.dp2.RNS.lab3.VehClientFactory</tt> system property to
	 * determine the VehClientFactory implementation class to load.
	 * </p>
	 * <p>
	 * Once an application has obtained a reference to a
	 * <tt>VehClientFactory</tt> it can use the factory to obtain a new
	 * {@link VehClient} instance.
	 * </p>
	 * 
	 * @return a new instance of a <tt>VehClientFactory</tt>.
	 * 
	 * @throws FactoryConfigurationError if the implementation is not available 
	 * or cannot be instantiated.
	 */
	public static VehClientFactory newInstance() throws FactoryConfigurationError {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		if(loader == null) {
			loader = VehClientFactory.class.getClassLoader();
		}
		
		String className = System.getProperty(propertyName);
		if (className == null) {
			throw new FactoryConfigurationError("cannot create a new instance of a NfvClientFactory"
												+ "since the system property '" + propertyName + "'"
												+ "is not defined");
		}
		
		try {
			Class<?> c = (loader != null) ? loader.loadClass(className) : Class.forName(className);
			return (VehClientFactory) c.newInstance();
		} catch (Exception e) {
			throw new FactoryConfigurationError(e, "error instantiatig class '" + className + "'.");
		}
	}
	
	
	/**
	 * Creates a new instance of a {@link NfvClient} implementation.
	 * 
	 * @return a new instance of a {@link NfvClient} implementation.
	 * @throws {@link VehClientException} if an implementation of {@link NfvClient} cannot be created.
	 */
	public abstract VehClient newVehClient() throws VehClientException;
}