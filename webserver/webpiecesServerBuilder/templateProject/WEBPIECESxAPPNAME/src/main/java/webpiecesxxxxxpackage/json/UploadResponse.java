package webpiecesxxxxxpackage.json;

public class UploadResponse {

	private boolean isSuccess;

	public UploadResponse() {
	}
	
	public UploadResponse(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
}
