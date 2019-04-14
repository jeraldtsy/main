package seedu.address.model.quiz;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a quiz that stores a list of QuizCard
 */
public class Quiz {

    public static final String MESSAGE_CONSTRAINTS = "QuizMode must only be learn/review/preview/difficult";

    private List<QuizCard> originalQuizCardList;
    private List<QuizCard> generatedQuizCardList;
    private QuizMode mode;
    private QuizCard currentQuizCard;
    private int currentCardIndex;
    private int generatedCardSize;
    private boolean isQuizDone;
    private int quizTotalAttempts;
    private int quizTotalCorrectQuestions;

    /**
     * Builds constructor from originalQuizCardList which is generated by {@code Session}
     * @param originalQuizCardList contains a list of question, answer and list of optional
     */
    public Quiz(List<QuizCard> originalQuizCardList, QuizMode mode) {
        requireNonNull(originalQuizCardList);
        checkArgument(mode != null, MESSAGE_CONSTRAINTS);

        this.originalQuizCardList = originalQuizCardList;
        this.mode = mode;
        this.currentCardIndex = -1;
        this.generatedCardSize = -1;
        this.isQuizDone = false;
        this.quizTotalAttempts = 0;
        this.quizTotalCorrectQuestions = 0;

        generate();
    }

    /**
     * Generates a list of cards based on the chosen cards given by session.
     */
    private void generate() {
        generatedQuizCardList = new ArrayList<>();

        switch (mode) {
        case PREVIEW:
            generatePreview();
            break;
        case LEARN:
            // Learn is a combination of Preview + Review
            generatePreview();
            generateReview();
            break;
        case REVIEW:
            generateReview();
            break;
        case DIFFICULT:
            generatePreview();
            break;
        default:
            break;
        }

        generatedCardSize = generatedQuizCardList.size();
    }

    /**
     * Generates a list of card with the mode Review
     */
    private void generateReview() {
        QuizCard currentCard;
        for (int i = 0; i < originalQuizCardList.size(); i++) {
            currentCard = originalQuizCardList.get(i);
            generatedQuizCardList.add(currentCard.generateOrderedQuizCardWithIndex(i, QuizMode.REVIEW));
        }

        for (int i = 0; i < originalQuizCardList.size(); i++) {
            currentCard = originalQuizCardList.get(i);
            generatedQuizCardList.add(currentCard.generateFlippedQuizCardWithIndex(i));
        }
    }

    /**
     * Generates a list of card with the mode Preview see but don't need to answer.
     */
    private void generatePreview() {
        QuizCard currentCard;

        for (int i = 0; i < originalQuizCardList.size(); i++) {
            currentCard = originalQuizCardList.get(i);
            generatedQuizCardList.add(currentCard.generateOrderedQuizCardWithIndex(i, QuizMode.PREVIEW));
        }
    }

    public int getQuizTotalAttempts() {
        return quizTotalAttempts;
    }

    public int getQuizTotalCorrectQuestions() {
        return quizTotalCorrectQuestions;
    }

    public String getCurrentProgress() {
        return (currentCardIndex + 1) + "/" + generatedCardSize;
    }

    public QuizCard getCurrentQuizCard() {
        requireNonNull(currentQuizCard);
        return currentQuizCard;
    }

    public List<String> getOpt() {
        return currentQuizCard.getOpt();
    }

    public List<QuizCard> getOriginalQuizCardList() {
        return originalQuizCardList;
    }

    public void setQuizDone() {
        isQuizDone = true;
    }

    public boolean isQuizDone() {
        return isQuizDone;
    }

    public List<QuizCard> getGeneratedQuizCardList() {
        return generatedQuizCardList;
    }

    /**
     * Returns true if there is card left in quiz.
     */
    public boolean hasCardLeft() {
        return currentCardIndex < (generatedCardSize - 1);
    }

    /**
     * Returns the next card in line.
     */
    public QuizCard getNextCard() {
        currentCardIndex++;

        if (currentCardIndex < generatedCardSize) {
            currentQuizCard = generatedQuizCardList.get(currentCardIndex);
            return currentQuizCard;
        }

        throw new IndexOutOfBoundsException("No cards left.");
    }

    /**
     * Updates the totalAttempts and streak of a specified card in the current session
     * and current quiz session totalAttempts and totalCorrectQuestions
     * @param index of the card
     * @param answer user input
     */
    public boolean updateTotalAttemptsAndStreak(int index, String answer) {
        QuizCard sessionCard = originalQuizCardList.get(index);
        boolean isCorrect = currentQuizCard.isCorrect(answer);
        sessionCard.updateTotalAttemptsAndStreak(isCorrect);

        if (isCorrect) {
            quizTotalCorrectQuestions++;
        }

        quizTotalAttempts++;

        return isCorrect;
    }

    /**
     * Toggles between if the card labeled difficult.
     * @param index of the card
     * @return result after toggling
     */
    public boolean toggleIsCardDifficult(int index) {
        QuizCard sessionCard = originalQuizCardList.get(index);
        sessionCard.toggleIsCardDifficult();

        return sessionCard.isCardDifficult();
    }

    /**
     * Format data needed by Session
     * @return a list of index of card, total attempts, streak and isDifficult in this session
     *         which contains streak higher than 0.
     */
    public List<List<Integer>> end() {
        List<List<Integer>> session = new ArrayList<>();
        QuizCard card;
        for (int i = 0; i < originalQuizCardList.size(); i++) {
            card = originalQuizCardList.get(i);
            int isCardDifficult = card.isCardDifficult() ? 1 : 0;

            if (card.getStreak() != 0) {
                session.add(Arrays.asList(i, card.getTotalAttempts(), card.getStreak(), isCardDifficult));
            }
        }

        return session;
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof Quiz)) {
            return false;
        }

        // state check
        Quiz other = (Quiz) obj;
        return other.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalQuizCardList, generatedQuizCardList, mode,
            currentQuizCard, currentCardIndex, isQuizDone);
    }

}
