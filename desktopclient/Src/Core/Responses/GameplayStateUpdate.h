
#ifndef TRACH_GAMEPLAYSTATEUPDATE_H
#define TRACH_GAMEPLAYSTATEUPDATE_H

#include <string>
#include <cpprest/json.h>

using namespace std;
using namespace web;

class GameplayStateUpdate
{
public:

    string gameplayState;
    int playerId;
    int gameplayId;

    GameplayStateUpdate(const string json)
    {
        json::value obj = json::value::parse(json);
        gameplayState = obj["gamePlayState"].as_string();
        playerId = obj["playerId"].as_integer();
        gameplayId = obj["gamePlayId"].as_integer();
    }
};


#endif //TRACH_GAMEPLAYSTATEUPDATE_H
