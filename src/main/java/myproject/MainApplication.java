
package myproject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import myproject.pojo.LogFileInput;
import myproject.pojo.LogFileOutput;

@SpringBootApplication
public class MainApplication {

	private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

	private static final int TERMINATION_POLL_PERIOD = 10;
	private final AtomicInteger errorCountTotal = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = SpringApplication.run(MainApplication.class, args);
		MainApplication mainApp = ctx.getBean(MainApplication.class);
		mainApp.processInput();
		logger.info("In main thread");
		SpringApplication.exit(ctx);
	}

	public long processInput() throws InterruptedException {
		logger.info("Starting to reading files");
		HashMap<String, Boolean> fileStatusMap = new HashMap<>();

		ConcurrentHashMap<Long, List<LogFileOutput>> concurrentMapForOutput = new ConcurrentHashMap<>();

		File fileDirectory = new File("/Users/Bash/my_github/Healofy/src/main/resources");
		if (fileDirectory.isDirectory()) {
			logger.info("Found Log directory {}", fileDirectory.getAbsolutePath());
		} else {
			logger.info("Not found the directory path");
		}
		int emptCounter = 0;
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BoundedExecutor boundedExecutor = new BoundedExecutor(executorService, 2 * 10);
		long startTime = System.currentTimeMillis();

		while (true) {
			boolean exitCounterState = false;
			File[] list = fileDirectory.listFiles();
			if (list != null) {
				for (File fileItem : list) {
					if (fileStatusMap.containsKey(fileItem.getAbsolutePath())) {
						logger.info("File already processed, go to other file");
					} else {
						logger.info("Path of fileItem {}", fileItem);
						fileStatusMap.put(fileItem.getAbsolutePath(), true);
						submitTask(fileItem, boundedExecutor, concurrentMapForOutput);
						exitCounterState = true;
					}
				}
			}
			if (!exitCounterState) {
				emptCounter++;
			}
			if (emptCounter == 2) {
				break;
			}
			Thread.sleep(5000); // sleep for 5s
		}
		logger.info("Awaiting termination of all threads");
		while (!boundedExecutor.isReadyToTerminate()) {
			logger.info("Awaiting termination of all threads");
			Thread.sleep(TERMINATION_POLL_PERIOD);
		}
		logger.info("printing final state of concurrentHashMap");
		printMapState(concurrentMapForOutput);

		boundedExecutor.shutdown();
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}

	private void printMapState(ConcurrentHashMap<Long, List<LogFileOutput>> concurrentMapForOutput) {
		logger.info(
				"============================================================================================================");
		logger.info("================================FINAL OUTPUT============================================");
		TreeMap<Long, List<LogFileOutput>> sortedMap = new TreeMap<>(concurrentMapForOutput);
		for (Map.Entry<Long, List<LogFileOutput>> entry : sortedMap.entrySet()) {
			String dateString = getTSinString(entry.getKey());
			List<LogFileOutput> errorList = entry.getValue();
			// segregate errors based on exception type
			Map<String, Integer> exceptionTypeMap = new HashMap<>();
			for (LogFileOutput out : errorList) {
				if (exceptionTypeMap.containsKey(out.getException())) {
					exceptionTypeMap.put(out.getException(), exceptionTypeMap.get(out.getException()) + 1);
				} else {
					exceptionTypeMap.put(out.getException(), 1);
				}
			}
			for (Map.Entry<String, Integer> entryInner : exceptionTypeMap.entrySet()) {
				logger.info("{}   {}   {}", dateString, entryInner.getKey(), entryInner.getValue());
			}
		}
	}

	private String getTSinString(Long timestamp) {
		timestamp = timestamp * 1000l;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		//Here you say to java the initial timezone. This is the secret
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		//Here you set to your timezone
		sdf.setTimeZone(TimeZone.getDefault());
		//Will print on your default Timezone
		return sdf.format(timestamp);
	}

	private List<LogFileInput> convertFileFormatToList(File file) {
		List<LogFileInput> logFileList = new ArrayList<>();
		try {
			logger.info("File path {}", file.getAbsolutePath());
			List<String> allLines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
			logger.info("All lines size {}", allLines.size());
			for (String line : allLines) {
				logger.info(line);
				String[] strings = line.split(",");
				LogFileInput input = new LogFileInput(strings[0], Long.valueOf(strings[1]), strings[2]);
				logFileList.add(input);
			}
		} catch (Exception e) {
			logger.error("error {}", e.getMessage());
		}
		return logFileList;
	}

	private void submitTask(final File file, final BoundedExecutor boundedExecutor,
			ConcurrentHashMap<Long, List<LogFileOutput>> concurrentMapForOutput) {
		try {
			boundedExecutor.submitTask(new Runnable() {

				@Override
				public void run() {
					try {
						doTask(file, concurrentMapForOutput);
					} catch (InterruptedException e) {
						logger.error("Unexpected InterruptedException while submitting task {}", file.getAbsolutePath(),
								e);
						errorCountTotal.incrementAndGet();
						Thread.currentThread().interrupt();
					}
				}
			});
		} catch (final Exception e) {
			logger.error("Unexpected exception while submitting task {}", e);
			errorCountTotal.incrementAndGet();
		}

	}

	private void doTask(final File file, ConcurrentHashMap<Long, List<LogFileOutput>> concurrentMapForOutput)
			throws InterruptedException {
		logger.info("Thread {} ", Thread.currentThread());
		List<LogFileInput> logFileList = convertFileFormatToList(file);
		for (LogFileInput input : logFileList) {
			long fifteenMinOffset = input.getTimestamp() / 1000;
			fifteenMinOffset = fifteenMinOffset - fifteenMinOffset % 900;
			if (concurrentMapForOutput.containsKey(fifteenMinOffset)) {
				List<LogFileOutput> outRecords = concurrentMapForOutput.get(fifteenMinOffset);
				outRecords.add(new LogFileOutput(input.getTimestamp(), input.getException()));
				concurrentMapForOutput.put(fifteenMinOffset, outRecords);
			} else {
				List<LogFileOutput> outRecords = new ArrayList<>();
				outRecords.add(new LogFileOutput(input.getTimestamp(), input.getException()));
				concurrentMapForOutput.put(fifteenMinOffset, outRecords);
			}
		}
	}

}
