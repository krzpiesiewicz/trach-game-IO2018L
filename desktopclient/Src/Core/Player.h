
#ifndef TRACH_PLAYER_H
#define TRACH_PLAYER_H


#include <boost/algorithm/string.hpp>
#include <vector>
#include "Card.h"

/**
 * represents all data of a signle player
 */
class Player
{
public:

    /**
     * @param obj object to deserialize from
     */
    explicit Player(web::json::value obj)
    {
        id = obj["id"].as_integer();
        name = obj["name"].as_string();
        health = obj["health"].as_integer();
        for (auto &rawCard : obj["hand"].as_array())
        {
            hand.emplace_back(rawCard);
        }
        for (auto &rawCard : obj["activeCards"].as_array())
        {
            activeCards.emplace_back(rawCard);
        }
    }

    /**
     * player id
     */
    int id;

    /**
     * player name
     */
    std::string name;

    /**
     * current health level
     */
    int health;

    /**
     * player's current hand
     */
    std::vector<Card> hand;

    /**
     * player's active cards
     */
    std::vector<Card> activeCards;

    /**
     * tries to find a card with given id among cards on players hand
     * @param cardId id to look for
     * @return found card
     */
    Card *findCardById(int cardId)
    {
        return find_if(hand.begin(),
                       hand.end(),
                       [&](Card &x) { return x.id == cardId; }).base();
    }
};


#endif //TRACH_PLAYER_H
