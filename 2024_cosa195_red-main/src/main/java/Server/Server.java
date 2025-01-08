package Server;

import DataAccessObject.PlayerDAO;
import DataObject.Card;
import DataObject.Game;
import DataObject.Player;
import DataObject.StatusCode;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.sse.SseClient;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.util.ArrayList;
import java.util.Iterator;

public class Server {
    private static Game game;

    public static ArrayList<SseClient> sseConnections = new ArrayList<>();

//    static Player newPlayer;

    public static void main(String[] args) {

        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.staticFiles.add("/htdocs", Location.CLASSPATH);
                    javalinConfig.plugins.enableCors(corsContainer -> {
                        corsContainer.add(CorsPluginConfig::anyHost);
                    });
                })
                .start(7070);

            //Clients join the game using a GET request at /join_game
            app.get("/join_game",  joinGame);

            //Clients deal cards and begin game by using a GET request at /dealcards
            app.get("/dealcards", dealCards);

            //Clients request to play a card with a POST request at /playcard
            app.post("/playcard", playCard);

            //Clients request to draw a card with a GET request at /drawcard
            app.get("/drawcard", drawCard);

            //Clients request to handle timeout with a GET request at /outOfTime
            app.get("/outoftime", outOfTime);

            //Clients request their cosmetics with a GET request at /getcosmetics
            app.get("/getcosmetics", getCosmetic);

            //Clients request to update their cosmetics with a POST request at /updatecosmetics
            app.post("/updatecosmetics", updateCosmetic);

            app.post("/endturn", endTurn);

            app.post("/uno", declareUno);

            //Clients subscribe to SSE (Server.Server Side Events) at /sse. This is how we'll send them game states.
            app.sse("/sse", sseClient -> {
                    System.out.println("New client subscribed to SSE : " + sseClient.ctx().ip());
                    sseClient.keepAlive();
                    sseConnections.add(sseClient);
            });

    }

    public static void broadcastGameState() {
        Iterator<SseClient> iterator = sseConnections.iterator();
        while (iterator.hasNext()) {
            SseClient sctx = iterator.next();
            if (sctx.terminated()) {
                iterator.remove();
            } else {
                System.out.println("Sending game state out to " + sctx.ctx().ip());
                sctx.sendEvent("game_state", game);
            }
        }
    }
    public static void broadcastMessage(String message) {
        Iterator<SseClient> iterator = sseConnections.iterator();
        while (iterator.hasNext()) {
            SseClient sctx = iterator.next();
            if (sctx.terminated()) {
                iterator.remove();
            } else {
                System.out.println("Sending '" + message + "' to : " + sctx.ctx().ip());
                sctx.sendEvent(message);
            }
        }
    }

    public static void resetGame(Player winner) {
        Iterator<SseClient> iterator = sseConnections.iterator();

        while (iterator.hasNext()) {
            SseClient sctx = iterator.next();
            if (sctx.terminated()) {
                iterator.remove();
            } else {
                System.out.println("Sending end of game notice to " + sctx.ctx().ip());
                sctx.sendEvent("end_game", winner);
            }
        }

        game = null;
        sseConnections.forEach(SseClient::close);
    }

    public static void cancelGame() {
        Iterator<SseClient> iterator = sseConnections.iterator();

        while (iterator.hasNext()) {
            SseClient sctx = iterator.next();
            if (sctx.terminated()) {
                iterator.remove();
            } else {
                System.out.println("Sending cancel game notice to " + sctx.ctx().ip());
                sctx.sendEvent("cancel_game", game);
            }
        }

        game = null;
        sseConnections.forEach(SseClient::close);
    }

    //Event Handler for when a user requests to join the game.
    public static Handler joinGame = ctx -> {
        //Instantiate our game if it currently null
        if (game == null) {
            game = new Game();
        }

        //If the game has less than 10 players and isn't started yet, they can join.
        if (game.getPlayers().size() < 10 && !game.isStarted()) {
            //Set variables based on the info sent from client
            String id = ctx.header("from");
            String username = ctx.header("username");

            //Try and fetch player from database
            Player player = PlayerDAO.getPlayer(username);

            //If successful add them to the game
            if (player != null) {
                if (game.getPlayerFromID(player.getPlayerID()) == null) {
                    game.addPlayer(player);
                    System.out.println("New player joined: " + player.getPlayerUsername() + " - " + player.getPlayerID());
                    ctx.json(game);
                    broadcastGameState();
                } else {
                    ctx.result("Unable to join game.");
                }
            } else {
            //Otherwise create a new player then add them to the database and the game.
                PlayerDAO.addOrUpdatePlayer(new Player(username));
                player = PlayerDAO.getPlayer(username);
                game.addPlayer(player);
                ctx.json(game);
                broadcastGameState();
            }

        } else {
            ctx.result("Unable to join game.");
        }
    };

    //Event Handler for when a user requests to deal cards / start the game.
    public static Handler dealCards = ctx -> {
        String id = ctx.header("from");
        String username = ctx.header("username");
        System.out.println("Deal request made from: " +  username + " - " + id);

        //Start the game if it hasn't started yet
        if (!game.isStarted()) {
            game.startGame();
        }

        broadcastGameState();
    };

    //Event Handler for when a user requests to play a card.
    public static Handler playCard = ctx -> {

        String id = ctx.header("from");
        String username = ctx.header("username");
        Card card = ctx.bodyAsClass(Card.class);

        System.out.println( "Play card requests received from: " + username + " - " + id + " Card: " + card.getSuit() + " " + card.getValue());

        int playerID = id == null ? 0 : Integer.parseInt(id);
        StatusCode code = game.playCard(game.getPlayerFromID(playerID), card);
        switch (code) {
            case SUCCESS:
                broadcastGameState();
                broadcastMessage(username + " played a " + card + "!");
                break;
            case END_GAME:
                Player winningPlayer = game.endGame(game.getPlayerFromID(playerID));
                resetGame(winningPlayer);
                break;
            case FAILED:
                break;
            default:
                break;

        }
    };

    //Event Handler for when a user requests to draw a card.
    public static Handler drawCard = ctx -> {

        String id = ctx.header("from");
        String username = ctx.header("username");
        System.out.println( "Draw card requests received from: " + username + " - " + id);

        if (!game.isStarted()) {
            return;
        }

        int playerID = id == null ? 0 : Integer.parseInt(id);
        StatusCode code = game.drawCard(game.getPlayerFromID(playerID));
        switch (code) {
            case SUCCESS:
                broadcastGameState();
                broadcastMessage(username + " drew a card!");
                break;
            case END_GAME:
                resetGame(game.getPlayerFromID(playerID));
                break;
            case FAILED:
                break;
            default:
                break;

        }
    };

    //Event Handler for when a client's turn timer runs out, and they send a message to the server
    public static Handler outOfTime = ctx -> {
        String id = ctx.header("from");
        String username = ctx.header("username");
        System.out.println( "Play ran out of time: " + username + " - " + id);

        int playerID = id == null ? 0 : Integer.parseInt(id);

        if (game.playerTimeout(playerID)) {
            broadcastGameState();
        }
    };

    public static Handler getCosmetic = ctx -> {
        String username = ctx.header("username");
        Player player = PlayerDAO.getPlayer(username);
        if (player != null) {
            System.out.println("Getting cosmetics for : " + player.getPlayerUsername() + " - " + player.getPlayerID());
            ctx.json(player);
        } else {
            PlayerDAO.addOrUpdatePlayer(new Player(username));
            player = PlayerDAO.getPlayer(username);
            ctx.json(player);
        }
    };

    public static Handler updateCosmetic = ctx -> {
        Player player = ctx.bodyAsClass(Player.class);
        System.out.println("Customisation Test: Received " + player + " : " + player.getPlayerUsername() + ", " + player.getAvatar());
        ctx.json(player);
        PlayerDAO.addOrUpdatePlayer(player);
    };

    public static Handler endTurn = ctx -> {
        String id = ctx.header("from");
        String username = ctx.header("username");

        System.out.println( "End turn request from: " + username + " - " + id);
        int playerID = id == null ? 0 : Integer.parseInt(id);
        StatusCode code = game.endTurn(game.getPlayerFromID(playerID));
        switch (code) {
            case SUCCESS:
                broadcastGameState();
                broadcastMessage("It's " + game.getCurrentPlayer().getPlayerUsername() + "'s turn!");
                break;
            case MISSED_UNO:
                broadcastGameState();
                broadcastMessage(username + " didn't declare UNO! It's " + game.getCurrentPlayer().getPlayerUsername() + "'s turn!");
                break;
            case FAILED:
                ctx.result("You must draw or play a card before ending your turn!");
                break;
            default:
                break;

        }
    };

    public static Handler declareUno = ctx -> {
        String id = ctx.header("from");
        String username = ctx.header("username");

        System.out.println( "End turn request from: " + username + " - " + id);
        int playerID = id == null ? 0 : Integer.parseInt(id);
        StatusCode code = game.callUno(game.getPlayerFromID(playerID));

        switch (code) {
            case SUCCESS:
                broadcastMessage(username + " declares UNO!");
                break;
            case FAILED:
                break;
            default:
                break;

        }
    };
}
