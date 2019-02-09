package it.polito.dp2.RNS.sol3.service;
// This validator performs JAXB unmarshalling with validation
// against the schema
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXParseException;

@Provider
@Consumes({"application/xml","text/xml"})
public class XmlValidationProvider<T> implements MessageBodyReader<T> {
	final String jaxbPackage = "it.polito.dp2.RNS.sol3.jaxb";
	Schema schema;
	JAXBContext jc;
	Logger logger;
	String responseBodyTemplate;


	public XmlValidationProvider() {
		logger = Logger.getLogger(XmlValidationProvider.class.getName());

		try {				
			InputStream schemaStream = XmlValidationProvider.class.getResourceAsStream("/xsd/RnsSystem.xsd");
			if (schemaStream == null) {
				logger.log(Level.SEVERE, "xml schema file Not found.");
				throw new IOException();
			}
            SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(new StreamSource(schemaStream));
            
            jc = JAXBContext.newInstance( jaxbPackage );
            
			InputStream templateStream = XmlValidationProvider.class.getResourceAsStream("/html/BadRequestTemplate.html");
			if (templateStream == null) {
				logger.log(Level.SEVERE, "html template file Not found.");
				throw new IOException();
			}
	        BufferedReader reader = new BufferedReader(new InputStreamReader(templateStream));
	        StringBuilder out = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
			responseBodyTemplate = out.toString();

            logger.log(Level.INFO, "XmlProvider initialized successfully");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error initializing XmlProvider. Service will not work properly.", e);
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return jaxbPackage.equals(type.getPackage().getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		//define the unmarshaller 
		Unmarshaller unmarshaller;
		try {
			//create the unmrshaller and set the schema 
			unmarshaller = jc.createUnmarshaller();
	        unmarshaller.setSchema(schema);
			try {
				Object obj = unmarshaller.unmarshal(entityStream);
				if (obj.getClass().equals(type))
					return (T) obj;
				else {
					logger.log(Level.WARNING, "Request body validation error. Wrong Type.");
					BadRequestException bre = new BadRequestException("Request body validation error. Wrong Type.");
					String responseBody = responseBodyTemplate.replaceFirst("___TO_BE_REPLACED___", "Request body validation error. Wrong Type.");
					Response response = Response.fromResponse(bre.getResponse()).entity(responseBody).type("text/html").build();
					throw new BadRequestException("Request body validation error", response);
				}
			} catch (JAXBException ex) {
				logger.log(Level.WARNING, "Request body validation error.", ex);
				Throwable linked = ex.getLinkedException();
				String validationErrorMessage = "Request body validation error";
				if (linked != null && linked instanceof SAXParseException)
					validationErrorMessage += ": " + linked.getMessage();
				BadRequestException bre = new BadRequestException("Request body validation error");
				String responseBody = responseBodyTemplate.replaceFirst("___TO_BE_REPLACED___", validationErrorMessage);
				Response response = Response.fromResponse(bre.getResponse()).entity(responseBody).type("text/html").build();
				throw new BadRequestException("Request body validation error", response);
			}
		} catch (JAXBException e) {
			logger.log(Level.INFO, "Unable to initialize unmarshaller.", e);
			throw new InternalServerErrorException();
		}

	}

}
