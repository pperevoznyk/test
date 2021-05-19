package com.test.demo.service;

import com.test.demo.model.Sentence;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TsvReaderService {

	private final int CONTENT_INDEX = 0;
	private final int PAGE_INDEX = 2;
	private final int LINE_INDEX = 3;
	private final int X1_INDEX = 4;
	private final int X2_INDEX = 5;
	private final int Y_INDEX = 6;
	private final int PADDING_INDEX = 7;
	private final int FONT_INDEX = 8;

	@Value("${config.max-header-lines}")
	private int maxHeaderLines;

	@Value("${config.max-footer-lines}")
	private int maxFooterLines;

	@Value("${config.header-footer-occurrences-threshold}")
	private double headerFooterThreshold;

	@Value("${config.levenshtein-threshold}")
	private double levenshteinThreshold;

	@Value("${config.exceptions}")
	private Set<String> exceptions;

	public void processFile(String filePath) throws IOException {
		List<String[]> fileLines = readTsvFile(new File(filePath));
		List<Sentence> sentences = getSentences(fileLines);
		List<Sentence> sentencesToCheck = sentences.stream()
				.filter(
						sentence -> !exceptions.contains(sentence.getContent().toLowerCase()) &&
								(sentence.getLine() < maxHeaderLines || sentence.getLine() > sentence.getMaxLine() - maxFooterLines)
				).collect(Collectors.toList());

		int maxPage = getMaxPage(sentencesToCheck);

		long startTime = System.currentTimeMillis();
		for (Sentence sentence : sentencesToCheck) {

			if (sentence.isHeaderFooter()) {
				continue;
			}

			List<Sentence> occurrences = sentencesToCheck.stream()
					.filter(s -> checkConditions(sentence, s))
					.collect(Collectors.toList());

			if (occurrences.size() > maxPage * headerFooterThreshold) {
				occurrences.forEach(s -> s.setHeaderFooter(true));
			}
		}
		long timePoint1 = System.currentTimeMillis();
		System.out.println(timePoint1 - startTime);


//		sentences.stream().filter(Sentence::isHeaderFooter).forEach(line -> log.info("{}, {}, {}", line.getContent(), line.getPage(), line.getLine()));

		writeResultToFile(sentences, fileLines.get(0), filePath);
	}

	private boolean checkConditions(Sentence s1, Sentence s2) {

		String s1content = s1.getContent().toLowerCase();
		String s2content = s2.getContent().toLowerCase();

		int levenshteinDistance = new LevenshteinDistance().apply(s1content, s2content);

//		log.info("sentence1={}", s1content);

		if (levenshteinDistance > levenshteinThreshold) {
			return false;
		}

		if (s1.getLine() == s2.getLine()) {
			return true;
		}

		int linesFromBottomS1 = s1.getMaxLine() - s1.getLine();
		int linesFromBottomS2 = s2.getMaxLine() - s2.getLine();

		return linesFromBottomS1 == linesFromBottomS2;
	}

	private List<String[]> readTsvFile(File file) {
		TsvParserSettings settings = new TsvParserSettings();
		settings.getFormat().setLineSeparator("\n");

		TsvParser parser = new TsvParser(settings);

		return parser.parseAll(file);
	}

	private void writeResultToFile(List<Sentence> sentences, String[] headers, String oldFileName) throws IOException {
		String newFileName = oldFileName.substring(0, oldFileName.length() - 4) + "_processed.tsv";

		TsvWriter writer = new TsvWriter(new FileWriter(new File(newFileName)), new TsvWriterSettings());
		writer.writeHeaders(headers);
		writer.writeRowsAndClose(getRows(sentences));
	}

	private List<Sentence> getSentences(List<String[]> fileLines) {
		List<Sentence> result = new ArrayList<>();
		List<String[]> stringSentences = fileLines.subList(1, fileLines.size());

		for (String[] stringSentence : stringSentences) {
			int maxLineOnPage = stringSentences.stream().filter(s -> s[PAGE_INDEX].equals(stringSentence[PAGE_INDEX]))
					.map(s -> Integer.parseInt(s[LINE_INDEX]))
					.max(Integer::compareTo)
					.get();

			Sentence newSentence = new Sentence(
					stringSentence[CONTENT_INDEX],
					Integer.parseInt(stringSentence[PAGE_INDEX]),
					Integer.parseInt(stringSentence[LINE_INDEX]),
					maxLineOnPage,
					stringSentence[X1_INDEX],
					stringSentence[X2_INDEX],
					stringSentence[Y_INDEX],
					stringSentence[PADDING_INDEX],
					stringSentence[FONT_INDEX],
					false
			);

			result.add(newSentence);
		}

		return result;
	}

	private String[][] getRows(List<Sentence> sentences) {
		String[][] result = new String[sentences.size()][];

		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			result[i] = new String[]{
					sentence.getContent(),
					Boolean.toString(sentence.isHeaderFooter()),
					Integer.toString(sentence.getPage()),
					Integer.toString(sentence.getLine()),
					sentence.getX1(),
					sentence.getX2(),
					sentence.getY(),
					sentence.getPadding(),
					sentence.getFont()
			};
		}

		return result;
	}

	private int getMaxPage(List<Sentence> sentences) {
		return sentences.stream().max(Comparator.comparingInt(Sentence::getPage)).get().getPage();
	}
}
