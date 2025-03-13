package org.elasticsearch.index.analysis;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import static sun.security.x509.OIDMap.addAttribute;

/**
 * This filter generates Greeklish tokens from Greek tokens.
 * Greeklish tokens have the same position and offset as the original Greek tokens,
 * and their type will be {@code greeklish_word}.
 */
public class GreeklishTokenFilter extends TokenFilter {
	private static final Logger logger = LoggerFactory.getLogger(GreeklishTokenFilter.class);

	public static final String TOKEN_TYPE = "greeklish_word";

	private Stack<char[]> greeklishWords = new Stack<>();
	private AttributeSource.State current;

	private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

	private final GreeklishConverter greeklishConverter;

	// Constructor
	public GreeklishTokenFilter(TokenStream tokenStream, int maxExpansions, boolean generateGreekVariants) {
		super(tokenStream);
		this.greeklishConverter = new GreeklishConverter(maxExpansions, generateGreekVariants);
	}

	@Override
	public boolean incrementToken() throws IOException {
		// If we have Greeklish tokens in the stack, use them
		if (!greeklishWords.isEmpty()) {
			char[] greeklishWord = greeklishWords.pop();
			restoreState(current);
			termAttribute.copyBuffer(greeklishWord, 0, greeklishWord.length);
			termAttribute.setLength(greeklishWord.length);
			posIncAttribute.setPositionIncrement(0);
			typeAttribute.setType(TOKEN_TYPE);
			return true;
		}

		// Otherwise, continue with the token stream
		if (!input.incrementToken()) {
			return false;
		}

		// Check if the token can generate Greeklish and capture the current state
		if (addWordsToStack()) {
			current = captureState();
		}

		return true;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
	}

	/**
	 * This method checks if a token can be converted to Greeklish.
	 * If yes, it adds the Greeklish tokens to the stack.
	 */
	private boolean addWordsToStack() throws IOException {
		List<StringBuilder> greeklishTokens = greeklishConverter.convert(termAttribute.buffer(), termAttribute.length());
		if (greeklishTokens == null || greeklishTokens.isEmpty()) {
			return false;
		}

		for (StringBuilder word : greeklishTokens) {
			greeklishWords.push(word.toString().toCharArray());
		}

		return true;
	}
}
