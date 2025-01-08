package DataObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public class Card implements Serializable {

    public static Map<Character,String> suitMap = Map.ofEntries(
            entry('r',"Red"),
            entry('g',"Green"),
            entry('y',"Yellow"),
            entry('b',"Blue"));

    public static Map<Integer ,String> valueMap = Map.ofEntries(
            entry(1,"One"),
            entry(2,"Two"),
            entry(3,"Three"),
            entry(4,"Four"),
            entry(5,"Five"),
            entry(6,"Six"),
            entry(7,"Seven"),
            entry(8,"Eight"),
            entry(9,"Nine"),
            entry(10,"Zero"),
            entry(11,"Draw Two"),
            entry(12,"Skip Turn"),
            entry(13,"Reverse"),
            entry(14,"Wild Card"),
            entry(15,"Draw Four"));
    char suit; // could be any colour
    int value; // card with value of 0 will be 10, 11 = +2, 12 = skip, 13 = reverse

    @JsonCreator
    public Card(@JsonProperty("suit") char suit, @JsonProperty("value") int value) {
        this.suit = suit;
        this.value = value;
    }

    public char getSuit() {
        return suit;
    }
    public int getValue() {
        return value;
    }
    public int getPoints() {
        if (value <= 9) {
            return value; // Number cards (0-9)
        } else if (value <= 12) {
            return 20; // Skip, Reverse, and Draw 2 cards
        } else if (value == 13) {
            return 50; // Wild card
        } else if (value == 14) {
            return 50; // Wild Draw 4 card
        }
        return 0; // Default case (shouldn't happen)
    }
    public void setSuit(char suit) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && value == card.value;

    }

    @Override
    public String toString() {
        return Card.suitMap.get(this.suit) + " " + Card.valueMap.get(this.value) + " Card";
    }
}