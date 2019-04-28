
#ifndef TRACH_CARDTREEINTERNALNODE_H
#define TRACH_CARDTREEINTERNALNODE_H

#include <vector>

using namespace std;

class CardTreeInternalNode
{
public:

    int cardId;
    vector<CardTreeInternalNode>children;
};


#endif //TRACH_CARDTREEINTERNALNODE_H
