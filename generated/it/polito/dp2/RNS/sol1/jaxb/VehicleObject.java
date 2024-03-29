//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.01.16 at 05:27:48 PM CET 
//


package it.polito.dp2.RNS.sol1.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for VehicleObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VehicleObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vehicle">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="localization" type="{}localization"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="vehicleID" use="required" type="{}plateID" />
 *                 &lt;attribute name="vehicleType" use="required" type="{}VehicleType" />
 *                 &lt;attribute name="vehicleState" use="required" type="{}VehicleState" />
 *                 &lt;attribute name="entryTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VehicleObject", propOrder = {
    "vehicle"
})
public class VehicleObject {

    @XmlElement(required = true)
    protected VehicleObject.Vehicle vehicle;

    /**
     * Gets the value of the vehicle property.
     * 
     * @return
     *     possible object is
     *     {@link VehicleObject.Vehicle }
     *     
     */
    public VehicleObject.Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Sets the value of the vehicle property.
     * 
     * @param value
     *     allowed object is
     *     {@link VehicleObject.Vehicle }
     *     
     */
    public void setVehicle(VehicleObject.Vehicle value) {
        this.vehicle = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="localization" type="{}localization"/>
     *       &lt;/sequence>
     *       &lt;attribute name="vehicleID" use="required" type="{}plateID" />
     *       &lt;attribute name="vehicleType" use="required" type="{}VehicleType" />
     *       &lt;attribute name="vehicleState" use="required" type="{}VehicleState" />
     *       &lt;attribute name="entryTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "localization"
    })
    public static class Vehicle {

        @XmlElement(required = true)
        protected Localization localization;
        @XmlAttribute(name = "vehicleID", required = true)
        protected String vehicleID;
        @XmlAttribute(name = "vehicleType", required = true)
        protected VehicleType vehicleType;
        @XmlAttribute(name = "vehicleState", required = true)
        protected VehicleState vehicleState;
        @XmlAttribute(name = "entryTime", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar entryTime;

        /**
         * Gets the value of the localization property.
         * 
         * @return
         *     possible object is
         *     {@link Localization }
         *     
         */
        public Localization getLocalization() {
            return localization;
        }

        /**
         * Sets the value of the localization property.
         * 
         * @param value
         *     allowed object is
         *     {@link Localization }
         *     
         */
        public void setLocalization(Localization value) {
            this.localization = value;
        }

        /**
         * Gets the value of the vehicleID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVehicleID() {
            return vehicleID;
        }

        /**
         * Sets the value of the vehicleID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVehicleID(String value) {
            this.vehicleID = value;
        }

        /**
         * Gets the value of the vehicleType property.
         * 
         * @return
         *     possible object is
         *     {@link VehicleType }
         *     
         */
        public VehicleType getVehicleType() {
            return vehicleType;
        }

        /**
         * Sets the value of the vehicleType property.
         * 
         * @param value
         *     allowed object is
         *     {@link VehicleType }
         *     
         */
        public void setVehicleType(VehicleType value) {
            this.vehicleType = value;
        }

        /**
         * Gets the value of the vehicleState property.
         * 
         * @return
         *     possible object is
         *     {@link VehicleState }
         *     
         */
        public VehicleState getVehicleState() {
            return vehicleState;
        }

        /**
         * Sets the value of the vehicleState property.
         * 
         * @param value
         *     allowed object is
         *     {@link VehicleState }
         *     
         */
        public void setVehicleState(VehicleState value) {
            this.vehicleState = value;
        }

        /**
         * Gets the value of the entryTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEntryTime() {
            return entryTime;
        }

        /**
         * Sets the value of the entryTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEntryTime(XMLGregorianCalendar value) {
            this.entryTime = value;
        }

    }

}
