
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

using namespace web::websockets::client;
using namespace web::json;

class ServerConnection
{
private:
    websocket_client client;
    int updateId;
    Player *mainPlayer;
    std::string nextEvaluationDate;

    std::string receive()
    {
        try
        {
            auto message = client.receive().get();
            auto messageString = message.extract_string().get();
            std::cout << "received message: " << messageString << "\n";
            return messageString;
        }
        catch (const websocket_exception &ex)
        {
            std::cout << "receiving message failed\n";
            std::cout << ex.what() << "\n";
            return "";
        }
    }

    void sendMessage(const std::string &message)
    {
        try
        {
            websocket_outgoing_message msg;
            msg.set_utf8_message(message);
            client.send(msg).get();
            std::cout << "sending message: " << message << "\n";
        }
        catch (const websocket_exception &ex)
        {
            std::cout << "sending message: " << message << " failed\n";
            std::cout << ex.what() << "\n";
        }
    }

    GameplayStateUpdate *gameplayState;
public:


/**
 * waits for server to send new gameState
 * @return latest gameState
 */
    GameStateUpdate *getCurrentState()
    {
        GameStateRequest request(gameplayState->gameplayId);
        auto message = receive();
        auto *result = new GameStateUpdate(message);
        updateId = result->updateId;
        mainPlayer = result->gameState->findPlayerById(gameplayState->playerId);
        return result;
    }

    /**
     *
     * @return main player id
     */
    int getMainPlayerId() {
        return gameplayState->playerId;
    }

    /**
     * plays given tree at card with given id
     * @param tree tree to play
     * @param targetCardId what to play against
     */
    void playCardTreeAtCardTree(CardTreeInternalNode *tree, int targetCardId)
    {
        int playerId = gameplayState->playerId;
        int gameplayId = gameplayState->gameplayId;
        std::string msgType = "PlayedCardsRequest";

        web::json::value obj = web::json::value::parse("{}");
        obj["playerId"] = web::json::value(playerId);
        obj["gamePlayId"] = web::json::value(gameplayId);
        obj["msgType"] = web::json::value(msgType);
        obj["updateId"] = web::json::value(updateId);

        web::json::value startingCard = web::json::value::parse("{}");;
        startingCard["type"] = web::json::value("PlayedCardInTree");
        startingCard["card"] = mainPlayer->findCardById(tree->cardId)->toJson();
        startingCard["whoPlayedId"] = web::json::value(playerId);
        startingCard["parentCardId"] = web::json::value(targetCardId);

        web::json::value cardNode = web::json::value::parse("{}");
        cardNode["playedCard"] = startingCard;
        cardNode["childrenNodes"] = web::json::value::parse("[]");

        obj["played"] = cardNode;

        sendMessage(obj.serialize());
    }

    /**
     * plays given tree at player with given id
     * @param tree tree to play
     * @param targetPlayerId what to play against
     */
    void playCardAtPlayer(CardTreeInternalNode *tree, int targetPlayerId)
    {
        int playerId = gameplayState->playerId;
        int gameplayId = gameplayState->gameplayId;
        std::string msgType = "PlayedCardsRequest";

        web::json::value obj = web::json::value::parse("{}");
        obj["playerId"] = web::json::value(playerId);
        obj["gamePlayId"] = web::json::value(gameplayId);
        obj["msgType"] = web::json::value(msgType);
        obj["updateId"] = web::json::value(updateId);

        web::json::value startingCard = web::json::value::parse("{}");;
        startingCard["type"] = web::json::value("PlayedStartingCardAtPlayer");
        startingCard["card"] = mainPlayer->findCardById(tree->cardId)->toJson();
        startingCard["whoPlayedId"] = web::json::value(playerId);
        startingCard["targetPlayerId"] = web::json::value(targetPlayerId);

        web::json::value cardTree = web::json::value::parse("{}");
        cardTree["playedCard"] = startingCard;
        cardTree["childrenNodes"] = web::json::value::parse("[]");

        obj["played"] = cardTree;

        sendMessage(obj.serialize());
    }

    /**
     * establishes connection with server, needs to be called once
     * @param serverAddress address of server
     */
    void connect(std::string serverAddress)
    {
        try
        {
            std::cout << "connecting to " << serverAddress << "\n";
            client.connect(serverAddress).get();
            std::cout << "connected!" << "\n";
        }
        catch (const websocket_exception &ex)
        {
            std::cout << ex.what() << "\n";
        }
    }

    /**
     * sends message to start game and waits for other players to join and to start game
     */
    void startGame()
    {
        std::cout << "sending startGame request\n";
        QuickMultiplayerGameRequest request;
        sendMessage(request.toString());
        auto messageString = receive();
        gameplayState = new GameplayStateUpdate(messageString);
        std::cout << "started game! \n";

    }
};


#endif //TRACH_SERVERCONNECTION_H
