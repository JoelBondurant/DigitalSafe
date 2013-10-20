package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.digitalsafe.crypto.HashUtility;
import com.analyticobjects.utility.ByteUtility;
import com.analyticobjects.utility.StringUtility;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Keys for a DHT.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public final class DHTKey implements Comparable, Serializable {

	public static final int SIZE_IN_BITS = 256;
	public static final int SIZE_IN_BYTES = 32;
	public static final int BYTE_SIZE = 8;
	private byte[] value;

	private DHTKey() {}
	
	/**
	 * Generate a new DHTKey.
	 *
	 * @return A newly generated key.
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 * @throws InvalidKeyException
	 */
	public static DHTKey gen() throws NoSuchAlgorithmException, InterruptedException, InvalidKeyException {
		DHTKey dhtKey = new DHTKey();
		dhtKey.value = HashUtility.generateRandom256();
		if (dhtKey.value.length != SIZE_IN_BYTES && dhtKey.value.length != SIZE_IN_BITS / BYTE_SIZE) {
			throw new InvalidKeyException("Key length verification failed.");
		}
		return dhtKey;
	}

	/**
	 * Generate a DHTKey for a byteBlock to store.
	 * 
	 * @param byteBlock A block of bytes to store.
	 * @return A DHTKey for storing the block of bytes.
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.security.InvalidKeyException
	 */
	public static DHTKey gen(byte[] byteBlock) throws NoSuchAlgorithmException, InvalidKeyException {
		DHTKey dhtKey = new DHTKey();
		dhtKey.value = HashUtility.simpleHash256(byteBlock);
		if (dhtKey.value.length != SIZE_IN_BYTES && dhtKey.value.length != SIZE_IN_BITS / BYTE_SIZE) {
			throw new InvalidKeyException("Key length verification failed.");
		}
		return dhtKey;
	}

	/**
	 * This DHTKey ordering is based on simple mapping to base two positive integers.
	 *
	 * @param other Another key to compare to.
	 * @return
	 */
	public int compareDHTKey(DHTKey other) {
		for (int byteNum = SIZE_IN_BYTES - 1; byteNum >= 0; byteNum--) {
			for (int bitNum = BYTE_SIZE - 1; bitNum >= 0; bitNum--) {
				boolean thisBit = ByteUtility.bitAt(bitNum, this.value[byteNum]);
				boolean otherBit = ByteUtility.bitAt(bitNum, other.value[byteNum]);
				if (thisBit && !otherBit) {
					return 1;
				}
				if (!thisBit && otherBit) {
					return -1;
				}
			}
		}
		return 0;
	}

	/**
	 * The angle for this key within the keyspace.
	 *
	 * @return Estimate for key angle, in units of tau (2pi) [0, 1].
	 */
	public double angle() {
		return angle(this);
	}

	/**
	 * Calculate an estimate for the angle between keys.
	 *
	 * @param other Another key.
	 * @return A crude estimate for the minimum angle between the keys.
	 */
	public double angleBetween(DHTKey other) {
		if (this.equals(other)) {
			return 0.0;
		}
		double thisAngle = this.angle();
		double otherAngle = other.angle();
		double max = Math.max(thisAngle, otherAngle);
		double min = Math.min(thisAngle, otherAngle);
		double absAngle = max - min;
		if (absAngle < 0.5) {
			return absAngle;
		}
		return (1.0 - max) + (min);
	}

	/**
	 * Calculate the angle to a key in units of tau (2pi). 0b0{256} is at angle 0, and 0b1{256} is at at angle 1.
	 *
	 * @param dhtKey A DHTKey object to measure.
	 * @return An approximate angle at which the key lies in the keyspace in [0.00, 1.00].
	 */
	public static double angle(DHTKey dhtKey) {
		double angle = 0.0;
		int bitIndex = 1;
		for (int byteNum = SIZE_IN_BYTES - 1; byteNum >= 0; byteNum--) {
			for (int bitNum = BYTE_SIZE - 1; bitNum >= 0; bitNum--) {
				boolean thisBit = ByteUtility.bitAt(bitNum, dhtKey.value[byteNum]);
				if (thisBit) {
					angle += Math.pow(0.5, bitIndex);
				}
				bitIndex++;
				if (bitIndex > 16) {
					return angle;
				}
			}
		}
		return angle;
	}

	/**
	 * Generate a new key between this and the other.
	 *
	 * @param other Another DHT key to bound the splitting.
	 * @return
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.lang.InterruptedException
	 * @throws java.security.InvalidKeyException
	 */
	public DHTKey split(DHTKey other) throws NoSuchAlgorithmException, InterruptedException, InvalidKeyException {
		if (other == null || this.equals(other)) {
			return null; // no.
		}
		DHTKey maxKey, minKey;
		if (this.compareDHTKey(other) > 0) {
			maxKey = this;
			minKey = other;
		} else {
			maxKey = other;
			minKey = this;
		}
		DHTKey newDHTKey = DHTKey.gen();
		newDHTKey.boundAboveBy(maxKey);
		newDHTKey.boundBelowBy(minKey);
		return newDHTKey;
	}

	private void boundAboveBy(DHTKey upperBound) {
		for (int byteNum = SIZE_IN_BYTES - 1; byteNum >= 0; byteNum--) {
			for (int bitNum = BYTE_SIZE - 1; bitNum >= 0; bitNum--) {
				boolean thisBit = ByteUtility.bitAt(bitNum, this.value[byteNum]);
				boolean boundingBit = ByteUtility.bitAt(bitNum, upperBound.value[byteNum]);
				if (!thisBit && boundingBit) {
					return;
				}
				if (thisBit && !boundingBit) {
					this.value[byteNum] = ByteUtility.setBitAt(bitNum, false, this.value[byteNum]);
				}
			}
		}
	}

	private void boundBelowBy(DHTKey lowerBound) {
		for (int byteNum = SIZE_IN_BYTES - 1; byteNum >= 0; byteNum--) {
			for (int bitNum = BYTE_SIZE - 1; bitNum >= 0; bitNum--) {
				boolean thisBit = ByteUtility.bitAt(bitNum, this.value[byteNum]);
				boolean boundingBit = ByteUtility.bitAt(bitNum, lowerBound.value[byteNum]);
				if (!thisBit && boundingBit) {
					this.value[byteNum] = ByteUtility.setBitAt(bitNum, true, this.value[byteNum]);
				}
				if (thisBit && !boundingBit) {
					return;
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("com.analyticobjects.digitalsafe.net.DHTKey::");
		sb.append("\n<");
		sb.append(ByteUtility.toHexString0x(value));
		sb.append(">,\n<");
		sb.append(StringUtility.insertLineBreaks(ByteUtility.toBinString0b(value), 80));
		sb.append(">");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DHTKey)) {
			return false;
		}
		DHTKey other = (DHTKey) obj;
		return (compareDHTKey(other) == 0);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public int compareTo(Object obj) {
		if (obj == null || !(obj instanceof DHTKey)) {
			return 0;
		}
		DHTKey other = (DHTKey) obj;
		return compareDHTKey(other);
	}

}
