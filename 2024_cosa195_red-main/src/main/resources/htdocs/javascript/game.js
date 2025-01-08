"use strict";
//GLOBAL VARIABLE DECLARATION

//const SERVER_URL = "http://10.28.106.122:7070";
const SERVER_URL = "http://localhost:7070"
const REGULAR_ARROW_IMAGE = "url(../images/circular-arrows.png)";
const REVERSE_ARROW_IMAGE = "url(../images/circular-arrows-reversed.png)";

// let username = sessionStorage.getItem("username") || window.prompt("What is your username?");
let username = sessionStorage.getItem("username") || window.prompt("What is your username?");
//Initialize the card library and tell it which element to use for the table.
cards.init({table:'#card-table', type:UNO});

//Create a new deck of cards and adjust its position.
let deck = new cards.Deck();
deck.addCards(cards.all);
deck.x -= 100;
deck.render({immediate:true});

//Create a new discard pile and adjust its position
let discardPile = new cards.Deck({faceUp:true});
discardPile.x += 50;

//Global variable declaration
const handPlayerMap = new Map();
const playerIDUsernameMap = new Map();
const playerIDAvatarMap = new Map();

let isStarted = false;
let timer = 0;
let timeoutID = null;
let gameEnded = false;
let userID = 0;
let isPickingColor = false;
let currentPlayerID;
let userEndOfTurn = false;

//Function that displays an alert on the screen, just pass the text into it. If an alert is already showing, it won't display anything.
function displayAlert(alert) {

    //Checks if an alert is still being displayed, returns and does nothing if so.
    if ($('#alertBox').length > 0) {
        return;
    }

    let table = $('#card-table');
    let body = $('body');
    let alertBox = $("<div id='alertBox'><p>"+ alert + "</p></div>");
    table.append(alertBox);

    alertBox.css({
        'top': '25%',
        'position': 'absolute',
        'text-shadow': '2px 0 #000000, -2px 0 #000000, 0 2px #000000, 0 -2px #000000',
        'color': 'white',
        'opacity': 0,
        'font-size': '2.5em',
        'max-width': '600px',
        'max-height': '100px'
    });

    let tableWidth = table.width();
    let textWidth = alertBox.width();

    let centerPosition = (tableWidth - textWidth) / 2;

    alertBox.css('left', centerPosition - 50);

    //fade in and move slightly
    alertBox.animate({ 'left': centerPosition - 20, 'opacity': 1 }, 300, 'linear', function() {
        //keep moving but now slower
        alertBox.animate({ 'left': centerPosition + 20, 'opacity': 1 }, 450, 'linear', function() {
            //fade out and move slightly, then remove the element
            alertBox.animate({'left': centerPosition + 50, 'opacity': 0}, 300, 'linear', function() {
                alertBox.remove();
            })
        });
    });
}

function createHand(count) {
    let hand;
    switch(count) {
        case 0:
            hand = new cards.Hand({faceUp:false,  y:675, x:150 + cards.SIZE.width});
            break;
        case 1:
            hand = new cards.Hand({faceUp:false,  y:490, x:150 + cards.SIZE.width});
            break;
        case 2:
            hand = new cards.Hand({faceUp:false,  y:305, x:150 + cards.SIZE.width});
            break;
        case 3:
            hand = new cards.Hand({faceUp:false, y:120, x:150 + cards.SIZE.width});
            break;
        case 4:
            hand = new cards.Hand({faceUp:false, y:120});
            break;
        case 5:
            hand = new cards.Hand({faceUp:false, y:120, x:1050});
            break;
        case 6:
            hand = new cards.Hand({faceUp:false,  y:305, x:1050});
            break;
        case 7:
            hand = new cards.Hand({faceUp:false,  y:490, x:1050});
            break;
        case 8:
            hand = new cards.Hand({faceUp:false,  y:675, x:1050});
            break;
    }

    return hand;
}

function getImagePath(avatarName) {
    switch (avatarName) {
        case "PTSD":
            return "../images/ptsd.jpg";
        case "HOMER":
            return "../images/homer.png";
        case "BANANA":
            return "../images/bananaMan.png";
        default:
            return "../images/ptsd.jpg";
    }
}

