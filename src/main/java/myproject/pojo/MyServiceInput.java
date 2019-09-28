package myproject.pojo;

public class MyServiceInput {

	private String url;
	private boolean parallel;
	private int count;

	public MyServiceInput() {
		
	}
	public MyServiceInput(String url, boolean parallel, int count) {
		this.url = url;
		this.parallel = parallel;
		this.count = count;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
