
#ifndef TRACH_CARDTREE_H
#define TRACH_CARDTREE_H


#include <vector>
#include "PlayedStartingCard.h"
#include "CardNode.h"
#include <cpprest/json.h>

using namespace std;
using namespace web;

class CardTree
{
public:

    CardTree(json::value obj)
    {
        playedCard = new PlayedStartingCard(obj["playedCard"]);
        for(auto& rawNode : obj["childrenNodes"].as_array())
        {
            childrenNodes.emplace_back(rawNode);
        }
    }

    PlayedStartingCard* playedCard;
    vector<CardNode>childrenNodes;
};


#endif //TRACH_CARDTREE_H
