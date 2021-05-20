package intiv.test.service;

import intiv.test.model.Sentence;
import intiv.test.util.SentenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HeaderFooterService {

	@Value("${config.max-header-lines:10}")
	private int maxHeaderLines;
	@Value("${config.max-footer-lines:10}")
	private int maxFooterLines;
	@Value("${config.header-footer-occurrences-threshold:0.8}")
	private double headerFooterThreshold;
	@Value("${config.levenshtein-threshold:2}")
	private double levenshteinThreshold;
	@Value("${config.exceptions:_horizontal_line}")
	private Set<String> exceptions;

	public void markSentences(List<Sentence> sentences) {

		//filter out lines other than in maxHeaderLines and maxFooterLines range for performance gain
		List<Sentence> sentencesToCheck = sentences.stream()
				.filter(
						sentence -> !exceptions.contains(sentence.getContent().toLowerCase()) &&
								(sentence.getLine() < maxHeaderLines || sentence.getLine() > sentence.getMaxLine() - maxFooterLines)
				).collect(Collectors.toList());

		mark(sentencesToCheck);
	}

	private void mark(List<Sentence> sentences) {
		final int maxPage = SentenceUtils.getMaxPage(sentences);
		Queue<Sentence> sentenceQueue = new LinkedList<>(sentences);

		while (!sentenceQueue.isEmpty()) {
			Sentence sentence = sentenceQueue.peek();

			if (sentence.isHeaderFooter()) {
				sentenceQueue.poll();
				continue;
			}

			List<Sentence> occurrences = sentenceQueue.stream()
					.filter(s -> checkConditions(sentence, s))
					.collect(Collectors.toList());

			if (occurrences.size() > maxPage * headerFooterThreshold) {
				occurrences.forEach(s -> s.setHeaderFooter(true));
			}
			sentenceQueue.poll();
		}
	}

	private boolean checkConditions(Sentence s1, Sentence s2) {

		String s1content = s1.getContent().toLowerCase();
		String s2content = s2.getContent().toLowerCase();

		int levenshteinDistance = new LevenshteinDistance().apply(s1content, s2content);

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
}
