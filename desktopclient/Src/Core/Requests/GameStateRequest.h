
#ifndef TRACH_GAMESTATEREQUEST_H
#define TRACH_GAMESTATEREQUEST_H

/**
 * request to get latest gameState
 */
class GameStateRequest
{
public:

    GameStateRequest(const int gamePlayId)
    {
        obj = web::json::value();
        obj["msgType"] = web::json::value("GameStateRequest");
        obj["gamePlayId"] = web::json::value(gamePlayId);
    }

    std::string toString()
    {
        return obj.serialize();
    }

private:

    web::json::value obj;

};


#endif //TRACH_GAMESTATEREQUEST_H
