/*******************************************************************************
 * Copyright (c) 2013, 2014 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.item.utilities.trilinos;

import org.eclipse.ice.datastructures.ICEObject.ICEJAXBManipulator;
import org.eclipse.ice.datastructures.ICEObject.Persistable;
import org.eclipse.ice.datastructures.form.AllowedValueType;
import org.eclipse.ice.datastructures.form.Entry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * <!-- begin-UML-doc -->
 * <p>
 * This class represents a Teuchos parameter. It is a simple data structure that
 * only holds data.
 * </p>
 * <!-- end-UML-doc -->
 * 
 * @author Jay Jay Billings
 * @generated 
 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
@XmlRootElement(name = "Parameter")
public class Parameter implements Persistable {
	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The name of the parameter.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@XmlAttribute
	public String name;
	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The type of the parameter.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@XmlAttribute
	public String type;
	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The value of the parameter.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@XmlAttribute
	public String value;

	/**
	 * This attribute describes the parameter's AllowedValueType for conversion
	 * to an ICE Entry.
	 */
	@XmlTransient
	private AllowedValueType allowedType = AllowedValueType.Continuous;

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation returns this parameter as an ICE Entry. The name and value
	 * are translated directly. The value of the parameter at the time of
	 * construction as the default value. The default id value is 1 and the
	 * description is undefined. The AllowedValueType of the Entry is mapped to
	 * Discrete for a parameter type of bool, Undefined for a parameter type of
	 * string and Continuous for a parameter type of int or double. The latter
	 * has bounds of Double.POSITIVE_INFINITY and Double.NEGATIVE_INFINITY.
	 * Arrays of all types have an AllowedValueType of Undefined.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @return <p>
	 *         The Entry.
	 *         </p>
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public Entry toEntry() {
		// begin-user-code

		// Determine the type - default to continuous
		allowedType = AllowedValueType.Continuous;
		// Switch to Discrete or Undefined as required, but only if the type is
		// defined
		if (type != null) {
			if (type.contains("string") || type.contains("Array(")) {
				allowedType = AllowedValueType.Undefined;
			} else if (type.contains("bool")) {
				allowedType = AllowedValueType.Discrete;
			}
		}

		// Create the Entry
		Entry entry = new Entry() {
			protected void setup() {
				// Set the details
				allowedValueType = Parameter.this.allowedType;
				defaultValue = Parameter.this.value;
				value = Parameter.this.value;
				// Figure out the allowed values
				if (allowedValueType.equals(AllowedValueType.Continuous)) {
					allowedValues.add(String.valueOf(Double.NEGATIVE_INFINITY));
					allowedValues.add(String.valueOf(Double.POSITIVE_INFINITY));
				} else if (allowedValueType.equals(AllowedValueType.Discrete)) {
					allowedValues.add("true");
					allowedValues.add("false");
				}
			}
		};
		entry.setName(name);
		entry.setDescription(name);
		entry.setTag(type);
		entry.setId(1);

		return entry;
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation loads the Parameter from an Entry. It performs the inverse
	 * of the toEntry() operation with the added restrictions that all Arrays
	 * (determined by the presence of "{" and
	 * "}) have a type of Array(string) and all floating point numbers (determined by the presence of "
	 * ." for an AllowedValueType of Continuous) have type double. All types of
	 * Discrete are consider to be bool.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @param entry
	 *            <p>
	 *            The Entry.
	 *            </p>
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void fromEntry(Entry entry) {
		// begin-user-code

		// Only load if the Entry is not null
		if (entry != null) {
			// Get the type and value
			AllowedValueType allowedType = entry.getValueType();
			String entryValue = entry.getValue();
			// Figure out the type
			if (allowedType.equals(AllowedValueType.Continuous)) {
				type = (entryValue.contains(".")) ? "double" : "int";
			} else if (allowedType.equals(AllowedValueType.Undefined)) {
				type = (!entryValue.contains("{") && !entryValue.contains("}")) ? "string"
						: "Array(string)";
			} else {
				type = "bool";
			}
			// Set the name and value
			name = entry.getName();
			value = entryValue;
		}

		return;

		// end-user-code
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Persistable#loadFromXML(InputStream inputStream)
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void loadFromXML(InputStream inputStream) {
		// begin-user-code

		// Initialize JAXBManipulator
		ICEJAXBManipulator jaxbManipulator = new ICEJAXBManipulator();

		// Call the read() on jaxbManipulator to create a new Object instance
		// from the inputStream
		Object dataObject;
		try {
			dataObject = jaxbManipulator.read(this.getClass(), inputStream);
			// Copy contents of new object into current data structure
			this.name = ((Parameter) dataObject).name;
			this.type = ((Parameter) dataObject).type;
			this.value = ((Parameter) dataObject).value;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Nullerize jaxbManipilator
		jaxbManipulator = null;

		return;
		// end-user-code
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Persistable#persistToXML(OutputStream outputStream)
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void persistToXML(OutputStream outputStream) {
		// begin-user-code

		// Initialize JAXBManipulator
		ICEJAXBManipulator jaxbManipulator = new ICEJAXBManipulator();

		// Call the write() on jaxbManipulator to write to outputStream
		try {
			jaxbManipulator.write(this, outputStream);

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Nullerize jaxbManipilator
		jaxbManipulator = null;

		return;

		// end-user-code
	}
}