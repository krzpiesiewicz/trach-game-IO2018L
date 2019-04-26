var wsUri = "wss://localhost:9001/ws";
var websocket;

function init() {
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
    if (msg.msgType == "GamePlayStateUpdate") {
        myPlayerId = msg.playerId;
    }
    if (msg.msgType == "GameStateUpdate") {
        handleGameStateUpdate(msg.gameState);
    }
}

function onError(evt) {
    console.log("ERROR" + evt.data);
}

function doSend(message) {
    console.log("SENT: " + message);
    websocket.send(message);
}

window.addEventListener("load", init, false);

function handleGameStateUpdate(gameState) {
    addPlayersStats(gameState.players);
    addHandCards(gameState.players[myPlayerId - 1].hand);
}