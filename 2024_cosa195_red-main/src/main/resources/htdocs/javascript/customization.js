"use strict";
//GLOBAL VARIABLE DECLARATION
const SERVER_URL = "http://localhost:7070"
let playerUsername = sessionStorage.getItem("username") || window.prompt("What is your username?");
let playerObj;
let playerID;
let chosenAvatar;
let chosenCardImage;
let currencyAmount;
let isLoaded = false;

function deselectAll() {
    document.querySelectorAll('.card-button').forEach(btn => {
        btn.textContent = 'Select'
        btn.style.backgroundColor = 'red';
    });
}

const url = SERVER_URL+"/getcosmetics";

    fetch(url, {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            'username': playerUsername
        }
    }).then(response => response.json())
    .then(player => {
        console.log(player);
        playerObj = player;

        chosenAvatar = player.avatar;
        chosenCardImage = player.cardImage;
        currencyAmount = player.currencyValue;
        playerUsername = player.playerUsername;
        playerID = player.playerID;

        $("#"+chosenAvatar+"").text('Selected').css("background-color", "blue");
        $("#"+chosenCardImage+"").text('Selected').css("background-color", "blue");
        isLoaded = true;
    });

//Oleksii wrote the code below, I just moved it to its own JS file
document.querySelectorAll('.avatar-button').forEach(button => {
    button.addEventListener('click', function() {
        document.querySelectorAll('.avatar-button').forEach(btn => {
            if (btn !== this) {
                btn.textContent = 'Select'
                btn.style.backgroundColor = 'red';
            }
        });

        if (isLoaded && this.textContent !== 'Selected') {
            this.textContent = 'Selected';
            this.style.backgroundColor = 'blue';
            let url = SERVER_URL + "/updatecosmetics"
            playerObj.avatar = this.id;
            fetch(url, {
                method: "POST",
                body: JSON.stringify(playerObj),
                headers: {
                    'Content-Type': 'application/json',
                }
            }).then(response => response.json())
                .then(player => {
                    console.log("Updated Avatar");
                    console.log(player);
                    playerObj = player;

                    chosenAvatar = player.avatar;
                    chosenCardImage = player.cardImage;
                    currencyAmount = player.currencyValue;
                    playerUsername = player.playerUsername;
                    playerID = player.playerID;
                    deselectAll();
                    $("#"+chosenAvatar+"").text('Selected').css("background-color", "blue");
                    $("#"+chosenCardImage+"").text('Selected').css("background-color", "blue");
                })
        }
    });
});

document.querySelectorAll('.card-button').forEach(button => {
    button.addEventListener('click', function() {
        document.querySelectorAll('.card-button').forEach(btn => {
            if (btn !== this) {
                btn.textContent = 'Select'
                btn.style.backgroundColor = 'red';
            }
        });

        if (isLoaded && this.textContent !== 'Selected') {
            console.log(this.textContent);
            this.textContent = 'Selected';
            this.style.backgroundColor = 'blue';
            let url = SERVER_URL + "/updatecosmetics"
            playerObj.cardImage = this.id;
            fetch(url, {
                method: "POST",
                body: JSON.stringify(playerObj),
                headers: {
                    'Content-Type': 'application/json',
                }
            }).then(response => response.json())
                .then(player => {
                    console.log("Updated Cardback");
                    console.log(player);
                    playerObj = player;

                    chosenAvatar = player.avatar;
                    chosenCardImage = player.cardImage;
                    currencyAmount = player.currencyValue;
                    playerUsername = player.playerUsername;
                    playerID = player.playerID;
                    deselectAll();
                    $("#"+chosenAvatar+"").text('Selected').css("background-color", "blue");
                    $("#"+chosenCardImage+"").text('Selected').css("background-color", "blue");
                })
        }
    });
});