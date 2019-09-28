package myproject.pojo;

public class MyServiceOutput {

	private int totalTime;
	private String error;

	public MyServiceOutput() {

	}

	public MyServiceOutput(int totalTime, String error) {
		this.totalTime = totalTime;
		this.error = error;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
