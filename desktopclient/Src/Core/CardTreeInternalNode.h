
#ifndef TRACH_CARDTREEINTERNALNODE_H
#define TRACH_CARDTREEINTERNALNODE_H

#include <vector>

/**
 * helper class used to transfer data about card trees between UI components
 */
class CardTreeInternalNode
{
public:

    /**
     * id of card this node represents
     */
    int cardId;

    /**
     * cards one level lower in tree hierarchy than this node
     */
    std::vector<CardTreeInternalNode>children;
};


#endif //TRACH_CARDTREEINTERNALNODE_H
