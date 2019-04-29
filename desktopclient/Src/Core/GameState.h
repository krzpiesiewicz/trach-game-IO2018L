
#ifndef TRACH_GAMESTATE_H
#define TRACH_GAMESTATE_H

#include<vector>
#include "Player.h"
#include "CardTree.h"

using namespace std;
using namespace web;

class GameState
{
public:

    explicit GameState(json::value obj)
    {
        auto rawPlayers = obj["players"].as_array();
        for (auto& rawPlayer : rawPlayers)
        {
            cout <<rawPlayer.serialize()<<"\n";
            players.emplace_back(rawPlayer);
        }
        cout <<"coveredCards: "<< obj["coveredCardsStack"].serialize() << "\n";
        cout <<"usedCardsStack: "<< obj["usedCardsStack"].serialize() << "\n";
        cout <<"tableActiveCards: "<< obj["tableActiveCards"].serialize() << "\n";


        playerIdOnmove = obj["playerIdOnMove"].as_integer();
        hasCardTree = obj.has_object_field("cardTree");
        if (hasCardTree)
        {
            cout <<"cardTree: "<< obj["cardTree"].serialize() << "\n";
            cardTree = new CardTree(obj["cardTree"]);
        }
    }

    std::vector<Player>players;
    std::vector<Card>coveredCardsStack;
    std::vector<Card>usedCardsStack;
    std::vector<Card>tableActiveCards;
    CardTree* cardTree;
    bool hasCardTree;
    int roundId;
    int playerIdOnmove;

    Player* findPlayerById(int playerId)
    {
        return find_if(players.begin(),
                              players.end(),
                              [&](Player &x) { return x.id == playerId; }).base();
    }

};


#endif //TRACH_GAMESTATE_H
