var myPlayerId;

function addPlayersStats(players) {
    console.log(players);
    players.forEach(function(player) {
        html = '<div class="playerStat" data-player-id="' + player.id + '">' + player.name + ' HP: ' + player.health + '</div>';
        console.log(html);
        $("#statsView").append(html);
    });
}

function addHandCards(cards) {
    cards.forEach(function(card) {
        html = '<img class="handCard" data-card-id="' + card.id + '" src="cards/' + card.type + '.jpg"' + '>';
        $("#playerMeView").append(html);
    });
}