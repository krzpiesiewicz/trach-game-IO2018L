
#ifndef TRACH_GAMEPLAYSTATEUPDATE_H
#define TRACH_GAMEPLAYSTATEUPDATE_H

#include <string>
#include <cpprest/json.h>

/**
 * response from server with all information about gameplay
 */
class GameplayStateUpdate
{
public:

    std::string gameplayState;

    /**
     * id of main player
     */
    int playerId;

    /**
     * id of gameplay
     */
    int gameplayId;

    /**
     *
     * @param json object to deserialize from
     */
    GameplayStateUpdate(const std::string json)
    {
        web::json::value obj = web::json::value::parse(json);
        gameplayState = obj["gamePlayState"].as_string();
        playerId = obj["playerId"].as_integer();
        gameplayId = obj["gamePlayId"].as_integer();
    }
};


#endif //TRACH_GAMEPLAYSTATEUPDATE_H