function updateHandDisplays() {

    handPlayerMap.forEach((value, key) => {
        $("#handInfo_"+key).remove();

        let imageURL = getImagePath(playerIDAvatarMap.get(key));

        let avatarImg = $("<img src='" + imageURL + "'/>");

        let handCount = $(
            "<div class='handInfo' id='handInfo_" + key + "'>" +
                "<div class='avatar' style='display: flex'></div>" +
                "<div class='info'>" +
                    "<p>Name:</br>" + playerIDUsernameMap.get(key) + "</p>" +
                    "<p class='cardCount'># Of Cards: " + value.length + "</p>" +
                "</div>" +
            "</div>"
        );

        handCount.css({
            "position": "absolute",
            "left": value.x - 130 + "px",
            "top": value.y - 122 + "px",
            "background-color": "darkolivegreen",
            "border": "solid 2px black",
            "z-index": 999,
        });

        avatarImg.css({
            "max-width": "50px",
            "max-height": "50px",
            "margin-right": "5px",
            "border-radius": "10px"
        });

        handCount.find('.avatar').append(avatarImg);

        handCount.appendTo("#card-table");

    })
}

function highlightCurrentPlayer(ID) {

    $("#handInfo_"+ID).css({
        "border": "solid 2px yellow",
        "box-shadow": "0 0 10px 3px yellow"

    });

}

function addHandler(hand) {

    hand.click(function(card){

        if (isPickingColor) {
            return;
        }

        if (currentPlayerID !== userID) {
            displayAlert("It is not your turn.");
            return;
        }

        if (userEndOfTurn) {
            displayAlert("You've already acted this turn.");
            return;
        }

        if (card.rank === 14 || card.rank === 15) {
            // Instead of a prompt, create a popup with buttons
            let popup = document.createElement("div");
            popup.id = "wildCardPopup";
            popup.style.cssText = `
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -40%);
                background-color: white;
                border: 1px solid black;
                padding: 10px;
                z-index: 99999;
              `;

            let title = document.createElement("h3");
            title.textContent = "Choose a Color:";
            popup.appendChild(title);

            // Create buttons for each color option
            let colors = new Map;
            colors.set("r", "red");
            colors.set("y", "yellow");
            colors.set("b", "blue");
            colors.set("g", "green");

            colors.forEach((value, key) => {
                let button = document.createElement("button");
                button.textContent = value;
                button.style.cssText = `
                          margin: 5px;
                          padding: 5px 10px;
                          background-color: ${value};
                          color: black;
                          border: none;
                          cursor: pointer;
                        `;
                button.addEventListener("click", function() {
                    sendWildCard(card, key);
                    // Close the popup after selecting a color
                    isPickingColor = false;
                    document.getElementById("wildCardPopup").remove();
                });

                isPickingColor = true;
                popup.appendChild(button);
            })

            document.body.appendChild(popup);

        } else if (card.rank !== discardPile.topCard().rank && card.suit !== discardPile.topCard().suit) {
            displayAlert("That card does not match the discard pile.");
        } else {
            sendCard(card);
        }
    });
}

function createCardArrayFromJSON(json) {

    let cardData = JSON.parse(JSON.stringify(json));
    let cardArray = [];

    cardData.forEach(item => {
        let newCard = new cards.Card(item.suit, item.value, cards.options.table);
        newCard.x = deck.x;
        newCard.y = deck.y;
        cardArray.push(newCard);
    })

    return cardArray;
}

const urlParams = new URLSearchParams(window.location.search);
const joiningGame = urlParams.get('joiningGame');

