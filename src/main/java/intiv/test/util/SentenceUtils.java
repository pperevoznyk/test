package intiv.test.util;

import intiv.test.model.Sentence;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SentenceUtils {
	public static final int CONTENT_INDEX = 0;
	public static final int PAGE_INDEX = 2;
	public static final int LINE_INDEX = 3;
	public static final int X1_INDEX = 4;
	public static final int X2_INDEX = 5;
	public static final int Y_INDEX = 6;
	public static final int PADDING_INDEX = 7;
	public static final int FONT_INDEX = 8;

	public static Sentence getSentence(String[] stringSentence, int maxLineOnPage) {
		return new Sentence(
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
	}

	public static int getMaxPage(Collection<Sentence> sentences) {
		return sentences.stream().max(Comparator.comparingInt(Sentence::getPage))
				.get().getPage();
	}

	public static int getMaxLineOnPage(Collection<String[]> sentences, String pageNumber) {
		return sentences.stream().filter(s -> s[PAGE_INDEX].equals(pageNumber))
				.map(s -> Integer.parseInt(s[LINE_INDEX]))
				.max(Integer::compareTo)
				.get();
	}

	public static String[][] getRows(List<Sentence> sentences) {
		String[][] result = new String[sentences.size()][];

		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			result[i] = getRow(sentence);
		}

		return result;
	}

	public static String[] getRow(Sentence sentence) {
		return new String[]{
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
}
