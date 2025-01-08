const urlParams = new URLSearchParams(window.location.search);
const winningPlayer = urlParams.get('winner');
const winningPlayerPoints = urlParams.get('points');
const winningPlayerLifetimePoints = urlParams.get('ltPoints');


if (winningPlayer !== null && winningPlayerPoints !== null && winningPlayerLifetimePoints !== null) {
    $("#congrats_message").text("Congratulations, " + winningPlayer + "! You've won!");
    $("#congrats_points").text("Total Points This Game: " + winningPlayerPoints);
    $("#congrats_lifetime_points").text("Lifetime Points: " + winningPlayerLifetimePoints);
}

$("#play_again").click(function (){
    window.location.replace("intro_screen.html");
});