//#include <QtWidgets>
//#include <UI/PlayerInfoUI.h>
#include <cpprest/ws_client.h>
#include <cpprest/producerconsumerstream.h>
#include <cpprest/json.h>
#include "cpprest/containerstream.h"
#include "cpprest/filestream.h"
#include "ServerConnection.h"
#include <iostream>
#include <sstream>
#include <Src/Core/Requests/GameStateRequest.h>
#include <QtWidgets/QApplication>
#include <Src/UI/PlayerInfoUI.h>
#include <QtWidgets/QWidget>
#include <Src/UI/InGameUI.h>
#include <Src/Core/GUIUpdater.h>
#include <thread>


using namespace web;
using namespace web::websockets::client;
using namespace web::json;
using namespace std;

volatile int lastId = 0;

void updateGUI(GUIUpdater *updater, ServerConnection* connection, InGameUI *mainUI)
{
    lastId++;
    auto gameStateResponse = connection->getCurrentState();
    auto gameState = gameStateResponse->gameState;
    gameState.get()->roundId = lastId;
    mainUI->isUpdating = true;
    updater->sendUpdate(gameState.get());
    while (mainUI->isUpdating)
    {
        usleep(1000000);
    }
}

int main(int argc, char *argv[])
{

    ServerConnection* connection = new ServerConnection;
    connection->connect();
    connection->startGame();

    auto gameStateResponse = connection->getCurrentState();

    int playerId = connection->gameplayState->playerId;
    auto &gameState = gameStateResponse->gameState;
    gameState.get()->roundId = lastId;

    QApplication app(argc, argv);
    QWidget window;
    window.resize(900, 480);
    window.setWindowTitle("Trach v0.1");

    InGameUI *mainUI = new InGameUI(&window, gameState.get(), playerId, connection);

    auto *updater = new GUIUpdater();
    QObject::connect(updater, SIGNAL(sendUpdate(GameState * )), mainUI, SLOT(setData(GameState * )));
    auto loopTask = pplx::task<void>([&]()
                                 {
                                     while (lastId >= 0)
                                     {
                                         updateGUI(updater, connection, mainUI);
                                     }

                                 });

    window.show();
    return app.exec();

}