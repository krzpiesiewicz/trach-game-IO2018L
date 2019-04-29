
#ifndef TRACH_CARDNODE_H
#define TRACH_CARDNODE_H


#include "PlayedCardInTree.h"
#include <cpprest/json.h>

using namespace std;
using namespace web;

class CardNode
{
public:
    explicit CardNode(json::value obj)
    {
        playedCard = new PlayedCardInTree(obj["playedCard"]);
        for (auto& rawChild : obj["childrenNodes"].as_array())
        {
            childrenNodes.emplace_back(rawChild);
        }
    }

    PlayedCardInTree* playedCard;
    vector<CardNode>childrenNodes;
};


#endif //TRACH_CARDNODE_H
