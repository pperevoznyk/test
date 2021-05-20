package intiv.test.service;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import intiv.test.model.Sentence;
import intiv.test.util.SentenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static intiv.test.util.SentenceUtils.PAGE_INDEX;

@Service
@Slf4j
public class TsvReaderWriterService {

	public List<String[]> readTsvFile(File file) {
		TsvParserSettings settings = new TsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		TsvParser parser = new TsvParser(settings);

		return parser.parseAll(file);
	}

	public void writeSentencesToFile(List<Sentence> sentences, String[] headers, String oldFileName) throws IOException {
		Path outputPath = getOutputPath();
		String newFileName = outputPath.toAbsolutePath() + "/" + getNewFileName(oldFileName);
		TsvWriter writer = new TsvWriter(new FileWriter(new File(newFileName)), new TsvWriterSettings());
		writer.writeHeaders(headers);
		writer.writeRowsAndClose(SentenceUtils.getRows(sentences));
	}

	public List<Sentence> getSentences(List<String[]> fileLines) {
		List<Sentence> result = new ArrayList<>();
		Queue<String[]> stringSentences = new LinkedList<>(fileLines.subList(1, fileLines.size()));

		while (!stringSentences.isEmpty()) {
			String[] stringSentence = stringSentences.peek();
			int maxLineOnPage = SentenceUtils.getMaxLineOnPage(stringSentences, stringSentence[PAGE_INDEX]);
			Sentence newSentence = SentenceUtils.getSentence(stringSentence, maxLineOnPage);
			result.add(newSentence);
			stringSentences.poll();
		}

		return result;
	}

	private Path getOutputPath() throws IOException {
		Path outputPath = Paths.get("output");
		if (Files.notExists(outputPath)) {
			Files.createDirectory(outputPath);
		}
		return outputPath;
	}

	private String getNewFileName(String oldFileName){
		return oldFileName.substring(0, oldFileName.length() - 4) + "_processed.tsv";
	}
}
