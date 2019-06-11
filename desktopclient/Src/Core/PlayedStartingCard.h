
#ifndef TRACH_PLAYEDSTARTINGCARD_H
#define TRACH_PLAYEDSTARTINGCARD_H


#include <memory>
#include "Card.h"
#include <cpprest/json.h>

/**
 * represents single Played card played by player that is at top of the tree
 */
class PlayedStartingCard
{
public:
    /**
     * @param obj object to deserialize from
     */
    PlayedStartingCard(web::json::value obj)
    {
        card = new Card(obj["card"]);
        whoPlayedId = obj["whoPlayedId"].as_integer();
        targetPlayer = obj["type"].as_string() == "PlayedStartingCardAtPlayer";
        if (targetPlayer)
        {
            targetId = obj["targetPlayerId"].as_integer();
        } else {
            targetId = obj["targetCardId"].as_integer();
        }
    }

    /**
     * card this object represents
     */
    Card *card;

    /**
     * id of player who played this card
     */
    int whoPlayedId;

    /**
     * id of current target
     */
    int targetId;

    /**
     * is current target a player
     */
    bool targetPlayer;
};


#endif //TRACH_PLAYEDSTARTINGCARD_H
