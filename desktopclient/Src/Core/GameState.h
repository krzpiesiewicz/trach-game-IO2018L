
#ifndef TRACH_GAMESTATE_H
#define TRACH_GAMESTATE_H

#include<vector>
#include "Player.h"
#include "CardTree.h"

/**
 * represents all data about current state of the game
 */
class GameState
{
public:

    /**
     * @param obj object to deserialize from
     */
    explicit GameState(web::json::value obj)
    {
        auto rawPlayers = obj["players"].as_array();
        for (auto& rawPlayer : rawPlayers)
        {
            players.emplace_back(rawPlayer);
        }

        playerIdOnMove = obj["playerIdOnMove"].as_integer();
        hasCardTree = obj["cardTrees"].as_array().size();

        if (hasCardTree)
        {
            cardTree = new CardTree(obj["cardTrees"].as_array().at(0));
        }
    }

    /**
     * players that are currently playing the game
     */
    std::vector<Player>players;

    /**
     * card tree that is on the table
     */
    CardTree* cardTree;

    /**
     * is there any card tree on the table
     */
    bool hasCardTree;

    /**
     * id of current round
     */
    int roundId;

    /**
     * player that currently has possibility to play cards
     */
    int playerIdOnMove;

    /**
     * time of next planned evaluation
     */
    std::string evaluationTime;

    /**
     *
     * @param playerId id to look for
     * @return found player
     */
    Player* findPlayerById(int playerId)
    {
        return find_if(players.begin(),
                              players.end(),
                              [&](Player &x) { return x.id == playerId; }).base();
    }

};


#endif //TRACH_GAMESTATE_H
