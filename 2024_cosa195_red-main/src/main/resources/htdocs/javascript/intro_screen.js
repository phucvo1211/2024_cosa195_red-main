const urlParams = new URLSearchParams(window.location.search);
const failedToJoin = urlParams.get('unableToJoin');
const errorMessage = $('#errorMessage');

if (failedToJoin === "true") {
    errorMessage.text("Unable to join game.");
    errorMessage.addClass("visible");
}

let username = sessionStorage.getItem("username");
if (username !== null) {
    $("#username").text(username);
}

document.getElementById("connect").onclick = function () {
    let username= document.getElementById("username").value;

    if (username === "" || username === null || username.length > 16) {
        errorMessage.addClass("visible");
        errorMessage.text("You must enter a valid username with no more than 16 characters");
    } else {
        sessionStorage.setItem("username", username);
        errorMessage.removeClass("visible");
        window.location.replace("game.html?joiningGame=true");
    }
}

document.getElementById("customise").onclick = function () {
    let username= document.getElementById("username").value;
    let errorMessage = $('#errorMessage');

    if (username === "" || username === null || username.length > 16) {
        errorMessage.addClass('visible');
        errorMessage.text("You must enter a valid username with no more than 16 characters.");
    } else {
        sessionStorage.setItem("username",username);
        errorMessage.removeClass('visible');
        window.location.assign("customization_screen.html");
    }

}