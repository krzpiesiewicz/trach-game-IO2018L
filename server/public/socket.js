/**
 * Global variables needed for estabilishing connection
 */
var wsUri = "ws://localhost:9000/ws";
var websocket;

/**
 * Initialize connection with server
 */
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

/**
 * Send request for the new game to server
 * @param {object} evt 
 */
function onOpen(evt) {
    console.log("CONNECTED");
    doSend('{"msgType": "QuickMultiplayerGameRequest"}');
}

/**
 * Display information about connection loss to console
 * @param {object} evt 
 */
function onClose(evt) {
    console.log("DISCONNECTED");
}

/**
 * Handle message receiving
 * @param {object} evt 
 */
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

/**
 * Display information about connection error to console
 * @param {object} evt 
 */
function onError(evt) {
    console.log("ERROR" + evt.data);
}


/**
 * Send message
 * @param {string} message 
 */
function doSend(message) {
    console.log("SENT: ");
    console.log(JSON.parse(message));
    websocket.send(message);
}

window.addEventListener("load", init, false);

/**
 * Handles updates of game state
 * @param {object} gs 
 */
function handleGameStateUpdate(gs) {
    gameState = gs;
    updateView();
}

/**
 * Sends information to server that player tried to play a card
 * @param {number} thrownCard 
 * @param {number} parentId 
 * @param {number} targetId 
 */
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

/**
 * Sends information, that no more action 
 * is requested by the player in current turn
 */
function sendNoActionRequest() {
    var msg = {
        msgType: "NoActionRequest",
        gamePlayId: gamePlayId,
        playerId: myPlayerId,
        updateId: updateId
    };
    doSend(JSON.stringify(msg));
}

/**
 * Sends information to server that player 
 * tried to play a tree of cards
 * @param {number} atId 
 */
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
/**
 * Returns card from players hand by its index
 * @param {number} idx 
 * @returns {object} 
 */
function getCardByIdx(idx) {
    return gameState.players[myPlayerId - 1].hand[idx];
}