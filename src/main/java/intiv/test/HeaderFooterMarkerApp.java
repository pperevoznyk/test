package intiv.test;

import intiv.test.model.Sentence;
import intiv.test.service.HeaderFooterService;
import intiv.test.service.TsvReaderWriterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class HeaderFooterMarkerApp {

	private final TsvReaderWriterService tsvReaderService;
	private final HeaderFooterService headerFooterService;

	public static void main(String[] args) {
		SpringApplication.run(HeaderFooterMarkerApp.class, args);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			for (File file : getFiles(new File("."))) {
				long startTime = System.currentTimeMillis();
				List<String[]> lines = tsvReaderService.readTsvFile(file);
				List<Sentence> sentences = tsvReaderService.getSentences(lines);
				headerFooterService.markSentences(sentences);

				long endTime = System.currentTimeMillis();
				log.info("File {} processing time: {} ms", file.getName(), endTime - startTime);

				tsvReaderService.writeSentencesToFile(sentences, lines.get(0), file.getName());
			}
		} catch (IOException e) {
			log.error("IOError", e);
		}
	}

	private List<File> getFiles(final File folder) {
		List<File> files = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile() && fileEntry.getName().endsWith(".tsv")) {
				files.add(fileEntry);
			}
		}
		return files;
	}
}
