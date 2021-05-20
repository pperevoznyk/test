package intiv.test.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class Sentence {
	private final String content;
	private final int page;
	private final int line;
	private final int maxLine;
	private final String x1;
	private final String x2;
	private final String y;
	private final String padding;
	private final String font;

	private boolean isHeaderFooter;
}
