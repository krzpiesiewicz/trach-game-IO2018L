
#ifndef TRACH_CARDNODE_H
#define TRACH_CARDNODE_H


#include "PlayedCardInTree.h"
#include <cpprest/json.h>

/**
 * single node in card tree
 */
class CardNode
{
public:

    /**
     * @param obj object to deserialize from
     */
    explicit CardNode(web::json::value obj)
    {
        playedCard = new PlayedCardInTree(obj["playedCard"]);
        for (auto& rawChild : obj["childrenNodes"].as_array())
        {
            childrenNodes.emplace_back(rawChild);
        }
    }

    /**
     * card this object represents
     */
    PlayedCardInTree* playedCard;

    /**
     * cards one level below in tree hierarchy than playedCard
     */
    std::vector<CardNode>childrenNodes;
};


#endif //TRACH_CARDNODE_H
