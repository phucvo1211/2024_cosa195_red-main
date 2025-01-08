package DataObject;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

//IF THIS CONTAINS DECK, PLAYERS (AND THEREFOR THEIR CARDS), DISCARD PILE. THEN IT CAN BE OUR GAME STATE OBJECT.
public class Game implements Serializable {

    private static final int STARTING_CARD_AMOUNT = 7;
    private static final long TURN_TIMER_IN_SECONDS = 30;
    private static final long GAME_TIMER_IN_MINUTES = 15;
    private Map<Player, Boolean> unoCalledPlayers;

    private ArrayList<Player> players;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean reversePlayOrder;
    private boolean isStarted;
    private boolean skipPlayed;
    Timer gameTimer = new Timer();
    Timer turnTimer;
    TimerTask gameTimerTask;
    TimerTask turnTimerTask;

//    TimerTask gameTimerTask = new TimerTask() {
//        public void run(
//        ) {
//            System.out.println("End game called at the TIMER");
//            cancelGame();
//        }
//    };
//
//    TimerTask turnTimerTask = new TimerTask() {
//        @Override
//        public void run() {
//            System.out.println("End turn called at the TIMER");
//            endTurn();
//        }
//    };

    public Game() {
        players = new ArrayList<>();
        deck = new Deck();
        currentPlayerIndex = 0;
        reversePlayOrder = false;
        isStarted = false;
        skipPlayed = false;
        unoCalledPlayers = new ConcurrentHashMap<>();
        //maxTurnDuration = 0;
    }

    public StatusCode playCard(Player actingPlayer, Card card) {
        //Remove card from current player and update deck?
        if (actingPlayer != getCurrentPlayer()) {
            System.out.println("It is not your turn");
            return StatusCode.FAILED;
        }

        if (card.getSuit() != deck.getCurrentSuit() && card.getValue() != deck.getCurrentValue() && card.getValue() < 14) {
            System.out.println("You dont have this color or card value card.");
            return StatusCode.FAILED;
        }

        if (actingPlayer.getEndOfTurn()) {
            System.out.println("You have already acted this turn.");
            return StatusCode.FAILED;
        }

        //remove card from the player's hand
        actingPlayer.removeCard(card);
        actingPlayer.setEndOfTurn(true);

        // if statement for whenever the card value is above 10, which declares an action card
        if (card.value > 10) {
            callActionCards(card.value);
        }

        //add the card to the discard pile
        deck.currentDiscardCard(card);
        if (actingPlayer.getPlayerHandSize()==1) {
            unoCalledPlayers.put(actingPlayer,false);
        }else{
            unoCalledPlayers.put(actingPlayer,true);
        }

        if (actingPlayer.getCards().isEmpty()) {
            return StatusCode.END_GAME;
        } else {
            return StatusCode.SUCCESS;
        }
    }

    public void dealCards() {
        if (deck.getCards().isEmpty()) {
            System.out.println("Empty Deck. Cannot draw cards to players");
            return;
        }

        for (Player player : players) {
            for (int i = 0; i < STARTING_CARD_AMOUNT; i++) {
                player.addCard(deck.drawCard());
            }
        }
    }

    // method for checking if any values greater than 10 are applicable, which will declare a specific action card effect
    public void callActionCards(int value) {
        switch (value) {
            case 11:
                forceDraw(2);
                break;
            case 12:
                skipTurn();
                break;
            case 13:
                reverseOrder();
                break;
            case 15:
                forceDraw(4);
                break;
        }
    }

    public StatusCode drawCard(Player actingPlayer) {
        //Draw a card from deck and add it to the current player?
        if (actingPlayer != getCurrentPlayer()) {
            System.out.println("It is not your turn");
            return StatusCode.FAILED;
        }

        if (actingPlayer.getEndOfTurn()) {
            System.out.println("You have already acted this turn.");
            return StatusCode.FAILED;
        }

        Card drawnCard = deck.drawCard();
        actingPlayer.setEndOfTurn(true);

        if (drawnCard != null) {

            actingPlayer.addCard(drawnCard);
            // drawing card if it matches discardpile
            if(drawnCard.getSuit() == deck.getCurrentSuit() || drawnCard.getValue()==deck.getCurrentValue() ){
                System.out.println("Drawn card matches the discard pile. Forced to play the card.");
                playCard(actingPlayer, drawnCard);
                return StatusCode.SUCCESS;
            } else {
                return StatusCode.SUCCESS;
            }
        } else {
            System.out.println("No more cards in the deck");
            return StatusCode.FAILED;
        }

    }

    // check for player card count if 0, run if met
    boolean checkIfWin() {

        for (Player player : players) {
            if (player.getPlayerHandSize() == 0) {
                showWinner(player);
                player.incrementRoundsWon();
                updateCurrentPlayerPoints(player);
                return true;
            } else {

            }
        }
        return false;
    }

    public void showWinner(Player player){
        System.out.println(player.getPlayerUsername()+" WON THE GAME!");
        System.out.println(player.getRoundsWon());
    }

    public void skipTurn() {
        //Increase currentPlayerIndex by 1. Loop around if at the end of list.
        skipPlayed = true;
    }

    public void reverseOrder() {

        if (reversePlayOrder) {
            reversePlayOrder = false;
        } else {
            reversePlayOrder = true;
        }

    }

    public void forceDraw(int amount) {
        //Force the next player to draw amount of cards
        for (int cards = 0; cards < amount; cards++) {
            getNextPlayer().addCard(deck.drawCard());
        }
    }

