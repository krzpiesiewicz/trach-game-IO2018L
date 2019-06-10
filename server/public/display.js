// GLOBAL VARIABLES
var myPlayerId;
var gameState;
var gamePlayId;

/**
 * List of targetable card types
 */
var targetableList = ["attack"];

var waitTree;

/**
 * Checks if card type is targetable
 * @param {string} type 
 */
function checkTargetable(type) {
    if (type == "attack") return true;
    else return false;
}

/**
 * Attaches node to card-wait
 * @param {number} thrownCard 
 * @param {number} atId 
 * @param {number} node 
 */
function attachNode(thrownCard, atId, node) {
    if (atId == -1 && $("#cardWait").html() == "") {
        waitTree = {
            playedCard: {
                card: thrownCard,
                whoPlayedId: myPlayerId,
                type: "PlayedCardInTree",
                parentCardId: atId
            },
            childrenNodes: []
        };
    } else
    if (atId == node.playedCard.card.id) {
        node.childrenNodes.push({
            playedCard: {
                card: thrownCard,
                whoPlayedId: myPlayerId,
                type: "PlayedCardInTree",
                parentCardId: atId
            },
            childrenNodes: []
        });
    } else {
        node.childrenNodes.forEach((el) => {
            attachNode(thrownCard, atId, el);
        });
    }
    displayCardWait();
}

/**
 * Adds droppable class to apriopriate elements
 */
function addDroppable() {
    $(".playHere").droppable({
        drop: function(event, ui) {
            var thrownIdx = ui.draggable.attr("data-card-idx");
            var atId = parseInt($(this).attr("data-drop-id"));
            if (thrownIdx == -1) {
                playCardWait(atId);
                $("#cardWait").toggle();
            } else {
                var thrownCard = getCardByIdx(thrownIdx);
                var isTargetable = checkTargetable(thrownCard.type);
                if (isTargetable) {
                    handleTargetableRequest(thrownCard, atId, false);
                } else {
                    sendPlayedRequest(thrownCard, atId, -1);
                }
            }
        }
    });
    $(".playWait").droppable({
        drop: function(event, ui) {
            var thrownIdx = ui.draggable.attr("data-card-idx");
            var atId = parseInt($(this).attr("data-wait-id"));
            var thrownCard = getCardByIdx(thrownIdx);
            var isTargetable = checkTargetable(thrownCard.type);
            if (isTargetable) {
                handleTargetableRequest(thrownCard, atId, true);
            } else {
                attachNode(thrownCard, atId, waitTree);
            }
        }

    });
}

/**
 * Handles card-wait displaying
 */
function displayCardWait() {
    $("#cardWait").html("");
    buildTree(waitTree, $("#cardWait"), "#ffff00", true);
    drag = $("#cardWait img:nth-of-type(1)");
    drag.draggable({
        revert: true,
        containment: "body",
        appendTo: 'body',
        scroll: false,
    });
    drag.attr("data-card-idx", "-1");
}

/**
 * Applies initial settings
 */
function displayInit() {
    $("#beginAction").hide();
    $("#targetChooser").hide();
    $("#NoActionButton").click(function() {
        sendNoActionRequest();
    });
    $("#ToggleCardWait").click(function() {
        $("#cardWait").toggle();
        $("#cardWait").html("");
    });
    $("#cardWait").hide();
    addDroppable();
}


/**
 * Handles target choosing after playing targetable card
 * @param {number} thrownCard 
 * @param {number} atId 
 * @param {number} cardWait 
 */
function handleTargetableRequest(thrownCard, atId, cardWait) {
    $("#targetChooser").show();

    $(".target").click(function() {
        var targetId = $(this).attr("data-target-id");
        targetId = parseInt(targetId);
        if (!cardWait) {
            sendPlayedRequest(thrownCard, atId, targetId);
        } else {
            waitTree = {
                playedCard: {
                    card: thrownCard,
                    whoPlayedId: myPlayerId,
                    targetPlayerId: targetId,
                    type: "PlayedStartingCardAtPlayer"
                },
                childrenNodes: []
            };
            displayCardWait();
        }
        $("#targetChooser").hide();
        $(".target").unbind();
    });
}


/**
 * Updates view after game-state change
 */
function updateView() {
    $("#tree").hide();
    if (gameState.cardTrees.length == 0 &&
        gameState.playerIdOnMove == myPlayerId) {
        $("#beginAction").show();
    } else {
        $("#beginAction").hide();
    }

    // if ('cardTree' in gameState) {
    //     $("#tree").html("");
    //     $("#tree").show();
    //     buildTree(gameState.cardTree, $("#tree"), "#ffff00");
    // }

    if (gameState.cardTrees.length > 0) {
        $("#tree").html("");
        $("#tree").show();
        gameState.cardTrees.forEach((tree) => {
            buildTree(tree, $("#tree"), "#ffff00");
        });
    }

    addPlayersStats(gameState.players);
    addHandCards(gameState.players[myPlayerId - 1].hand);
    addTargets(gameState.players);
}


