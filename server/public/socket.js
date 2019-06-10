var wsUri = "ws://localhost:9000/ws";
var websocket;

function init() {
    displayInit();
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
        onOpen(evt)
    };
    websocket.onclose = function(evt) {
        onClose(evt)
    };
    websocket.onmessage = function(evt) {
        onMessage(evt)
    };
    websocket.onerror = function(evt) {
        onError(evt)
    };
}

function onOpen(evt) {
    console.log("CONNECTED");
    doSend('{"msgType": "QuickMultiplayerGameRequest"}');
}

function onClose(evt) {
    console.log("DISCONNECTED");
}

function onMessage(evt) {
    msg = JSON.parse(evt.data);
    console.log(msg);
    if (msg.msgType == "GamePlayInfoUpdate") {
        myPlayerId = msg.playerId;
        gamePlayId = msg.gamePlayId;
    }
    if (msg.msgType == "GameStateUpdate") {
        updateId = msg.updateId;
        handleGameStateUpdate(msg.gameState);
    }
}

function onError(evt) {
    console.log("ERROR" + evt.data);
}

function doSend(message) {
    console.log("SENT: ");
    console.log(JSON.parse(message));
    websocket.send(message);
}

window.addEventListener("load", init, false);

function handleGameStateUpdate(gs) {
    gameState = gs;
    updateView();
}

function sendPlayedRequest(thrownCard, parentId, targetId) {
    var msg = {
        msgType: "PlayedCardsRequest",
        gamePlayId: gamePlayId,
        playerId: myPlayerId,
        updateId: updateId,
        played: {
            playedCard: {
                card: thrownCard,
                whoPlayedId: myPlayerId
            },
            childrenNodes: []
        }

    };
    if (parentId == -1) {
        msg.played.playedCard.type = "PlayedStartingCardAtPlayer";
        msg.played.playedCard.targetPlayerId = targetId;
    } else {
        msg.played.playedCard.type = "PlayedCardInTree";
        msg.played.playedCard.parentCardId = parentId;
    }
    doSend(JSON.stringify(msg));
}

function sendNoActionRequest() {
    var msg = {
        msgType: "NoActionRequest",
        gamePlayId: gamePlayId,
        playerId: myPlayerId,
        updateId: updateId
    };
    doSend(JSON.stringify(msg));
}

function playCardWait(atId) {
    var msg = {
        msgType: "PlayedCardsRequest",
        gamePlayId: gamePlayId,
        playerId: myPlayerId,
        updateId: updateId,
        played: waitTree
    };
    if (atId != -1) {
        msg.played.playedCard.parentCardId = atId;
    }
    doSend(JSON.stringify(msg));
}


//HELPERS

function getCardByIdx(idx) {
    return gameState.players[myPlayerId - 1].hand[idx];
}