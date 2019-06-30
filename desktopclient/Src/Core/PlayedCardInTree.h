
#ifndef TRACH_PLAYEDCARDINTREE_H
#define TRACH_PLAYEDCARDINTREE_H


#include "Card.h"
#include <cpprest/json.h>

/**
 * represents single Played card played by player
 */
class PlayedCardInTree
{
public:

    /**
     * @param obj object to deserialize from
     */
    explicit PlayedCardInTree(web::json::value obj)
    {
        card = std::make_shared<Card>(obj["card"]);
        whoPlayedId = obj["whoPlayedId"].as_integer();
        parentCardId = obj["parentCardId"].as_integer();
    }

    /**
     * card this object represents
     */
    std::shared_ptr<Card> card;

    /**
     * id of player who played this card
     */
    int whoPlayedId;

    /**
     * id of a card one level higher in hierarchy
     */
    int parentCardId;
};


#endif //TRACH_PLAYEDCARDINTREE_H
