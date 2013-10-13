package com.analyticobjects.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class for object serialization.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class SerializableUtility {

	/**
	 * Deserialize a serialized object.
	 *
	 * @param <T> Object type parameter for parameterized type casting.
	 * @param serializedObject A byte array representation of the instance of T.
	 * @return The deserialized instance of T.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> T inflate(byte[] serializedObject) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedObject))) {
			return (T) ois.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			Logger.getLogger(SerializableUtility.class.getName()).log(Level.FINE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
	}

	/**
	 * Serialize a serializable object.
	 *
	 * @param <T> Type param.
	 * @param serializableObject A serializable object.
	 * @return A serialized representation of the input.
	 * @throws IOException
	 */
	public static <T extends Serializable> byte[] deflate(T serializableObject) throws IOException {
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);) {
			oos.writeObject(serializableObject);
			oos.flush();
			return bos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(SerializableUtility.class.getName()).log(Level.FINE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
	}

}
