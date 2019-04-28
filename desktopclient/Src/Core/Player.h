
#ifndef TRACH_PLAYER_H
#define TRACH_PLAYER_H


#include <boost/algorithm/string.hpp>
#include <vector>
#include "Card.h"

using namespace std;
using namespace web;

class Player
{
public:

    explicit Player(json::value obj)
    {
        id = obj["id"].as_integer();
        name = obj["name"].as_string();
        health = obj["health"].as_integer();
        cout << id << " " << name << " " << health << "\n";
        for (auto &rawCard : obj["hand"].as_array())
        {
            hand.emplace_back(rawCard);
        }
        for (auto &rawCard : obj["activeCards"].as_array())
        {
            activeCards.emplace_back(rawCard);
        }
    }

    int id;
    std::string name;
    int health;
    std::vector<Card> hand;
    std::vector<Card> activeCards;

    Card *findCardById(int cardId)
    {
        return find_if(hand.begin(),
                       hand.end(),
                       [&](Card &x) { return x.id == cardId; }).base();
    }
};


#endif //TRACH_PLAYER_H
