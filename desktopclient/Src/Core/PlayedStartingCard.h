
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
        card = new Card(obj["card"]);
        whoPlayedId = obj["whoPlayedId"].as_integer();
    }

    Card* card;
    int whoPlayedId;
    int targetId;
    int type;
};


#endif //TRACH_PLAYEDSTARTINGCARD_H
