package org.playorm.nio.api.deprecated;

/** 
 * @author dhiller
 */
public class CorruptPacketException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final boolean isTrailer;
	private final boolean isHeader;

	/**
	 * 
	 */
	public CorruptPacketException(boolean isHeader, boolean isTrailer) {
		this(null, null, isHeader, isTrailer);
	}

	/**
	 * @param message
	 */
	public CorruptPacketException(String message, boolean isHeader, boolean isTrailer) {
		this(message, null, isHeader, isTrailer);
	}

	/**
	 * @param cause
	 */
	public CorruptPacketException(Throwable cause, boolean isHeader, boolean isTrailer) {
		this(null, cause, isHeader, isTrailer);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public CorruptPacketException(String message, Throwable cause, boolean isHeader, boolean isTrailer) {
		super(message, cause);
		this.isHeader = isHeader;
		this.isTrailer = isTrailer;
	}
	
	
	public boolean isHeaderCorrupt() {
		return isHeader;
	}
	public boolean isTrailerCorrupt() {
		return isTrailer;
	}
}
