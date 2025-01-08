package DataObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {

    private int playerID;
    private String playerUsername;
    private ArrayList<Card> cards;
    private int roundsWon = 0;
    private int roundsLost = 0;
    private int pointsGained = 0;
    private int pointsLost = 0;
    private int currencyValue = 0;
    private AvatarImage avatar;
    private CardImage cardImage;
    private boolean endOfTurn;

    public Player(String playerUsername) {
        this.playerID = 0;
        this.playerUsername = playerUsername;
        this.cards = new ArrayList<>();

        //initialize statistics at 0
        this.roundsWon = 0;
        this.roundsLost = 0;
        this.pointsGained = 0;
        this.pointsLost = 0;
        this.currencyValue = 0;
        this.avatar = AvatarImage.PTSD;
        this.cardImage = CardImage.DEFAULT;
        this.endOfTurn = false;
    }

    @JsonCreator
    public Player(@JsonProperty("playerID") int playerID,
                  @JsonProperty("playerUsername") String playerUsername,
                  @JsonProperty("roundsWon") int roundsWon,
                  @JsonProperty("roundsLost") int roundsLost,
                  @JsonProperty("pointsGained") int pointsGained,
                  @JsonProperty("pointsLost") int pointsLost,
                  @JsonProperty("currencyValue") int currencyValue,
                  @JsonProperty("avatar") AvatarImage avatar,
                  @JsonProperty("cardImage") CardImage cardImage) {

        this.playerID = playerID;
        this.playerUsername = playerUsername;
        this.cards = new ArrayList<>();

        this.roundsWon = roundsWon;
        this.roundsLost = roundsLost;
        this.pointsGained = pointsGained;
        this.pointsLost = pointsLost;
        this.currencyValue = currencyValue;
        this.avatar = avatar;
        this.cardImage = cardImage;
        this.endOfTurn = false;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void removeCard(Card card) {

        if (card.getValue() >= 14) {
            Card tempCard = new Card('w', card.getValue());
            cards.remove(tempCard);
        }

        System.out.println(cards.remove(card));

    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setRoundsWon(int roundsWon) {
        this.roundsWon = roundsWon;
    }

    public void setRoundsLost(int roundsLost) {
        this.roundsLost = roundsLost;
    }

    public void incrementRoundsWon() {
        roundsWon++;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public void incrementRoundsLost() {
        roundsLost++;
    }

    public int getRoundsLost() {
        return roundsLost;
    }

    public void setPointsGained(int pointsGained) {
        this.pointsGained = pointsGained;
    }

    public int getPointsGained() {
        return pointsGained;
    }

    public int getPointsLost() {
        return pointsLost;
    }

    public void setPointsLost(int pointsLost) {
        this.pointsLost = pointsLost;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    @JsonIgnore
    public int getPlayerHandSize() {
        return cards.size();
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getCurrencyValue() {
        return currencyValue;
    }

    public void setCurrencyValue(int currencyValue) {
        this.currencyValue = currencyValue;
    }

    public AvatarImage getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarImage avatar) {
        this.avatar = avatar;
    }

    public CardImage getCardImage() {
        return cardImage;
    }

    public void setCardImage(CardImage cardImage) {
        this.cardImage = cardImage;
    }

    public boolean getEndOfTurn() {
        return endOfTurn;
    }

    public void setEndOfTurn(boolean endOfTurn) {
        this.endOfTurn = endOfTurn;
    }

    public int calculatepoints() {
        int totalpoints = 0;
        for (Card card : cards) {

            totalpoints += card.getPoints();
        }
        return totalpoints;
    }

    public int calculateHandPoints() {
        int totalpoints = 0;
        for (Card card : cards) {
            totalpoints += card.getPoints();
        }
        return totalpoints;
    }

    public boolean hasMatchingCard(Card currentCard) {
        for (Card card : cards) {
            if (card.getSuit() == currentCard.getSuit() || card.getValue() == currentCard.getValue()) {
                return true;
            }
        }
        return false;
    }

    public Card findMatchingCard(Card card) {
        for (Card c : cards) {
            if (c.getSuit() == card.getSuit() || c.getValue() == card.getValue()) {
                return c;
            }
        }
        return null;
    }

    //method to clear all the cards from the hand
    public void clearHand (){
        cards.clear();
    }

    //method to clear the points
    //this may be removed
    public void clearPoints() {
        pointsLost = 0;
    }
}
