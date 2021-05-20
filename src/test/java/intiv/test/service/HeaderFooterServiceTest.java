package intiv.test.service;

import intiv.test.model.Sentence;
import intiv.test.util.SentenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.*;

import static intiv.test.util.SentenceUtils.CONTENT_INDEX;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class HeaderFooterServiceTest {

	@Autowired
	private TsvReaderWriterService tsvReaderService;
	@Autowired
	private HeaderFooterService headerFooterService;

	@Test
	public void shouldBeSameMarkingAgainstExampleFiles() {

		List<File> files = new ArrayList<>();
		files.add(new File("src/test/resources/sds6979071286215649937.tsv"));
		files.add(new File("src/test/resources/sds6470802517340212903.tsv"));
		files.add(new File("src/test/resources/sds2912057926858607250.tsv"));
		files.add(new File("src/test/resources/sds6548799556800349124.tsv"));

		for (File file : files) {
			List<String[]> lines = tsvReaderService.readTsvFile(file);
			List<Sentence> sentences = tsvReaderService.getSentences(lines);
			headerFooterService.markSentences(sentences);

			Queue<String[]> lineQueue = new LinkedList<>(lines.subList(1, lines.size()));
			Queue<Sentence> sentenceQueue = new LinkedList<>(sentences);

			assertEquals(lineQueue.size(), sentenceQueue.size());

			while (!lineQueue.isEmpty()) {
				String[] line = lineQueue.poll();
				Sentence sentence = sentenceQueue.poll();
				assertArrayEquals(line, SentenceUtils.getRow(sentence));
			}
		}
	}

	@Test
	public void shouldBeDifferentMarkingAgainstExampleFile() {

		int failLineNumber1 = 602;
		int failLineNumber2 = 695;

		List<String[]> lines = tsvReaderService.readTsvFile(new File("src/test/resources/sds5493674484985787039.tsv"));
		List<Sentence> sentences = tsvReaderService.getSentences(lines);
		headerFooterService.markSentences(sentences);

		Queue<String[]> lineQueue = new LinkedList<>(lines.subList(1, lines.size()));
		Queue<Sentence> sentenceQueue = new LinkedList<>(sentences);

		assertEquals(lineQueue.size(), sentenceQueue.size());

		int notEqualsCounter = 0;

		while (!lineQueue.isEmpty()) {
			String[] line = lineQueue.poll();
			Sentence sentence = sentenceQueue.poll();
			if (!Arrays.equals(line, SentenceUtils.getRow(sentence))) {
				String[] failString1 = lines.get(failLineNumber1 - 1);
				String[] failString2 = lines.get(failLineNumber2 - 1);

				assertTrue(failString1[CONTENT_INDEX].equals(sentence.getContent()) ||
						failString2[CONTENT_INDEX].equals(sentence.getContent()));

				notEqualsCounter++;
			}
		}

		assertEquals(2, notEqualsCounter);
	}
}