/**
 * Adds target to target-choosing view
 * @param {object[]} players 
 */
function addTargets(players) {
    $("#targetChooser").html("");
    players.forEach(function(player) {
        html = '<button class="target" data-target-id="' + player.id + '">' + player.id + '</button>';
        $("#targetChooser").append(html);
    });
}

/**
 * Adds player stats to players view
 * @param {object[]} players 
 */
function addPlayersStats(players) {
    $("#statsView").html("");
    players.forEach(function(player) {
        html = '<div class="playerStat" data-player-id="' + player.id + '">' + player.name + ' HP: ' + player.health + '</div>';
        $("#statsView").append(html);
    });
}


/**
 * Adds cards to player's hand view
 * @param {object[]} cards 
 */
function addHandCards(cards) {
    $("#playerMeView").html("");
    cards.forEach(function(card, idx) {
        html = '<img class="handCard" data-card-idx="' + idx + '" src="/assets/cards/' + card.type + '.jpg"' + '>';
        $("#playerMeView").append(html);
    });
    $(".handCard").draggable({
        revert: true,
        containment: "body",
        appendTo: 'body',
        scroll: false,
    });
}

/**
 * Builds tree on table (or subtree)
 * @param {number} node 
 * @param {object} div 
 * @param {string} color 
 * @param {boolean} cardWait 
 */
function buildTree(node, div, color, cardWait = false) {
    div.css("background-color", color);
    div.append('<div class="cardContainer"></div>')
    displayCard(node.playedCard, div.children().last(), color, cardWait);

    var newColor = lightenDarkenColor(color, -30); //TODO
    div.append('<div class="childrenContainer"></div>');
    var childrenContainer = div.children().last();

    node.childrenNodes.forEach(function(child) {
        childrenContainer.append('<div class="node"></div>');
        newDiv = childrenContainer.children().last();
        buildTree(child, newDiv, newColor, cardWait);
    });

    addDroppable();
}


/**
 * Displays card in tree
 * @param {object} pC 
 * @param {object} div 
 * @param {string} color 
 * @param {boolean} cardWait 
 */
function displayCard(pC, div, color, cardWait) {
    card = pC.card;
    html = '<span class="playerId">' + pC.whoPlayedId + '</span>';
    dataType = cardWait ? 'wait' : 'drop';
    dropClass = cardWait ? 'playWait' : 'playHere';
    html += '<img class="imgAtTable ' + dropClass + '" data-' + dataType + '-id="' + card.id + '" src="/assets/cards/' + card.type + '.jpg">';
    if (pC.hasOwnProperty('targetPlayerId')) {
        target = pC.targetPlayerId;
    } else {
        target = "";
    }
    html += '<span class="playerId"> ' + target + ' </span>';
    div.append(html);

}

/**
 * Tunes color's brightness
 * @param {string} col 
 * @param {number} amt 
 */
function lightenDarkenColor(col, amt) { // Function copied from internet. Don't blame me for the style.
    var usePound = false;
    if (col[0] == "#") {
        col = col.slice(1);
        usePound = true;
    }
    var num = parseInt(col, 16);
    var r = (num >> 16) + amt;
    if (r > 255) r = 255;
    else if (r < 0) r = 0;
    var b = ((num >> 8) & 0x00FF) + amt;
    if (b > 255) b = 255;
    else if (b < 0) b = 0;
    var g = (num & 0x0000FF) + amt;
    if (g > 255) g = 255;
    else if (g < 0) g = 0;
    return (usePound ? "#" : "") + (g | (b << 8) | (r << 16)).toString(16);
}

/**
 * Tree display test
 */
var treeTest = {
    playedCard: {
        type: "PlayedStartingCardAtPlayer",
        card: {
            id: 13,
            type: "attack"
        },
        whoPlayedId: 1,
        targetPlayer: 2
    },
    childrenNodes: [{
            playedCard: {
                type: "PlayedCardInTree",
                card: {
                    id: 17,
                    type: "priority_inc"
                },
                whoPlayedId: 1,
                parentCardId: 13
            },
            childrenNodes: [

            ]
        },
        {
            playedCard: {
                type: "PlayedCardInTree",
                card: {
                    id: 19,
                    type: "defence"
                },
                whoPlayedId: 2,
                parentCardId: 17
            },
            childrenNodes: [{
                playedCard: {
                    type: "PlayedCardInTree",
                    card: {
                        id: 34,
                        type: "priority_inc"
                    },
                    whoPlayedId: 2,
                    parentCardId: 19
                },
                childrenNodes: [

                ]
            }]
        },
    ]
};

/**
 * Test invoker
 */
function doTestTree() {
    buildTree(treeTest, $("#tree"), "#ffff00");
}