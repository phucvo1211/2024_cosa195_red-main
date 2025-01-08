package DataObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Deck implements Serializable {
    Card currentCard;
    ArrayList<Card> discardedCards;
    ArrayList<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        this.discardedCards = new ArrayList<>();
        
        //Shuffle in red cards
        addInSuit('r');
        addInSuit('g');
        addInSuit('b');
        addInSuit('y');
        addInWildcards();
        shuffleDeck();

        //Move first card to discard pile
        Card firstCard = cards.removeLast();
        while (firstCard.getValue() >= 14) {
            cards.addFirst(firstCard);
            firstCard = cards.removeLast();
        }

        discardedCards.add(firstCard);
        currentCard = discardedCards.getLast();

    }

    private void addInSuit(char suit) {
        for (int i = 1; i <= 2; i++) {

            for (int j = 1; j <= 9; j++) {
                cards.add(new Card(suit, j));
            }

            for (int j = 11; j <= 13; j++) {
                cards.add(new Card(suit, j));
            }
        }
        cards.add(new Card(suit, 10));
    }

    private void addInWildcards() {
        for (int i = 1; i <= 4; i++) {
            cards.add(new Card('w', 14));
        }

        //shuffle in 4 draw fours
        for (int i = 1; i <= 4; i++) {
            cards.add(new Card('w', 15));
        }
    }

    public Deck(Card currentCard, ArrayList<Card> discardedCards, ArrayList<Card> cards) {
        this.currentCard = currentCard;
        this.discardedCards = discardedCards;
        this.cards = cards;
    }

    public void shuffleDeck() {
        if(cards ==null || cards.isEmpty()){
            return;
        }
        Collections.shuffle(cards);
    }

    public Card drawCard(){
        if(cards==null || cards.isEmpty()) return null;

        Card drawnCard = cards.getLast();
        cards.removeLast();
        
        if (cards.isEmpty()) {
            Card tempCard = discardedCards.removeLast();
            cards.clear();
            for (Card card: discardedCards) {
                cards.add(new Card(card.getSuit(), card.getValue()));
            }
            Collections.shuffle(cards);
            discardedCards.clear();
            discardedCards.add(tempCard);
        }
        return drawnCard;
    }

    public void currentDiscardCard(Card card){
        discardedCards.add(card);
    }

    @JsonIgnore
    public char getCurrentSuit(){
        return getCurrentCard().getSuit();
    }

    @JsonIgnore
    public int getCurrentValue() {
        return getCurrentCard().getValue();
    }

    @JsonIgnore
    public Card getCurrentCard(){
        return discardedCards.getLast();
    }

    public ArrayList<Card> getDiscardedCards() {
        return discardedCards;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

}