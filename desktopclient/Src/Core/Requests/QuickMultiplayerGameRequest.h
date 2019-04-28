
#ifndef TRACH_QUICKMULTIPLAYERGAMEREQUEST_H
#define TRACH_QUICKMULTIPLAYERGAMEREQUEST_H

#include <string>
#include <cpprest/json.h>

using namespace std;
using namespace web;

class QuickMultiplayerGameRequest
{
public:

    QuickMultiplayerGameRequest()
    {
        obj = json::value();
        obj["msgType"] = json::value("QuickMultiplayerGameRequest");
    }

    json::value obj;

    string toString()
    {
        return obj.serialize();
    }

};


#endif //TRACH_QUICKMULTIPLAYERGAMEREQUEST_H
