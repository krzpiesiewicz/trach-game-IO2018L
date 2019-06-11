
#ifndef TRACH_QUICKMULTIPLAYERGAMEREQUEST_H
#define TRACH_QUICKMULTIPLAYERGAMEREQUEST_H

#include <string>
#include <cpprest/json.h>

/**
 * request to start new multiplayer game
 */
class QuickMultiplayerGameRequest
{
public:

    QuickMultiplayerGameRequest()
    {
        obj = web::json::value();
        obj["msgType"] = web::json::value("QuickMultiplayerGameRequest");
    }

    /**
     *
     * @return object serialized to JSON as string
     */
    std::string toString()
    {
        return obj.serialize();
    }

private:

    web::json::value obj;
};


#endif //TRACH_QUICKMULTIPLAYERGAMEREQUEST_H
