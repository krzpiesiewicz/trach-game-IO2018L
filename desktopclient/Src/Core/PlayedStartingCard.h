
#ifndef TRACH_PLAYEDSTARTINGCARD_H
#define TRACH_PLAYEDSTARTINGCARD_H


#include <memory>
#include "Card.h"
#include <cpprest/json.h>

using namespace web;
using namespace std;

class PlayedStartingCard
{
public:
    PlayedStartingCard(json::value obj)
    {
        card = make_shared<Card>(obj["card"]);
        targetId = obj["whoPlayedId"].as_integer();
    }

    shared_ptr<Card> card;
    int whoPlayedId;
    int targetId;
    int type;
};


#endif //TRACH_PLAYEDSTARTINGCARD_H
