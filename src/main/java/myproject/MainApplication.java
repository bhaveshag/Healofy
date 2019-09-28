
package myproject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import myproject.pojo.MyServiceInput;

@SpringBootApplication
public class MainApplication {

	private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

	//	private static int threadPoolSize = 100;
	private static final int TERMINATION_POLL_PERIOD = 10;
	private final AtomicInteger errorCountTotal = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = SpringApplication.run(MainApplication.class, args);
		MainApplication mainApp = ctx.getBean(MainApplication.class);
		logger.info("Let's inspect the beans provided by Spring Boot: {}", mainApp);
	}

	public long processInput(List<MyServiceInput> listInput) {
		long totalTime = 0;
		for (MyServiceInput input : listInput) {

			long timeForParallelTasks = runParallelTasks(input);
			logger.info("input {}", input.toString());
			logger.info("total time taken timeForParallelTasks {}", timeForParallelTasks);
			totalTime = totalTime + timeForParallelTasks;
		}
		return totalTime;
	}

	private long runParallelTasks(MyServiceInput input) {
		final ExecutorService executorService = Executors.newFixedThreadPool(input.getCount());
		final BoundedExecutor boundedExecutor = new BoundedExecutor(executorService, 2 * input.getCount());
		long startTime = System.currentTimeMillis();
		try {
			submitTask(input, boundedExecutor);
			while (!boundedExecutor.isReadyToTerminate()) {
				logger.info("Awaiting termination of all threads");
				Thread.sleep(TERMINATION_POLL_PERIOD);
			}
		} catch (Exception e) {
			logger.error("Error occured while running the Update SolarFlag script", e);
		} finally {
			boundedExecutor.shutdown();
		}
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}

	private long runSequentialTasks(MyServiceInput input) {
		long startTime = System.currentTimeMillis();
		try {
			int delay = getDelayFromUrl(input.getUrl());
			for (int i = 0; i < input.getCount(); i++) {
				doTask(delay);
			}
		} catch (Exception e) {
			logger.error("Error occured while running the Update SolarFlag script", e);
		} finally {
		}
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}

	private void submitTask(final MyServiceInput input, final BoundedExecutor boundedExecutor) {
		int delay = getDelayFromUrl(input.getUrl());
		for (int i = 0; i < input.getCount(); i++) {
			try {
				boundedExecutor.submitTask(new Runnable() {

					@Override
					public void run() {
						try {
							doTask(delay);
						} catch (InterruptedException e) {
							logger.error("Unexpected InterruptedException while submitting task {}", input, e);
							errorCountTotal.incrementAndGet();
							Thread.currentThread().interrupt();
						}
					}
				});
			} catch (final Exception e) {
				logger.error("Unexpected exception while submitting task {}", input, e);
				errorCountTotal.incrementAndGet();
			}
		}

	}

	private void doTask(int delay) throws InterruptedException {
		logger.info("Thread {} ", Thread.currentThread());
		logger.info("Delay {} ", delay);
		Thread.sleep(delay);
	}

	private int getDelayFromUrl(String url) {
		List<NameValuePair> params;
		try {
			params = URLEncodedUtils.parse(new URI(url), "UTF-8");
			for (NameValuePair param : params) {
				if (param.getName().equalsIgnoreCase("delay")) {
					return Integer.parseInt(param.getValue());
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
