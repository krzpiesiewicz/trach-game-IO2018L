
#ifndef TRACH_GAMESTATE_H
#define TRACH_GAMESTATE_H

#include<vector>
#include "Player.h"

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
        cout << obj["coveredCardsStack"].serialize() << "\n";
        cout << obj["usedCardsStack"].serialize() << "\n";
        cout << obj["tableActiveCards"].serialize() << "\n";
    }

    std::vector<Player>players;
    std::vector<Card>coveredCardsStack;
    std::vector<Card>usedCardsStack;
    std::vector<Card>tableActiveCards;
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
