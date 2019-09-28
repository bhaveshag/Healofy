package myproject.pojo;

public class LogFileInput {

	private String id;
	private long timestamp;
	private String exception;

	public LogFileInput() {

	}

	public LogFileInput(String id, long timestamp, String exception) {
		this.id = id;
		this.timestamp = timestamp;
		this.exception = exception;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}
	
	

}
