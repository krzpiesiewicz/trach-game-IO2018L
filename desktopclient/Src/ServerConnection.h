
#ifndef TRACH_SERVERCONNECTION_H
#define TRACH_SERVERCONNECTION_H

#include <iostream>
#include <cpprest/ws_client.h>
#include <cpprest/producerconsumerstream.h>
#include <cpprest/json.h>
#include <Src/Core/GameState.h>
#include <Src/Core/Responses/GameplayStateUpdate.h>
#include <Src/Core/Requests/GameStateRequest.h>
#include <Src/Core/Responses/GameStateUpdate.h>
#include <Src/Core/CardTree.h>
#include <Src/Core/CardTreeInternalNode.h>
#include "cpprest/containerstream.h"
#include "cpprest/filestream.h"
#include "Src/Core/Requests/QuickMultiplayerGameRequest.h"

using namespace web;
using namespace web::websockets::client;
using namespace web::json;
using namespace std;

class ServerConnection
{
private:
    websocket_client client;
    int updateId;
    Player* mainPlayer;

    string receive()
    {
        try
        {
            auto message = client.receive().get();
            auto messageString = message.extract_string().get();
            return messageString;
        }
        catch (const websocket_exception &ex)
        {
            cout << "receiving message failed\n";
            cout << ex.what() << "\n";
            return "";
        }
    }

    void sendMessage(const string &message)
    {
        try
        {
            websocket_outgoing_message msg;
            msg.set_utf8_message(message);
            client.send(msg).get();
        }
        catch (const websocket_exception &ex)
        {
            cout << "sending message : " << message << " failed\n";
            cout << ex.what() << "\n";
        }
    }

public:

    GameplayStateUpdate* gameplayState;

    GameStateUpdate *getCurrentState()
    {
        GameStateRequest request(gameplayState->gameplayId);
        auto message = receive();
        auto *result = new GameStateUpdate(message);
        updateId = result->updateId;
        mainPlayer = result->gameState->findPlayerById(gameplayState->playerId);
        return result;
    }

    void playCardTreeAtCardTree(CardTreeInternalNode* tree, int targetCardId)
    {
        int playerId = gameplayState->playerId;
        int gameplayId = gameplayState->gameplayId;
        string msgType = "PlayedCardsRequest";

        json::value obj = json::value::parse("{}");
        obj["playerId"] = json::value(playerId);
        obj["gamePlayId"] = json::value(gameplayId);
        obj["msgType"] = json::value(msgType);
        obj["updateId"] = json::value(updateId);

        json::value startingCard = json::value::parse("{}");;
        startingCard["type"] = json::value("PlayedCardInTree");
        startingCard["card"] = mainPlayer->findCardById(tree->cardId)->toJson();
        startingCard["whoPlayedId"] = json::value(playerId);
        startingCard["parentCardId"] = json::value(targetCardId);

        json::value cardNode = json::value::parse("{}");
        cardNode["playedCard"] = startingCard;
        cardNode["childrenNodes"] = json::value::parse("[]");

        obj["played"] = cardNode;

        cout <<"Played Card: " << obj.serialize() <<"\n";
        sendMessage(obj.serialize());
    }

    void playCardAtPlayer(CardTreeInternalNode* tree, int targetPlayerId)
    {
        int playerId = gameplayState->playerId;
        int gameplayId = gameplayState->gameplayId;
        string msgType = "PlayedCardsRequest";

        json::value obj = json::value::parse("{}");
        obj["playerId"] = json::value(playerId);
        obj["gamePlayId"] = json::value(gameplayId);
        obj["msgType"] = json::value(msgType);
        obj["updateId"] = json::value(updateId);

        json::value startingCard = json::value::parse("{}");;
        startingCard["type"] = json::value("PlayedStartingCardAtPlayer");
        startingCard["card"] = mainPlayer->findCardById(tree->cardId)->toJson();
        startingCard["whoPlayedId"] = json::value(playerId);
        startingCard["targetPlayerId"] = json::value(targetPlayerId);

        json::value cardTree = json::value::parse("{}");
        cardTree["playedCard"] = startingCard;
        cardTree["childrenNodes"] = json::value::parse("[]");

        obj["played"] = cardTree;

        cout <<"Played Card: " << obj.serialize() <<"\n";
        sendMessage(obj.serialize());
    }

    void connect()
    {
        try
        {
            client.connect(U("ws://localhost:9000/ws")).get();
            cout << "connected!" << "\n";
        }
        catch (const websocket_exception &ex)
        {
            cout << ex.what() << "\n";
        }
    }

    void startGame()
    {
        cout << "sending startGame request\n";
        QuickMultiplayerGameRequest request;
        sendMessage(request.toString());
        auto messageString = receive();
        gameplayState = new GameplayStateUpdate(messageString);
        cout << "started game! \n";
        cout << "playerId: " << gameplayState->playerId << "\n";
        cout << "gameplayId: " << gameplayState->gameplayId << "\n";

    }
};


#endif //TRACH_SERVERCONNECTION_H
