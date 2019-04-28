
#ifndef TRACH_GAMESTATEUPDATE_H
#define TRACH_GAMESTATEUPDATE_H

#include <cpprest/json.h>
#include <Src/Core/GameState.h>

using namespace std;
using namespace web;

class GameStateUpdate
{
public:
    explicit GameStateUpdate(string json)
    {
        auto obj = json::value::parse(json);
        gameplayId = obj["gamePlayId"].as_integer();
        updateId = obj["updateId"].as_integer();
        gameState = make_shared<GameState>(obj["gameState"]);
    }

    int gameplayId;
    int updateId;
    shared_ptr<GameState> gameState;
};


#endif //TRACH_GAMESTATEUPDATE_H
