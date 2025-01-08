package DataAccessObject;

import DataObject.AvatarImage;
import DataObject.CardImage;
import DataObject.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {
    public static List<Player> getPlayerFromID() {
        ArrayList<Player> players = new ArrayList<Player>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/database.db");
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                players.add(new Player(
                        resultSet.getInt("playerID"),
                        resultSet.getString("playerUsername"),
                        resultSet.getInt("roundsWon"),
                        resultSet.getInt("roundsLost"),
                        resultSet.getInt("pointsGained"),
                        resultSet.getInt("pointsLost"),
                        resultSet.getInt("currencyValue"),
                        AvatarImage.valueOf(resultSet.getString("avatarImage")),
                        CardImage.valueOf(resultSet.getString("cardImage"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {

                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                connection = null;
            }
        }

        return players;
    }

    public static Player getPlayerFromID(String playerID) {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/database.db");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Player WHERE playerID = ?");
            statement.setString(1, playerID);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return new Player(
                        resultSet.getInt("playerID"),
                        resultSet.getString("playerUsername"),
                        resultSet.getInt("roundsWon"),
                        resultSet.getInt("roundsLost"),
                        resultSet.getInt("pointsGained"),
                        resultSet.getInt("pointsLost"),
                        resultSet.getInt("currencyValue"),
                        AvatarImage.valueOf(resultSet.getString("avatarImage")),
                        CardImage.valueOf(resultSet.getString("cardImage"))
                );
            }
        }

        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        finally {
            if (connection != null) {
                try {
                    connection.close();
                }

                catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                connection = null;
            }
        }

        return null;
    }

    public static Player getPlayer(String playerUsername) {
        Connection connection = null;
        Player player = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/database.db");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Player WHERE playerUsername = ?");
            statement.setString(1, playerUsername);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                player = new Player(
                        resultSet.getInt("playerID"),
                        resultSet.getString("playerUsername"),
                        resultSet.getInt("roundsWon"),
                        resultSet.getInt("roundsLost"),
                        resultSet.getInt("pointsGained"),
                        resultSet.getInt("pointsLost"),
                        resultSet.getInt("currencyValue"),
                        AvatarImage.valueOf(resultSet.getString("avatarImage")),
                        CardImage.valueOf(resultSet.getString("cardImage"))
                );
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }

                catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                connection = null;
            }
        }

        return player;
    }

    public static void addOrUpdatePlayer(Player player) {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/database.db");

            PreparedStatement statement;

            if (player.getPlayerID() == 0) {
                statement = connection.prepareStatement("INSERT INTO Player(playerUsername, roundsWon, roundsLost, pointsGained, pointsLost, currencyValue, avatarImage, cardImage) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            }

            else {
                statement = connection.prepareStatement("UPDATE Player SET playerUsername = ?, roundsWon = ?, roundsLost = ?, pointsGained = ?, pointsLost = ?, currencyValue = ?, avatarImage = ?, cardImage = ? WHERE playerID = ?");
                statement.setInt(9, player.getPlayerID());
            }

            statement.setString(1, player.getPlayerUsername());
            statement.setInt(2, player.getRoundsWon());
            statement.setInt(3, player.getRoundsLost());
            statement.setInt(4, player.getPointsGained());
            statement.setInt(5, player.getPointsLost());
            statement.setInt(6, player.getCurrencyValue());
            statement.setString(7, player.getAvatar().name());
            statement.setString(8, player.getCardImage().name());

            System.out.println("Inserted " + statement.executeUpdate() + " records.");
        }

        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        finally {
            if (connection != null) {
                try {
                    connection.close();
                }

                catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                connection = null;
            }
        }
    }
}
