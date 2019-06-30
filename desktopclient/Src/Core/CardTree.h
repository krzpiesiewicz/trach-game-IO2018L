
#ifndef TRACH_CARDTREE_H
#define TRACH_CARDTREE_H


#include <vector>
#include "PlayedStartingCard.h"
#include "CardNode.h"
#include <cpprest/json.h>

/**
 * tree of cards with root at some card
 */
class CardTree
{
public:

    /**
     * @param obj object to deserialize from
     */
    CardTree(web::json::value obj)
    {
        playedCard = new PlayedStartingCard(obj["playedCard"]);
        for(auto& rawNode : obj["childrenNodes"].as_array())
        {
            childrenNodes.emplace_back(rawNode);
        }
    }

    /**
     * card this object represents
     */
    PlayedStartingCard* playedCard;

    /**
     * cards one level below in tree hierarchy than playedCard
     */
    std::vector<CardNode>childrenNodes;
};


#endif //TRACH_CARDTREE_H
