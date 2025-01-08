package ServiceController;

import DataAccessObject.PlayerDAO;
import DataObject.Player;

import java.util.List;
import io.javalin.http.Handler;

public class PlayerServiceController {
    public static Handler fetchAllPlayers = ctx -> {
        List<Player> list = PlayerDAO.getPlayerFromID();
        ctx.json(list);
    };

    public static Handler fetchPlayerById = ctx -> {
        String playerID = ctx.pathParam("playerID");

        Player player = PlayerDAO.getPlayerFromID(playerID);

        if (player != null) {
            ctx.json(player);
        }
    };

    public static Handler postOrPutPlayer = ctx -> {
        Player player = ctx.bodyAsClass(Player.class);
        PlayerDAO.addOrUpdatePlayer(player);
    };
}
