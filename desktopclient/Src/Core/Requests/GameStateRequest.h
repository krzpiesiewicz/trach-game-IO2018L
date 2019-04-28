
#ifndef TRACH_GAMESTATEREQUEST_H
#define TRACH_GAMESTATEREQUEST_H

using namespace std;
using namespace web;

class GameStateRequest
{
public:
    GameStateRequest(const int gamePlayId)
    {
        obj = json::value();
        obj["msgType"] = json::value("GameStateRequest");
        obj["gamePlayId"] = json::value(gamePlayId);
    }

    json::value obj;

    string toString()
    {
        return obj.serialize();
    }

};


#endif //TRACH_GAMESTATEREQUEST_H
