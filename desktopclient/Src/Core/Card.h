
#ifndef TRACH_CARD_H
#define TRACH_CARD_H

#include <string>
#include <cpprest/json.h>

using namespace std;
using namespace web;

class Card
{
public:

    explicit Card(json::value obj)
    {
        id = obj["id"].as_integer();
        type = obj["type"].as_string();
    }

    json::value toJson()
    {
        json::value result;
        result["id"] = id;
        result["type"] = json::value(type);
        return result;
    }

    int id;
    std::string type;
};


#endif //TRACH_CARD_H
