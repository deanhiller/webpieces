package webpiecesxxxxxpackage.json;

public class FileUploadPiece {

	private String uniqueFileId;
	private int position;
	private int sliceSize;
	private String fileData;
	private String fileName;
	private String fileType;
	private String secureToken;
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getSliceSize() {
		return sliceSize;
	}
	public void setSliceSize(int sliceSize) {
		this.sliceSize = sliceSize;
	}
	public String getFileData() {
		return fileData;
	}
	public void setFileData(String fileData) {
		this.fileData = fileData;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getSecureToken() {
		return secureToken;
	}
	public void setSecureToken(String secureToken) {
		this.secureToken = secureToken;
	}
	public String getUniqueFileId() {
		return uniqueFileId;
	}
	public void setUniqueFileId(String uniqueFileId) {
		this.uniqueFileId = uniqueFileId;
	}
	
}
