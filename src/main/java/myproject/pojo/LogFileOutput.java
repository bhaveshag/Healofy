package myproject.pojo;

public class LogFileOutput {

	private long timestamp;
	private String exception;

	public LogFileOutput() {

	}

	public LogFileOutput(long timestamp, String exception) {
		this.timestamp = timestamp;
		this.exception = exception;
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