    private void incrementPlayerTurn() {
        if (!reversePlayOrder) {
            currentPlayerIndex = currentPlayerIndex >= players.size() - 1 ? 0 : currentPlayerIndex + 1;
            if (getSkipPlayed()) {
                currentPlayerIndex = currentPlayerIndex >= players.size() - 1 ? 0 : currentPlayerIndex + 1;
            }
        } else {
            currentPlayerIndex = currentPlayerIndex <= 0 ? players.size() - 1 : currentPlayerIndex - 1;
            if (getSkipPlayed()) {
                currentPlayerIndex = currentPlayerIndex <= 0 ? players.size() - 1 : currentPlayerIndex - 1;
            }
        }
    }

    public StatusCode endTurn(Player actingPlayer) {

        if (actingPlayer == getCurrentPlayer() && actingPlayer.getEndOfTurn()) {
            incrementPlayerTurn();
            actingPlayer.setEndOfTurn(false);
            getCurrentPlayer().setEndOfTurn(false);
            setSkipPlayed(false);

            for(Map.Entry<Player,Boolean> entry : unoCalledPlayers.entrySet()){
                if(entry.getKey() == actingPlayer && !entry.getValue()){
                    entry.getKey().addCard(deck.drawCard());
                    entry.getKey().addCard(deck.drawCard());
                    return StatusCode.MISSED_UNO;
                }
            }
            restartTurnTimer();
            return StatusCode.SUCCESS;
        } else {
            return StatusCode.FAILED;
        }

    }

    public void restartTurnTimer() {
        if (turnTimerTask != null) {
            turnTimerTask.cancel();
        }

        if (turnTimer != null) {
            turnTimer.cancel();
        }

        turnTimer = new Timer(true);
        turnTimerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println(getCurrentPlayer().getPlayerUsername() + " has ran out of time!");
                getCurrentPlayer().setEndOfTurn(true);
                endTurn(getCurrentPlayer());
                Server.Server.broadcastMessage("Time's up! It's " + getCurrentPlayer().getPlayerUsername() + "'s turn!.");
                Server.Server.broadcastGameState();
            }
        };
        turnTimer.schedule(turnTimerTask, Duration.ofSeconds(TURN_TIMER_IN_SECONDS).toMillis());
    }

    public void cancelTurnTimer() {
        if (turnTimerTask != null) {
            turnTimerTask.cancel();
        }

        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = null;
        turnTimerTask = null;
    }

    public boolean playerTimeout(int playerID){
        Player player = getPlayerFromID(playerID);
        if (player != null && player.equals(getCurrentPlayer())) {
            System.out.println("Player " + player.getPlayerID() + " has timed out.");
            endTurn(getPlayerFromID(playerID));
            return true;
        }

        return false;
    }

    public Player getNextPlayer() {

        //return next player by using currentPlayerIndex and players
        if (!reversePlayOrder) {
            return currentPlayerIndex >= players.size() - 1 ? players.getFirst() : players.get(currentPlayerIndex + 1);
        } else {
            return currentPlayerIndex <= 0 ? players.getLast() : players.get(currentPlayerIndex - 1);
        }
    }

    public Player getCurrentPlayer() {

        //return current player by using currentPlayerIndex and players
        return players.get(currentPlayerIndex);
    }

    public Deck getDeck() {
        return deck;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isReversePlayOrder() {
        return reversePlayOrder;
    }

    public Player getPlayerFromID(int playerID) {
        for (Player player : players) {
            if (player.getPlayerID() == playerID) {
                return player;
            }
        }

        return null;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean getSkipPlayed() {
        return skipPlayed;
    }

    public void setSkipPlayed(boolean skipPlayed) {
        this.skipPlayed = skipPlayed;
    }

    public void startGame() {
        isStarted = true;
        dealCards();
        restartTurnTimer();


        if (gameTimerTask != null) {
            gameTimerTask.cancel();
        }

        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new Timer(true);
        gameTimerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Game ran out of time! Cancelling it!");
                cancelTurnTimer();
                cancelGame();
                Server.Server.cancelGame();
                Server.Server.broadcastGameState();
            }
        };
        gameTimer.schedule(gameTimerTask, Duration.ofMinutes(GAME_TIMER_IN_MINUTES).toMillis() + 250);


    }

    // creating a method to  calcualte points of current player after end of game in which curret player points is equal to cards at oppents hand
    private int calculateTotalPointsForOpponents(Player currentPlayer) {
        int totalPoints = 0;
        for (Player player : players) {
            if (player != currentPlayer) {
                totalPoints += player.calculateHandPoints();
            }
        }
        return totalPoints;
    }

    private void updateCurrentPlayerPoints(Player currentPlayer) {
        int points = calculateTotalPointsForOpponents(currentPlayer);
        currentPlayer.setPointsGained(points);
    }

    public void cancelGame(){
        gameTimer.cancel();
        gameTimer = null;
        setStarted(false);
        System.out.println("Game has been cancelled ended.");
    }

    public Player endGame(Player winner){
        gameTimer.cancel();
        gameTimer = null;
        cancelTurnTimer();
        setStarted(false);

        for (Player player : players) {
//            player.clearHand();
//            player.clearPoints();
        }

        System.out.println("Game has ended.");
        return winner;
    }

    public StatusCode callUno(Player player){
        if(player.getPlayerHandSize() == 1) {
            unoCalledPlayers.put(player,true);
            return StatusCode.SUCCESS;
        }
        else {
            System.out.println("You must have exactly 1 card left to call Uno");
            return StatusCode.FAILED;
        }
    }
}
