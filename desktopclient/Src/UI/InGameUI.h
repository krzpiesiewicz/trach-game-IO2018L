
#ifndef TRACH_INGAMEUI_H
#define TRACH_INGAMEUI_H


#include <QtWidgets/QWidget>
#include <Src/Core/GameState.h>
#include <Src/UI/CurrentTreeTableUI.h>
#include <QtWidgets/QHBoxLayout>
#include <thread>
#include "HandUI.h"
#include "PlayersUI.h"
#include "../ServerConnection.h"

class InGameUI : public QWidget
{
Q_OBJECT

private:
    PlayerInfoUI* mainPlayerInfo;
    PlayersUI* playersInfo;
    CurrentTreeTableUI* currentTree;
    HandUI* handUI;
    int playerId;


public:
    bool isUpdating;
    InGameUI(QWidget *parent, GameState *state, int playerId, ServerConnection* connection)
            : QWidget(parent)
    {
        playersInfo = new PlayersUI(this, connection);
        this->playerId = playerId;
        auto &players = state->players;
        auto player = state->findPlayerById(playerId);
        handUI = new HandUI(parent, player->hand);

        auto mainLayout = new QHBoxLayout(parent);

        QVBoxLayout *leftMainLayout = new QVBoxLayout();

        QHBoxLayout *horizontalLayout = new QHBoxLayout();
        currentTree = new CurrentTreeTableUI(this, connection, state, playerId, handUI);
        horizontalLayout->addWidget(currentTree);
        horizontalLayout->addWidget(new CurrentTreeTableUI(this, connection, state, playerId, handUI));


        auto bottomLayout = new QHBoxLayout();


        QVBoxLayout *rightBarLayout = new QVBoxLayout();

        rightBarLayout->addLayout(playersInfo->layout);

        mainPlayerInfo = new PlayerInfoUI(parent, *(player), connection);


        bottomLayout->addWidget(mainPlayerInfo, 1);
        bottomLayout->addWidget(handUI, 100);

        leftMainLayout->addLayout(horizontalLayout, 100);
        leftMainLayout->addLayout(bottomLayout, 1);

        mainLayout->addLayout(leftMainLayout, 10);
        mainLayout->addLayout(rightBarLayout, 1);

        setData(state);
    }

public slots:
    void setData(GameState* state)
    {
        playersInfo->setData(state->players);
        currentTree->setData(state);
        handUI->setData(state->findPlayerById(playerId)->hand);
        isUpdating = false;
    }
};


#endif //TRACH_INGAMEUI_H
