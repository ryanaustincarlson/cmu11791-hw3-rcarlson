package edu.cmu11791.rcarlson;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.Token;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * Tokenize and extract 1-, 2-, and 3-grams from the question and each answer. Add those NGrams as
 * instance vars for each question and answer.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class NGramAnnotator extends JCasAnnotator_ImplBase {

  private TokenizerFactory<Word> tokenizerFactory = PTBTokenizerFactory.newTokenizerFactory();

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    Iterator<Annotation> questionIter = jcas.getAnnotationIndex(Question.type).iterator();
    while (questionIter.hasNext()) {
      Question question = (Question) questionIter.next();
      Token[] tokens = tokenize(jcas, question);
      question.setNgrams(getNGramFSArray(jcas, tokens));
    }

    Iterator<Annotation> answerIter = jcas.getAnnotationIndex(Answer.type).iterator();
    while (answerIter.hasNext()) {
      Answer answer = (Answer) answerIter.next();
      Token[] tokens = tokenize(jcas, answer);
      answer.setNgrams(getNGramFSArray(jcas, tokens));
    }
  }

  private Token[] tokenize(JCas jcas, Annotation annotation) {
    String text = annotation.getCoveredText();
    Tokenizer<Word> tokenizer = tokenizerFactory.getTokenizer(new StringReader(text));

    List<Token> tokens = new ArrayList<Token>();
    for (Word word : tokenizer.tokenize()) {
      Token token = new Token(jcas);
      token.setBegin(annotation.getBegin() + word.beginPosition());
      token.setEnd(annotation.getBegin() + word.endPosition());
      tokens.add(token);
    }
    return tokens.toArray(new Token[tokens.size()]);
  }

  private FSArray getNGramFSArray(JCas jcas, Token[] tokens) {
    NGram[] ngrams = new NGram[3];
    ngrams[0] = getUnigrams(jcas, tokens);
    ngrams[1] = getBigrams(jcas, tokens);
    ngrams[2] = getTrigrams(jcas, tokens);

    FSArray ngramArray = new FSArray(jcas, ngrams.length);
    ngramArray.copyFromArray(ngrams, 0, 0, ngrams.length);
    return ngramArray;
  }

  private NGram getUnigrams(JCas jcas, Token[] tokens) {
    return getNgram(jcas, tokens, 1, "unigram");
  }

  private NGram getBigrams(JCas jcas, Token[] tokens) {
    return getNgram(jcas, tokens, 2, "bigram");
  }

  private NGram getTrigrams(JCas jcas, Token[] tokens) {
    return getNgram(jcas, tokens, 3, "trigram");
  }

  private NGram getNgram(JCas jcas, Token[] tokens, int n, String elementType) {
    NGram ngrams = new NGram(jcas);
    ngrams.setElementType(elementType);
    ngrams.setBegin(tokens[0].getBegin());
    ngrams.setEnd(tokens[tokens.length - 1].getEnd());

    Token[] ngramTokens = new Token[tokens.length - n + 1];
    for (int tokenIndex = 0; tokenIndex < tokens.length - n + 1; tokenIndex++) {
      Token token = tokens[tokenIndex];
      Token nextToken = tokens[tokenIndex + n - 1];

      Token ngramToken = new Token(jcas);
      ngramToken.setConfidence(1);
      ngramToken.setBegin(token.getBegin());
      ngramToken.setEnd(nextToken.getEnd());
      ngramTokens[tokenIndex] = ngramToken;
    }

    FSArray ngramArray = new FSArray(jcas, ngramTokens.length);
    ngramArray.copyFromArray(ngramTokens, 0, 0, ngramTokens.length);
    ngrams.setElements(ngramArray);

    return ngrams;
  }
}