if (joiningGame === "true") {

    window.history.replaceState({}, document.title, "/");
    //This fetch method makes a GET request to the server at /join_game. This should create a player for them.
    fetch(SERVER_URL + "/join_game", {
        method: "GET",
        headers: {
            'from': userID,
            'username': username
        }
    }).then(response => {
        //Check to make sure we get a response back from the server
        //If we don't, we errored while joining and should go back to login screen
        let contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") === -1) {
                console.log(response.text().then(value => console.log(value)));
                deck.x = -5000;
                $('#deal').hide();
                window.location.replace("intro_screen.html?unableToJoin=true");
                return;
            }

        //Subscribe to SSE (Server Side Events) at /sse
        const evtSource = new EventSource(SERVER_URL + "/sse");

        //Behaviour when the SSE connection is opened.
        //Behaviour when the SSE connection is opened.
        evtSource.onopen = ev => {
                console.log("SSE Initiated");
            }

        evtSource.addEventListener("game_state", (ev) => {
                console.log("SSE Game State Received");
                let dataJSON = JSON.parse(ev.data);
                console.log(dataJSON);

                let playerArray = dataJSON.players;
                currentPlayerID = dataJSON.currentPlayer.playerID;

                if (currentPlayerID !== userID && isPickingColor) {
                    isPickingColor = false;
                    document.getElementById("wildCardPopup").remove();
                }

                let playerHandCount;
                if (dataJSON.started) {
                    $('#deal').hide();
                    $('#endturn').show();
                    isStarted = true;
                }

                //if the current player is the one playing then send it to the server
                // if(dataJSON.currentPlayer.playerID === userID && isStarted === true){
                //     //method to start the timer
                //     startTimer();
                // } else {
                //     //if not the current player then clear the time interval
                //     clearInterval(timeoutID);
                // }

                //DEBUGGING
                console.log("Map of hands-to-players is as follows:");
                console.log(handPlayerMap.size);
                console.log(handPlayerMap);

                //Find the index of -our- user
                let index = 0;
                for (let i = 0; i < playerArray.length; i++) {
                    if (playerArray[i].playerID === userID) {
                        index = i;
                        userEndOfTurn = playerArray[i].endOfTurn;
                        break;
                    }
                }

                //Update the hand of each player, or create hand if they don't have one yet, and render them
                for (let i = 0; i < playerArray.length; i++) {
                    let player = playerArray[i];

                    if (handPlayerMap.has(player.playerID)) {

                        console.log("Player already has a hand - updating it.");
                        let hand = handPlayerMap.get(player.playerID);

                        let cardArray = createCardArrayFromJSON(player.cards);
                        hand.setCards(cardArray);
                        hand.render({immediate: true});

                        if (player.playerID === userID) {
                            playerHandCount = cardArray.length;
                            console.log("Player hand count " + playerHandCount);
                        }

                    } else {

                        console.log("Player doesn't have a hand - creating one.");
                        let hand = createHand(i - (index + 1));
                        handPlayerMap.set(player.playerID, hand);
                        playerIDUsernameMap.set(player.playerID, player.playerUsername);
                        playerIDAvatarMap.set(player.playerID, player.avatar)

                        let cardArray = createCardArrayFromJSON(player.cards);

                        hand.setCards(cardArray);
                        hand.render({immediate: true});
                    }
                }
                //Highlight current player's hand
                updateHandDisplays();

                if (dataJSON.reversePlayOrder) {
                    $("#card-table").css("background-image", REVERSE_ARROW_IMAGE);
                } else {
                    $("#card-table").css("background-image", REGULAR_ARROW_IMAGE);
                }

                if (isStarted === true) {
                    //Update the cards in the deck and render it
                    let cardArray = createCardArrayFromJSON(dataJSON.deck.cards);
                    deck.setCards(cardArray);
                    deck.render({immediate: true});

                    //update the cards in the discard pile and render it
                    let discardArray = createCardArrayFromJSON(dataJSON.deck.discardedCards);
                    discardPile.setCards(discardArray);
                    discardPile.render({immediate: true});

                    highlightCurrentPlayer(dataJSON.currentPlayer.playerID);
                    updateUNOButton(playerHandCount);
                }

            })

        evtSource.addEventListener("end_game", (ev) => {
            console.log("End Game Received");
            let dataJSON = JSON.parse(ev.data);
            console.log(dataJSON);

            let name = dataJSON.playerUsername;
            let points = dataJSON.pointsGained;
            let ltPoints = dataJSON.pointsGained;

            window.location.replace("end_game.html?winner=" + name + "&points=" + points + "&ltPoints=" + ltPoints);
        })

        evtSource.addEventListener("cancel_game", (ev) => {
            console.log("Cancel Game Received");
            window.location.replace("end_game.html");
        })

        evtSource.onmessage = (response) => {
            displayAlert(response.data);
        }

        //Behaviour when the SSE connection encounters an error (such as a disconnect for more than a few seconds).
        evtSource.onerror = ev => {
            console.log("SSE Errored");
        }

        response.json().then(r => {
            console.log("Joining Game - Game State is as follows: ");
            console.log(r);

            let players = r.players;
            let userAvatar = "PTSD";

            //Find the ID of the user with the same username as us
            for (let i = 0; i < players.length; i++) {
                if (players[i].playerUsername === username) {
                    userID = players[i].playerID;
                    userAvatar = players[i].avatar;
                    break;
                }
            }
            console.log("UserID = " + userID);

            //Find the index of -our- user
            let index = 0;
            for (let i = 0; i < players.length; i++) {
                if (players[i].playerID === userID) {
                    index = i;
                    break;
                }
            }

            console.log("Index = " + index);

            //Make arrays of all players that come before our user
            let beforePlayerArray = players.slice(0, index);
            console.log("Before Player Array: " + beforePlayerArray);

            //Create user's hand
            let hand = new cards.Hand({faceUp:true,  y:825, iPadding: 20});
            handPlayerMap.set(userID, hand);
            playerIDUsernameMap.set(userID, username);
            playerIDAvatarMap.set(userID, userAvatar);
            addHandler(hand);
            hand.render({immediate: true});

            //loop through all hands before our user and create them so they appear on the left side of the screen
            let handCounter = 8;
            for (let i = beforePlayerArray.length - 1; i >= 0; i--) {
                let hand = createHand(handCounter--);
                handPlayerMap.set(players[i].playerID, hand);
                playerIDUsernameMap.set(players[i].playerID, players[i].playerUsername);
                playerIDAvatarMap.set(players[i].playerID, players[i].avatar);
                hand.render({immediate: true});
            }

            updateHandDisplays();
            $("#deal").show();
        }) }, () => {
            //What to do if we do not get a response back from the server (network error, we broke it, etc.)
            console.log("Unable to join game.");
        })
}
//function to send the end game data to server
// function sendEnd(){
//     const  url = SERVER_URL+"/gameEnd"
//     fetch(url, {
//         method: "GET",
//         headers: {
//             'Content-Type': 'application/json',
//             'from': userID
//         }
//     })
// }

