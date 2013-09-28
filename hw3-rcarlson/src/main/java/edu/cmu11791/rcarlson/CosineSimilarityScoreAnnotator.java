package edu.cmu11791.rcarlson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

/**
 * Calculates the Cosine Similarity between a question and every potential answer.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class CosineSimilarityScoreAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    Map<String, Integer> featuresToIndicesMap = new HashMap<String, Integer>();
    int numFeatures = mapFeaturesToIndices(jcas, featuresToIndicesMap); // side effects

    int[] questionFeatureVector = getQuestionFeatureVector(jcas, numFeatures, featuresToIndicesMap);

    Iterator<Annotation> answerIter = jcas.getAnnotationIndex(Answer.type).iterator();
    while (answerIter.hasNext()) {
      Answer answer = (Answer) answerIter.next();
      int[] answerFeatureVector = getAnswerFeatureVector(answer, numFeatures, featuresToIndicesMap);
      double cosineSimilarity = getCosineSimilarity(questionFeatureVector, answerFeatureVector);

      AnswerScore answerScore = new AnswerScore(jcas);
      answerScore.setBegin(answer.getBegin());
      answerScore.setEnd(answer.getEnd());
      answerScore.setAnswer(answer);
      answerScore.setScore(cosineSimilarity);
      answerScore.setConfidence(0.75);
      answerScore.setCasProcessorId(CosineSimilarityScoreAnnotator.class.getSimpleName());
      answerScore.addToIndexes();
    }
  }

  private double getCosineSimilarity(int[] a, int[] b) {
    // assume *a* and *b* have the same length
    int aSquaredSum = 0;
    int bSquaredSum = 0;
    int dotProduct = 0;
    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      aSquaredSum += a[i] * a[i];
      bSquaredSum += b[i] * b[i];
    }
    double cosineSim = dotProduct / (Math.sqrt(aSquaredSum) * Math.sqrt(bSquaredSum));
    return cosineSim;
  }

  private int[] getQuestionFeatureVector(JCas jcas, int numFeatures,
          Map<String, Integer> featuresToIndicesMap) {
    Question question = AbstractScoreAnnotator.getQuestion(jcas);
    int[] questionFeatureVector = new int[numFeatures]; // initialized to zeros
    FSArray questionNgrams = question.getNgrams();
    for (int ngramIndex = 0; ngramIndex < questionNgrams.size(); ngramIndex++) {
      NGram questionNgram = (NGram) questionNgrams.get(ngramIndex);
      FSArray ngramFeatures = questionNgram.getElements();
      for (int i = 0; i < ngramFeatures.size(); i++) {
        String text = ((Annotation) ngramFeatures.get(i)).getCoveredText();
        questionFeatureVector[featuresToIndicesMap.get(text)]++;
      }
    }

    return questionFeatureVector;
  }

  private int[] getAnswerFeatureVector(Answer answer, int numFeatures,
          Map<String, Integer> featuresToIndicesMap) {
    int[] answerFeatureVector = new int[numFeatures]; // initialized to zeros
    FSArray answerNgrams = answer.getNgrams();
    for (int ngramIndex = 0; ngramIndex < answerNgrams.size(); ngramIndex++) {
      NGram answerNgram = (NGram) answerNgrams.get(ngramIndex);
      FSArray ngramFeatures = answerNgram.getElements();
      for (int i = 0; i < ngramFeatures.size(); i++) {
        String text = ((Annotation) ngramFeatures.get(i)).getCoveredText();
        answerFeatureVector[featuresToIndicesMap.get(text)]++;
      }
    }

    return answerFeatureVector;
  }

  private int mapFeaturesToIndices(JCas jcas, Map<String, Integer> vocabToIndexMap) {
    int vocabCount = 0;

    Question question = AbstractScoreAnnotator.getQuestion(jcas);
    FSArray questionNgrams = question.getNgrams();
    for (int ngramIndex = 0; ngramIndex < questionNgrams.size(); ngramIndex++) {
      FSArray ngramFeatures = ((NGram) questionNgrams.get(ngramIndex)).getElements();
      for (int i = 0; i < ngramFeatures.size(); i++) {
        Annotation annotation = (Annotation) ngramFeatures.get(i);
        String text = annotation.getCoveredText();
        // have we seen this text before? if not, add it to the vocab->index map
        if (!vocabToIndexMap.containsKey(text)) {
          vocabToIndexMap.put(text, vocabCount);
          vocabCount++;
        }
      }
    }

    Iterator<Annotation> answerIter = jcas.getAnnotationIndex(Answer.type).iterator();
    while (answerIter.hasNext()) {
      Answer answer = (Answer) answerIter.next();
      FSArray answerNGrams = answer.getNgrams();
      for (int ngramIndex = 0; ngramIndex < answerNGrams.size(); ngramIndex++) {
        FSArray ngramFeatures = ((NGram) answerNGrams.get(ngramIndex)).getElements();
        for (int i = 0; i < ngramFeatures.size(); i++) {
          Annotation annotation = (Annotation) ngramFeatures.get(i);
          String text = annotation.getCoveredText();
          // have seen this before? if not, add it to the vocab->index map
          if (!vocabToIndexMap.containsKey(text)) {
            vocabToIndexMap.put(text, vocabCount);
            vocabCount++;
          }
        }
      }
    }
    return vocabCount;
  }
}
