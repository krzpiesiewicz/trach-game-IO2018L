
#ifndef TRACH_PLAYEDCARDINTREE_H
#define TRACH_PLAYEDCARDINTREE_H


#include "Card.h"
#include <cpprest/json.h>

using namespace web;
using namespace std;

class PlayedCardInTree
{
public:

    explicit PlayedCardInTree(json::value obj)
    {
        card = make_shared<Card>(obj["card"]);
        whoPlayedId = obj["whoPlayedId"].as_integer();
        parentCardId = obj["parentCardId"].as_integer();
    }

    shared_ptr<Card> card;
    int whoPlayedId;
    int parentCardId;

};


#endif //TRACH_PLAYEDCARDINTREE_H