//Sending normal card info to server
function sendCard(card) {
    const jsonData = {
        suit: card.suit,
        value: card.rank
    };

    fetch(SERVER_URL + "/playcard", {
        method: "POST",
        body: JSON.stringify(jsonData),
        headers: {
            'Content-Type': 'application/json',
            'from': userID,
            'username': username
        }
    });
}

//Sending wild card info with selected color to server
function sendWildCard(card, selectedColor) {

    const jsonData = {
        suit: selectedColor,
        value: card.rank,
    };

    fetch(SERVER_URL + "/playcard", {
        method: "POST",
        body: JSON.stringify(jsonData),
        headers: {
            'Content-Type': 'application/json',
            'from': userID,
            'username': username
        }
    });
}

//Event handler for when the deal button is clicked. This should send a request to deal cards and start the game.
$('#deal').click(function() {
    $('#deal').hide();

    //Make a request to the server to draw a card
    fetch(SERVER_URL + "/dealcards", {
        method: "GET",
        headers: {
            'from': userID,
            'username': username
        }
    }).then(r => {
        //Logic for any response from the server
    }, reason => {
        //Logic if we do not get a response
    })

});

//Event handler for when the deck is clicked. This should send a request to draw a card.
deck.click(function(card) {


    if (!isStarted) {
        return;
    }

    if (currentPlayerID !== userID) {
        displayAlert("It is not your turn.");
        return;
    }

    if (userEndOfTurn) {
        displayAlert("You've already acted this turn.");
        return;
    }

    fetch(SERVER_URL + "/drawcard", {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            'from': userID,
            'username': username
        }
    }).then(r => {
        //Logic for any response from server
    }, reason => {
        //Logic if we don't get a response from server
    });
});

let unoButton = $("#uno");
unoButton.click(function () {
        const url = SERVER_URL + "/uno";

        fetch(url, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'from': userID,
                'username': username
            }
        }).then(response => {
            if (response.ok) {
                console.log("Sent successfully !!!");
            } else {
                console.error("Failed !");
            }
        }).catch(error => {
            console.error("Error: ", error);
        });
    });

// Logic for updating UNO button visibility based on player's hand size
function updateUNOButton(playerHandSize) {
    if (playerHandSize === 1) {
        unoButton.show(); // Show UNO button
    } else {
        unoButton.hide(); // Hide UNO button
    }
}

$("#endturn").click(function () {

    if (currentPlayerID !== userID) {
        displayAlert("It is not your turn.");
        return;
    }

    const url = SERVER_URL + "/endturn";

    fetch(url, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            'from': userID,
            'username': username
        }
    }).then(value => value.text())
        .then(value => {
            displayAlert(value);
        })

// .then(response => response.text())
//         .then(value => {
//             displayAlert(value);
//         })
//         .catch(error => {
//             console.error("Error: ", error);
//         })
})


