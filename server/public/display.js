// GLOBAL VARIABLES
var myPlayerId;
var gameState;
var gamePlayId;

var targetableList = ["attack"];

function checkTargetable(type) {
    if (type == "attack") return true;
    else return false;
    // targetableList.forEach(function(el) {
    //     console.log(el);
    //     if (el == type) {
    //         return true;
    //     }
    // });
    // return false;
}


function displayInit() {
    $("#beginAction").hide();

    $(".playHere").droppable({
        drop: function(event, ui) {
            var thrownIdx = ui.draggable.attr("data-card-idx");
            var atId = $(this).attr("data-drop-id");
            var thrownCard = getCardByIdx(thrownIdx);
            var isTargetable = checkTargetable(thrownCard.type);
            console.log(isTargetable);
            if (isTargetable) {
                handleTargetableRequest(thrownCard, atId);
            } else {
                sendPlayedRequest(thrownCard, atId, -1);
            }
        }
    });

    $("#targetChooser").hide();
}

function handleTargetableRequest(thrownCard, atId) {
    $("#targetChooser").show();

    $(".target").click(function() {
        var targetId = $(this).attr("data-target-id");
        targetId = parseInt(targetId);
        sendPlayedRequest(thrownCard, atId, targetId);
        $("#targetChooser").hide();
        $(".target").unbind();
    });
}

function updateView() {
    if (gameState.tableActiveCards.length == 0 &&
        gameState.playerIdOnMove == myPlayerId) {
        $("#beginAction").show();
    } else {
        $("#beginAction").hide();
    }
    addPlayersStats(gameState.players);
    addHandCards(gameState.players[myPlayerId - 1].hand);
    addTargets(gameState.players);
}

function addTargets(players) {
    $("#targetChooser").html("");
    players.forEach(function(player) {
        html = '<button class="target" data-target-id="' + player.id + '">' + player.id + '</button>';
        $("#targetChooser").append(html);
    });
}

function addPlayersStats(players) {
    $("#statsView").html("");
    players.forEach(function(player) {
        html = '<div class="playerStat" data-player-id="' + player.id + '">' + player.name + ' HP: ' + player.health + '</div>';
        $("#statsView").append(html);
    });
}

function addHandCards(cards) {
    $("#playerMeView").html("");
    cards.forEach(function(card, idx) {
        html = '<img class="handCard" data-card-idx="' + idx + '" src="cards/' + card.type + '.jpg"' + '>';
        $("#playerMeView").append(html);
    });
    $(".handCard").draggable({
        revert: true,
        containment: "body",
        appendTo: 'body',
        scroll: false,
    });
}

function buildTree(node, div, color) {
    div.addClass("node");
    div.css("background-color", color);

    newDiv = displayCard(node.playedCard, div, color);
    newColor = lightenDarkenColor(color, 40); //TODO
    node.childrenNodes.forEach(function(child) {
        buildTree(child, newDiv, newColor);
    });
}

function displayCard(card, div, color) {
    //html = "div"

}

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

function doTestTree() {
    buildTree(treeTest, $("body"), "#ffff00");
}