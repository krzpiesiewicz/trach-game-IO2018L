
#ifndef TRACH_CARD_H
#define TRACH_CARD_H

#include <string>
#include <cpprest/json.h>

/**
 * single card from deck
 */
class Card
{
public:

    /**
     * @param obj object to deserialize from
     */
    explicit Card(web::json::value obj)
    {
        id = obj["id"].as_integer();
        type = obj["type"].as_string();
    }

    /**
     * serializes object to JSON
     * @returns object serialized to string in JSON format
     */
    web::json::value toJson()
    {
        web::json::value result;
        result["id"] = id;
        result["type"] = web::json::value(type);
        return result;
    }

    int id;

    std::string type;
};


#endif //TRACH_CARD_H
